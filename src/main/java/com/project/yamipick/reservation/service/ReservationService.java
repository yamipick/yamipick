package com.project.yamipick.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.reservation.entity.Reservation;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.reservation.repository.ReservationRepository;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StoreTableTypeRepository storeTableTypeRepository;

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
        
        // TODO: 여기서 나중에
        //  - 매장 영업시간 체크
        //  - 브레이크타임 체크
        //  - 같은 시간대 중복 예약 여부 체크
        //  이런 로직 추가해도 됨

        Reservation reservation = Reservation.builder()
                .reserveDate(dto.getReserveDate())
                .reserveTime(dto.getReserveTime())
                .peopleCount(dto.getPeopleCount())
                .status("대기")  // 기본 상태
                .user(user)
                .store(store)
                .storeTableType(storeTableType)
                .build();

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
}
