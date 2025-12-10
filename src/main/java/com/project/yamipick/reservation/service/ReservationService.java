package com.project.yamipick.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.entity.Reservation;
import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.reservation.repository.ReservationRepository;
import com.project.yamipick.reservation.repository.StoreScheduleRepository;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StoreTableTypeRepository storeTableTypeRepository;
    private final StoreScheduleRepository storeScheduleRepository;

    /**
     * 예약 생성
     */
    public ReservationDTO createReservation(ReservationDTO dto) {

        User user = userRepository.findById(dto.getSeqUser())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. seqUser=" + dto.getSeqUser()));

        Store store = storeRepository.findById(dto.getSeqStore())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + dto.getSeqStore()));

        StoreTableType storeTableType = storeTableTypeRepository.findById(dto.getSeqStoreTable())
                .orElseThrow(() -> new IllegalArgumentException("테이블 타입을 찾을 수 없습니다. seqStoreTable=" + dto.getSeqStoreTable()));

        // 🔒 과거 날짜 예약 막기
        if (dto.getReserveDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("이미 지난 날짜로는 예약할 수 없습니다.");
        }
        
        // 🔒 예약 시간 필수 체크
        if (dto.getReserveTime() == null || dto.getReserveTime().isBlank()) {
            throw new IllegalArgumentException("예약 시간을 선택해 주세요.");
        }
        
     // 🔒 1단계: 인원수 vs 테이블용량 체크 (이미 tableSelect에서 한 번 걸렀지만 백엔드에서도 한 번 더)
        Integer capacity = storeTableType.getCapacity();
        Integer quantity = storeTableType.getQuantity();

        if (capacity == null || quantity == null || capacity <= 0 || quantity <= 0) {
            throw new IllegalStateException("테이블 타입 설정이 올바르지 않습니다.");
        }

        int people = dto.getPeopleCount();
        int neededTables = (people + capacity - 1) / capacity;

        if (neededTables > quantity) {
            throw new IllegalArgumentException("해당 테이블 타입으로는 인원을 수용할 수 없습니다.");
        }

        // TODO: 나중 단계에서
        //  - 매장 영업시간/요일 체크
        //  - 브레이크타임 체크
        //  - 이미 예약된 인원/테이블 반영해서 만석 계산

        Reservation reservation = Reservation.builder()
                .reserveDate(dto.getReserveDate())
                .reserveTime(dto.getReserveTime())
                .peopleCount(dto.getPeopleCount())
                .status("대기")  // 기본 상태
                .user(user)
                .store(store)
                .storeTableType(storeTableType)
                .build();
        
     // 🔒 영업시간 / 휴무일 / 브레이크 타임 체크
        validateStoreSchedule(store, dto.getReserveDate(), dto.getReserveTime());

        Reservation saved = reservationRepository.save(reservation);

        return toDTO(saved);
    }

    /**
     * 특정 유저의 전체 예약 내역
     */
    public List<ReservationDTO> getUserReservations(Long seqUser) {
        List<Reservation> list =
                reservationRepository.findByUser_SeqUserOrderByReserveDateDescReserveTimeDesc(seqUser);

        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 매장의 특정 날짜 예약 목록 (테이블 구분 X, 전체)
     */
    public List<ReservationDTO> getStoreReservations(Long seqStore, LocalDate date) {
        List<Reservation> list =
                reservationRepository.findByStore_SeqStoreAndReserveDate(seqStore, date);

        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 매장 + 테이블 타입 + 날짜별 예약 목록 (테이블별 예약 현황 조회용)
     */
    public List<ReservationDTO> getStoreTableReservations(Long seqStore, Long seqStoreTable, LocalDate date) {
        List<Reservation> list =
                reservationRepository.findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDate(
                        seqStore, seqStoreTable, date
                );

        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    //예약 상세보기
    public ReservationDTO getReservationDetail(Long seqReservation) {
        Reservation r = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        return toDTO(r);
    }
    
    @Value
    public static class TableOptionDTO {
        Long seqStoreTable;
        String name;
        boolean available;   // 나중에 만석 처리할 때 false로 줄 수 있음
    }

    public List<TableOptionDTO> getAvailableTableTypes(Long seqStore,
                                                       LocalDate reserveDate,
                                                       String reserveTime,
                                                       Integer peopleCount) {

        // 지금 단계에서는 단순히 "해당 매장의 모든 테이블 타입"을 다 내려줌
        Store store = storeRepository.findById(seqStore)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + seqStore));

        List<StoreTableType> types = storeTableTypeRepository.findByStore(store);

        return types.stream()
                .map(t -> {

                    int capacity = t.getCapacity();   // 테이블당 인원
                    int quantity = t.getQuantity();   // 테이블 개수

                    if (capacity <= 0 || quantity <= 0) {
                        // 이상한 값이면 바로 예약 불가
                        return new TableOptionDTO(t.getSeqStoreTable(), t.getName(), false);
                    }

                    // 2) 이미 이 시간대에 예약된 same 타입 예약들 가져오기
                    List<Reservation> reservedList =
                            reservationRepository
                                    .findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDateAndReserveTime(
                                            seqStore,
                                            t.getSeqStoreTable(),
                                            reserveDate,
                                            reserveTime
                                    );

                    // 3) 이미 사용 중인 테이블 개수 계산
                    int usedTables = reservedList.stream()
                            .mapToInt(r -> {
                                int pc = r.getPeopleCount();
                                // ceil(pc / capacity)
                                return (pc + capacity - 1) / capacity;
                            })
                            .sum();

                    int remainTables = quantity - usedTables;

                    // 4) 이번 예약에 필요한 테이블 수
                    int needTables = (peopleCount + capacity - 1) / capacity;

                    boolean available = remainTables >= needTables;

                    return new TableOptionDTO(
                            t.getSeqStoreTable(),
                            t.getName(),
                            available
                    );
                })
                .collect(Collectors.toList());
    }
    
    // 예약 취소용
    public void cancelReservation(Long seqReservation) {
        Reservation reservation = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        // 필요하면 상태 체크
        if (!"대기".equals(reservation.getStatus())) {
            throw new IllegalStateException("대기 상태의 예약만 취소할 수 있습니다.");
        }

        reservation.changeStatus("취소");
        // JPA 더티 체킹으로 자동 업데이트됨 (save() 안 해도 됨)
    }
    
    /**
     * 엔티티 → DTO 변환
     */
    private ReservationDTO toDTO(Reservation r) {
        return ReservationDTO.builder()
                .seqReservation(r.getSeqReservation())
                .reserveDate(r.getReserveDate())
                .reserveTime(r.getReserveTime())
                .peopleCount(r.getPeopleCount())
                .status(r.getStatus())
                .seqUser(r.getUser().getSeqUser())
                .seqStore(r.getStore().getSeqStore())
                .seqStoreTable(r.getStoreTableType().getSeqStoreTable())
                .storeName(r.getStore().getName())
                .tableTypeName(r.getStoreTableType().getName())
                .build();
    }
    
    private void validateStoreSchedule(Store store, LocalDate reserveDate, String reserveTime) {

        // 요일 계산: 일(0) ~ 토(6) 에 맞춰서
        int dayOfWeek; // 0=일, 1=월 ... 6=토

        switch (reserveDate.getDayOfWeek()) {
            case MONDAY    -> dayOfWeek = 1;
            case TUESDAY   -> dayOfWeek = 2;
            case WEDNESDAY -> dayOfWeek = 3;
            case THURSDAY  -> dayOfWeek = 4;
            case FRIDAY    -> dayOfWeek = 5;
            case SATURDAY  -> dayOfWeek = 6;
            case SUNDAY    -> dayOfWeek = 0;
            default        -> dayOfWeek = 0;
        }

        // ★ StoreScheduleRepository에 이 메서드가 없으면 추가해야 함
        StoreSchedule schedule = storeScheduleRepository
                .findByStoreAndDayOfWeek(store, dayOfWeek)
                .orElseThrow(() -> new IllegalStateException("해당 요일 스케줄이 설정되어 있지 않습니다."));

        // 1) 휴무일이면 막기
        if ("N".equalsIgnoreCase(schedule.getIsOpen())) {
            throw new IllegalArgumentException("해당 요일은 휴무일입니다.");
        }

        String time = reserveTime; // "HH:MM" 형식

        // 2) 영업시간 범위 체크 (time < openTime 또는 time >= closeTime 이면 불가)
        if (time.compareTo(schedule.getOpenTime()) < 0 ||
            time.compareTo(schedule.getCloseTime()) >= 0) {
            throw new IllegalArgumentException("영업시간 외에는 예약할 수 없습니다.");
        }

        // 3) 브레이크 타임 체크
        if (schedule.getBreakStart() != null && !schedule.getBreakStart().isBlank()
                && schedule.getBreakEnd() != null && !schedule.getBreakEnd().isBlank()) {

            if (time.compareTo(schedule.getBreakStart()) >= 0 &&
                time.compareTo(schedule.getBreakEnd())   <  0) {
                throw new IllegalArgumentException("브레이크 타임에는 예약할 수 없습니다.");
            }
        }
    }
    
    @Value
    public static class TimeSlotDTO {
        String time;       // "10:00"
        boolean available; // 이 시간에 예약 가능한 테이블 타입이 하나라도 있으면 true
    }

    public List<TimeSlotDTO> getAvailableTimeSlots(Long seqStore,
                                                   String reserveDateStr,
                                                   Integer peopleCount) {

        List<TimeSlotDTO> result = new ArrayList<>();
        
        // 🔹 String → LocalDate 한 번만 변환
        LocalDate reserveDate = LocalDate.parse(reserveDateStr); // "2025-12-10" 형식 기준

        // 00:00 ~ 23:00 한 시간 단위로 돌면서 확인
        for (int h = 0; h < 24; h++) {
            String time = String.format("%02d:00", h);

            // 🔹 여기서 LocalDate 넘김
            List<TableOptionDTO> tables =
                    getAvailableTableTypes(seqStore, reserveDate, time, peopleCount);

            boolean hasAvailable = tables.stream().anyMatch(TableOptionDTO::isAvailable);

            result.add(new TimeSlotDTO(time, hasAvailable));
        }

        return result;
    }
    
}

