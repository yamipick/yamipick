package com.project.yamipick.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // ========================================================================
    // 1. 예약 생성/조회
    // ========================================================================

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

        LocalDate reserveDate = dto.getReserveDate();
        String reserveTime = dto.getReserveTime();
        int people = dto.getPeopleCount();

        // 🔒 과거 날짜 예약 막기
        if (reserveDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("이미 지난 날짜로는 예약할 수 없습니다.");
        }
        
        // 🔒 오늘 예약이면 "현재 시간 + 2시간 이후"만 허용
        if (reserveDate.isEqual(LocalDate.now())) {
            LocalTime nowTime = LocalTime.now();
            LocalTime reserveLocalTime = LocalTime.parse(reserveTime); // "HH:mm" 형식 가정

            if (!reserveLocalTime.isAfter(nowTime.plusHours(2))) {
                throw new IllegalArgumentException("현재 시간으로부터 2시간 이후만 예약 가능합니다.");
            }
        }

        // 🔒 예약 시간 필수 체크
        if (reserveTime == null || reserveTime.isBlank()) {
            throw new IllegalArgumentException("예약 시간을 선택해 주세요.");
        }

        // 🔒 1단계: 인원수 vs 테이블용량 체크 (tableSelect에서 한 번 걸렀지만 백엔드에서도 한 번 더)
        Integer capacity = storeTableType.getCapacity();
        Integer quantity = storeTableType.getQuantity();

        if (capacity == null || quantity == null || capacity <= 0 || quantity <= 0) {
            throw new IllegalStateException("테이블 타입 설정이 올바르지 않습니다.");
        }

        int neededTables = (people + capacity - 1) / capacity;
        if (neededTables > quantity) {
            throw new IllegalArgumentException("해당 테이블 타입으로는 인원을 수용할 수 없습니다.");
        }

        // 🔒 2단계: 영업시간 / 휴무일 / 브레이크 타임 체크
        validateStoreSchedule(store, reserveDate, reserveTime);

        // 🔒 3단계: (선택) 이미 같은 시간/테이블 타입에 만석인지 최종 체크
        validateCapacityWithExistingReservations(store, storeTableType, reserveDate, reserveTime, people);

        // 엔티티 생성
        Reservation reservation = Reservation.builder()
                .reserveDate(reserveDate)
                .reserveTime(reserveTime)
                .peopleCount(people)
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

        markExpired(list);
        
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

        markExpired(list);
        
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

    // 예약 상세보기
    public ReservationDTO getReservationDetail(Long seqReservation) {
        Reservation r = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        markExpired(java.util.List.of(r));
        
        return toDTO(r);
    }

    // 예약 취소용
    public void cancelReservation(Long seqReservation, String reason) {
        Reservation reservation = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        // 필요하면 상태 체크
        if (!"대기".equals(reservation.getStatus())) {
            throw new IllegalStateException("대기 상태의 예약만 취소할 수 있습니다.");
        }

        reservation.cancelByUser(reason);
        // JPA 더티 체킹으로 자동 업데이트됨 (save() 안 해도 됨)
    }
    
    //예약 알림
    public List<ReservationDTO> getUserNotificationReservations(Long seqUser) {

        // 최근 예약 순으로 다 가져온 뒤,
        // 상태가 '대기'가 아닌 것만 골라서 상위 5개 정도만 노출
        return reservationRepository
                .findByUser_SeqUserOrderByReserveDateDescReserveTimeDesc(seqUser)
                .stream()
                .filter(r -> !"대기".equals(r.getStatus()))   // 대기는 알림에서 제외
                .limit(5)                                     // 필요하면 개수 조절
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 2. 테이블 타입 / 시간대 조회 (예약 가능 여부 계산)
    // ========================================================================

    @Value
    public static class TableOptionDTO {
        Long seqStoreTable;
        String name;
        boolean available;   // 나중에 만석 처리할 때 false로 줄 수 있음
    }

    /**
     * 특정 매장 + 날짜 + 시간 + 인원 기준으로
     * 각 테이블 타입이 예약 가능한지 여부 계산
     */
    public List<TableOptionDTO> getAvailableTableTypes(Long seqStore,
                                                       LocalDate reserveDate,
                                                       String reserveTime,
                                                       Integer peopleCount) {

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

                    // 이미 이 시간대에 예약된 same 타입 예약들 가져오기
                    List<Reservation> reservedList =
                            reservationRepository
                                    .findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDateAndReserveTime(
                                            seqStore,
                                            t.getSeqStoreTable(),
                                            reserveDate,
                                            reserveTime
                                    );

                    // 이미 사용 중인 테이블 개수 계산
                    int usedTables = reservedList.stream()
                            .mapToInt(r -> {
                                int pc = r.getPeopleCount();
                                // ceil(pc / capacity)
                                return (pc + capacity - 1) / capacity;
                            })
                            .sum();

                    int remainTables = quantity - usedTables;

                    // 이번 예약에 필요한 테이블 수
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

    @Value
    public static class TimeSlotDTO {
        String time;       // "10:00"
        boolean available; // 이 시간에 예약 가능한 테이블 타입이 하나라도 있으면 true
    }

    /**
     * 날짜/인원 기준으로
     * 영업시간 + 브레이크타임을 반영한 "예약 가능 시간대" 리스트
     */
    public List<TimeSlotDTO> getAvailableTimeSlots(Long seqStore,
                                                   String reserveDateStr,
                                                   Integer peopleCount) {

        if (reserveDateStr == null || reserveDateStr.isBlank()) {
            throw new IllegalArgumentException("예약 날짜가 필요합니다.");
        }
        if (peopleCount == null || peopleCount <= 0) {
            throw new IllegalArgumentException("인원 수는 1명 이상이어야 합니다.");
        }

        // "yyyy-MM-dd" 형식 가정
        LocalDate reserveDate = LocalDate.parse(reserveDateStr);
        
        // 오늘/현재 시각
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        // 현재 시각 + 2시간
        LocalDateTime threshold = now.plusHours(2);

        // 1) 매장 한 번 조회
        Store store = storeRepository.findById(seqStore)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + seqStore));

        // 2) 스케줄 한 번 조회 (휴무/영업시간/브레이크)
        StoreSchedule schedule = getSchedule(store, reserveDate);

        // 2-1) 휴무일이면 그냥 빈 리스트 반환 → 프론트에서 "휴무일입니다" 띄우면 됨
        if ("N".equalsIgnoreCase(schedule.getIsOpen())) {
            return List.of();
        }

        String openTime   = schedule.getOpenTime();   // 예: "10:00"
        String closeTime  = schedule.getCloseTime();  // 예: "22:00"
        String breakStart = schedule.getBreakStart(); // 예: "15:00" (없으면 null/빈문자)
        String breakEnd   = schedule.getBreakEnd();   // 예: "17:00"

        // 3) 이 매장의 테이블 타입 한 번만 조회
        List<StoreTableType> tableTypes = storeTableTypeRepository.findByStore(store);

        // 4) 이 날짜의 전체 예약 한 번만 조회
        List<Reservation> reservationsOfDay =
                reservationRepository.findByStore_SeqStoreAndReserveDate(seqStore, reserveDate);

        // 4-1) 시간별로 그룹핑 (reserveTime 기준: "HH:MM")
        var reservationsByTime = reservationsOfDay.stream()
                .collect(Collectors.groupingBy(Reservation::getReserveTime));

        List<TimeSlotDTO> result = new ArrayList<>();

        // 5) 00:00 ~ 23:00 돌면서
        //    - 영업시간 밖이면 제외
        //    - 브레이크 타임이면 제외
        //    - 남은 테이블 있는 시간만 available = true
        for (int h = 0; h < 24; h++) {
            String time = String.format("%02d:00", h); // "00:00" ~ "23:00"

            // 5-1) 영업시간 범위 체크
            if (time.compareTo(openTime) < 0 || time.compareTo(closeTime) >= 0) {
                continue; // 영업 시작 전/종료 후 → 시간 옵션 자체를 안 보냄
            }

            // 5-2) 브레이크 타임이면 건너뜀
            if (breakStart != null && !breakStart.isBlank()
                    && breakEnd != null && !breakEnd.isBlank()) {

                if (time.compareTo(breakStart) >= 0 &&
                    time.compareTo(breakEnd)   <  0) {
                    continue;
                }
            }
            
            // 5-3) 오늘 날짜면 "현재 시간 + 2시간 이후"만 허용
            if (reserveDate.isEqual(today)) {
                // time은 "HH:00" 형식
                LocalTime slotTime = LocalTime.parse(time); // 예: "15:00"
                LocalDateTime slotDateTime = LocalDateTime.of(reserveDate, slotTime);

                // slotDateTime 이 threshold 이후가 아니면 (동일 or 이전이면) 스킵
                if (!slotDateTime.isAfter(threshold)) {
                    continue;
                }
            }

            // 이 시간대의 예약들만
            List<Reservation> listForTime =
                    reservationsByTime.getOrDefault(time, List.of());

            boolean hasAvailableType = false;

            // 6) 테이블 타입들 중 하나라도 수용 가능하면 이 시간은 available = true
            for (StoreTableType t : tableTypes) {
                int capacity = t.getCapacity(); // 테이블당 인원
                int quantity = t.getQuantity(); // 테이블 개수

                if (capacity <= 0 || quantity <= 0) {
                    continue;
                }

                // 이번 예약이 이 테이블 타입을 쓴다면 필요한 테이블 수
                int needTables = (peopleCount + capacity - 1) / capacity; // ceil

                // 이미 이 시간대 + 이 테이블 타입으로 잡힌 예약들
                int usedTables = listForTime.stream()
                        .filter(r -> r.getStoreTableType().getSeqStoreTable()
                                .equals(t.getSeqStoreTable()))
                        .mapToInt(r -> {
                            int pc = r.getPeopleCount();
                            return (pc + capacity - 1) / capacity; // 예약마다 필요한 테이블 수
                        })
                        .sum();

                int remainTables = quantity - usedTables;

                if (remainTables >= needTables) {
                    hasAvailableType = true;
                    break; // 이 시간대는 "예약 가능" 확정
                }
            }

            result.add(new TimeSlotDTO(time, hasAvailableType));
        }

        return result;
    }

    // ========================================================================
    // 3. 공통 유틸 메서드
    // ========================================================================

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
                .cancelReason(r.getCancelReason())
                .seqUser(r.getUser().getSeqUser())
                .seqStore(r.getStore().getSeqStore())
                .seqStoreTable(r.getStoreTableType().getSeqStoreTable())
                .storeName(r.getStore().getName())
                .tableTypeName(r.getStoreTableType().getName())
                .nickname(r.getUser().getNickname())
                .build();
    }

    /**
     * 요일 인덱스 계산 (0=일, 1=월 ... 6=토)
     */
    private int getDayOfWeekIndex(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY    -> 1;
            case TUESDAY   -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY  -> 4;
            case FRIDAY    -> 5;
            case SATURDAY  -> 6;
            case SUNDAY    -> 0;
        };
    }

    /**
     * 날짜 기준 매장 스케줄 조회 (휴무/영업시간/브레이크 타임용)
     */
    private StoreSchedule getSchedule(Store store, LocalDate reserveDate) {
        int dayOfWeek = getDayOfWeekIndex(reserveDate);
        return storeScheduleRepository
                .findByStoreAndDayOfWeek(store, dayOfWeek)
                .orElseThrow(() -> new IllegalStateException("해당 요일 스케줄이 설정되어 있지 않습니다."));
    }

    /**
     * 영업시간 / 휴무일 / 브레이크 타임 체크
     * (createReservation에서 사용)
     */
    private void validateStoreSchedule(Store store, LocalDate reserveDate, String reserveTime) {

        StoreSchedule schedule = getSchedule(store, reserveDate);

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

    /**
     * 이미 같은 시간대/테이블 타입에 잡힌 예약들을 기준으로
     * 실제로도 자리가 남는지 최종 확인 (동시성 대비용)
     */
    private void validateCapacityWithExistingReservations(Store store,
                                                          StoreTableType tableType,
                                                          LocalDate reserveDate,
                                                          String reserveTime,
                                                          int people) {

        int capacity = tableType.getCapacity();
        int quantity = tableType.getQuantity();

        if (capacity <= 0 || quantity <= 0) {
            throw new IllegalStateException("테이블 타입 설정이 올바르지 않습니다.");
        }

        int needTables = (people + capacity - 1) / capacity;

        // 해당 매장 + 테이블 타입 + 날짜 + 시간 기준 예약들 조회
        List<Reservation> reservedList =
                reservationRepository
                        .findByStore_SeqStoreAndStoreTableType_SeqStoreTableAndReserveDateAndReserveTime(
                                store.getSeqStore(),
                                tableType.getSeqStoreTable(),
                                reserveDate,
                                reserveTime
                        );

        int usedTables = reservedList.stream()
                .mapToInt(r -> {
                    int pc = r.getPeopleCount();
                    return (pc + capacity - 1) / capacity;
                })
                .sum();

        int remainTables = quantity - usedTables;

        if (remainTables < needTables) {
            throw new IllegalArgumentException("이미 해당 시간대의 테이블이 모두 예약되었습니다.");
        }
    }
    
 // 한달치 휴무일 리스트 (yyyy-MM-dd 문자열)
    public List<String> getHolidayDatesForMonth(Long seqStore, int year, int month) {

        // 1) 매장 찾기
        Store store = storeRepository.findById(seqStore)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다. seqStore=" + seqStore));

        // 2) 이 매장의 "휴무인 요일"들 가져오기 (isOpen = 'N')
        // ★ StoreScheduleRepository에 findByStoreAndIsOpenIgnoreCase(Store store, String isOpen) 필요
        List<StoreSchedule> holidaySchedules =
                storeScheduleRepository.findByStoreAndIsOpenIgnoreCase(store, "N");

        if (holidaySchedules.isEmpty()) {
            // 휴무 요일 설정이 하나도 없으면 그냥 빈 리스트
            return List.of();
        }

        // 휴무 요일 번호만 뽑기 (0=일, 1=월 ... 6=토)
        List<Integer> holidayDaysOfWeek = holidaySchedules.stream()
                .map(StoreSchedule::getDayOfWeek)
                .toList();

        // 3) year, month에 해당하는 날짜들 중에서
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDay.lengthOfMonth();

        List<String> result = new ArrayList<>();

        for (int d = 1; d <= lengthOfMonth; d++) {
            LocalDate date = firstDay.withDayOfMonth(d);

            int dow; // 0=일, 1=월 ... 6=토
            switch (date.getDayOfWeek()) {
                case MONDAY    -> dow = 1;
                case TUESDAY   -> dow = 2;
                case WEDNESDAY -> dow = 3;
                case THURSDAY  -> dow = 4;
                case FRIDAY    -> dow = 5;
                case SATURDAY  -> dow = 6;
                case SUNDAY    -> dow = 0;
                default        -> dow = 0;
            }

            if (holidayDaysOfWeek.contains(dow)) {
                // "yyyy-MM-dd" 형식
                result.add(date.toString());
            }
        }

        return result;
    }
    
    // 🔹 대기 상태인데 시간이 지난 예약은 '예약만료'로 변경
    private void markExpired(List<Reservation> reservations) {
        LocalDateTime now = LocalDateTime.now();

        for (Reservation r : reservations) {

            // 대기 상태만 대상
            if (!"대기".equals(r.getStatus())) {
                continue;
            }

            // 예약일 + 예약시간
            LocalDate date = r.getReserveDate();
            String timeStr = r.getReserveTime(); // "HH:mm" 형식 가정
            LocalTime time = LocalTime.parse(timeStr);

            LocalDateTime reservedDateTime = LocalDateTime.of(date, time);

            // 현재 시간보다 이미 지났으면 예약만료로 변경
            if (reservedDateTime.isBefore(now)) {
                r.changeStatus("만료");
            }
        }
    }
    
    //예약 알림 메서드
    public long countWaitingReservationsForStore(Long seqStore) {
        return reservationRepository.countByStore_SeqStoreAndStatus(seqStore, "대기");
    }
    
 // ========================================================================
    // 4. 매장용 예약 상태 변경 (확정 / 취소 + 사유)
    // ========================================================================

    /**
     * 매장이 예약 확정
     */
    public void confirmReservation(Long seqReservation) {
        Reservation reservation = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        // 대기 상태인 예약만 확정 가능하게 하고 싶다면:
        if (!"대기".equals(reservation.getStatus())) {
            throw new IllegalStateException("대기 상태의 예약만 확정할 수 있습니다.");
        }

        reservation.confirm(); // 엔티티 메서드 호출
        // @Transactional + 더티체킹으로 자동 업데이트
    }

    /**
     * 매장이 예약 취소 + 취소 사유 남기기
     */
    public void storeCancelReservation(Long seqReservation, String reason) {
        Reservation reservation = reservationRepository.findById(seqReservation)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. seqReservation=" + seqReservation));

        if (!"대기".equals(reservation.getStatus())) {
            throw new IllegalStateException("대기 상태의 예약만 취소할 수 있습니다.");
        }

        reservation.cancelByStore(reason);
    }
    
}
