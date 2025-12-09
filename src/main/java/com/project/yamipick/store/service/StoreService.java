package com.project.yamipick.store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.reservation.repository.StoreScheduleRepository;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

	private final StoreRepository storeRepository;
	private final StoreScheduleRepository storeScheduleRepository;
	private final StoreTableTypeRepository storeTableTypeRepository;

	public Optional<Store> findByUser(User user) {
		return storeRepository.findByUser(user);
	}

	public boolean hasStore(User user) {
		return storeRepository.existsByUser(user);
	}

	public List<StoreSchedule> getSchedules(Store store) {
		return storeScheduleRepository.findByStore(store);
	}

	public List<StoreTableType> getTableTypes(Store store) {
		return storeTableTypeRepository.findByStore(store);
	}

	/**
	 * 매장 + 스케줄 + 테이블 타입까지 한 번에 등록
	 */
	public Store registerStoreWithDetail(User user, String kakaoPlaceId, String name, String address, String phone,
			String openTime, String closeTime, String breakStart, String breakEnd, List<String> tableTypeName,
			List<Integer> tableCapacity, List<Integer> tableQuantity) {

		// 1) 매장 중복 방어
		if (storeRepository.existsByUser(user)) {
			throw new IllegalStateException("이미 매장이 등록되어 있습니다.");
		}

		if (storeRepository.existsByKakaoPlaceId(kakaoPlaceId)) {
			throw new IllegalStateException("이미 등록된 카카오 장소입니다.");
		}

		// 1-1) ★ 영업시간 / 브레이크타임 검증 추가
		// 영업시간은 무조건 있어야 함
		if (openTime == null || openTime.isBlank() || closeTime == null || closeTime.isBlank()) {
			throw new IllegalArgumentException("영업시간은 반드시 설정해야 합니다.");
		}

		// 브레이크 타임은 둘 다 비우거나, 둘 다 채우거나
		boolean hasBreakStart = (breakStart != null && !breakStart.isBlank());
		boolean hasBreakEnd = (breakEnd != null && !breakEnd.isBlank());

		if (hasBreakStart != hasBreakEnd) {
			throw new IllegalArgumentException("브레이크 타임은 시작과 종료를 모두 입력해야 합니다.");
		}

		// 2) 매장 저장
		Store store = Store.builder().kakaoPlaceId(kakaoPlaceId).name(name).address(address).phone(phone).user(user)
				.build();

		store = storeRepository.save(store);

		// 3) 영업시간 → StoreSchedule 생성
		// 일단은 0~6(일~토) 전부 같은 시간으로 넣어주는 간단 버전
		for (int day = 0; day <= 6; day++) {
			StoreSchedule schedule = StoreSchedule.builder().store(store).dayOfWeek(day).isOpen("Y").openTime(openTime)
					.closeTime(closeTime).breakStart(breakStart).breakEnd(breakEnd).build();

			storeScheduleRepository.save(schedule);
		}

		// 4) 테이블 타입들 생성
		if (tableTypeName != null) {
			for (int i = 0; i < tableTypeName.size(); i++) {

				String typeName = tableTypeName.get(i);
				Integer capacity = safeGet(tableCapacity, i);
				Integer quantity = safeGet(tableQuantity, i);

				if (typeName == null || typeName.isBlank())
					continue;
				if (capacity == null || quantity == null)
					continue;

				StoreTableType tableType = StoreTableType.builder().store(store).name(typeName).capacity(capacity)
						.quantity(quantity).build();

				storeTableTypeRepository.save(tableType);
			}
		}

		return store;
	}

	// 리스트 길이 체크용 보조 메서드
	private Integer safeGet(List<Integer> list, int idx) {
		if (list == null)
			return null;
		if (idx < 0 || idx >= list.size())
			return null;
		return list.get(idx);
	}

	// StoreService 안에 추가
	public void initSchedulesIfEmpty(Store store) {

		List<StoreSchedule> list = storeScheduleRepository.findByStore(store);

		// 이미 스케줄 있으면 아무 것도 안 함
		if (!list.isEmpty()) {
			return;
		}

		// 여기서 사용할 기본 값 (원하는 값으로 바꿔도 됨)
		String defaultOpenTime = "00:00";
		String defaultCloseTime = "00:00";

		// 없으면 0~6(일~토) 기본 row 생성
		for (int day = 0; day <= 6; day++) {
			StoreSchedule schedule = StoreSchedule.builder().store(store).dayOfWeek(day).isOpen("Y") // 기본값: 영업
					.openTime(defaultOpenTime) // 처음엔 빈 상태로 두고
					.closeTime(defaultCloseTime) // 수정 화면에서 입력하게
					.breakStart(null).breakEnd(null).build();

			storeScheduleRepository.save(schedule);
		}
	}

	public void updateSchedules(Store store, List<Long> seqSchedule, List<String> isOpen, List<String> openTime,
			List<String> closeTime, List<String> breakStart, List<String> breakEnd) {

		for (int i = 0; i < seqSchedule.size(); i++) {
			Long id = seqSchedule.get(i);

			StoreSchedule schedule = storeScheduleRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("스케줄이 없습니다. id=" + id));

			// 혹시 다른 매장 것 건드리는 거 방지
			if (!schedule.getStore().getSeqStore().equals(store.getSeqStore())) {
				throw new IllegalStateException("다른 매장의 스케줄입니다.");
			}

			schedule.updateSchedule(isOpen.get(i), openTime.get(i), closeTime.get(i), breakStart.get(i),
					breakEnd.get(i));
		}
	}

	/**
	 * 테이블 타입 수정 - 대쉬보드에서 넘어온 타입 id / 이름 / 인원 / 개수로 갈아끼우기
	 */
	public void updateTableTypes(Store store, List<Long> seqStoreTable, List<String> name, List<Integer> capacity,
			List<Integer> quantity) {

		if (seqStoreTable == null || name == null) {
			return;
		}

		for (int i = 0; i < seqStoreTable.size(); i++) {

			Long id = seqStoreTable.get(i);

			String typeName = name.get(i);
			Integer cap = capacity != null && i < capacity.size() ? capacity.get(i) : null;
			Integer qty = quantity != null && i < quantity.size() ? quantity.get(i) : null;

			if (typeName == null || typeName.isBlank())
				continue;
			if (cap == null || qty == null)
				continue;

			StoreTableType tableType = storeTableTypeRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("테이블 타입이 없습니다. id=" + id));

			if (!tableType.getStore().getSeqStore().equals(store.getSeqStore())) {
				throw new IllegalStateException("다른 매장의 테이블 타입입니다.");
			}

			tableType.updateTableType(typeName, cap, qty);
		}
	}

	//테이블 추가
	public void addTableTypes(Store store, List<String> newTableTypeName, List<Integer> newTableCapacity,
			List<Integer> newTableQuantity) {

		if (newTableTypeName == null) {
			return;
		}

		for (int i = 0; i < newTableTypeName.size(); i++) {
			String typeName = newTableTypeName.get(i);
			Integer cap = newTableCapacity != null && i < newTableCapacity.size() ? newTableCapacity.get(i) : null;
			Integer qty = newTableQuantity != null && i < newTableQuantity.size() ? newTableQuantity.get(i) : null;

			if (typeName == null || typeName.isBlank())
				continue;
			if (cap == null || qty == null)
				continue;

			StoreTableType newType = StoreTableType.builder().store(store).name(typeName).capacity(cap).quantity(qty)
					.build();

			storeTableTypeRepository.save(newType);
		}
	}

	/**
	 * 테이블 타입 삭제
	 */
	public void deleteTableType(Store store, Long seqStoreTableId) {
	    StoreTableType tableType = storeTableTypeRepository.findById(seqStoreTableId)
	            .orElseThrow(() -> new IllegalArgumentException("테이블 타입이 없습니다. id=" + seqStoreTableId));

	    if (!tableType.getStore().getSeqStore().equals(store.getSeqStore())) {
	        throw new IllegalStateException("다른 매장의 테이블 타입입니다.");
	    }

	    storeTableTypeRepository.delete(tableType);
	}
}
