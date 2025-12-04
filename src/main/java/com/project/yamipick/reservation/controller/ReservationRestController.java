package com.project.yamipick.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.reservation.dto.ReservationDTO;
import com.project.yamipick.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationRestController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     */
    @PostMapping
    public ResponseEntity<ReservationDTO> create(@RequestBody ReservationDTO dto) {
        ReservationDTO saved = reservationService.createReservation(dto);
        return ResponseEntity.ok(saved);
    }

    /**
     * 특정 유저의 전체 예약 내역
     * GET /api/reservation/users/1
     */
    @GetMapping("/users/{seqUser}")
    public ResponseEntity<List<ReservationDTO>> getUserReservations(@PathVariable Long seqUser) {
        List<ReservationDTO> list = reservationService.getUserReservations(seqUser);
        return ResponseEntity.ok(list);
    }

    /**
     * 특정 매장의 특정 날짜 예약 목록 (전체)
     * GET /api/reservation/stores/6?date=2025-12-04
     */
    @GetMapping("/stores/{seqStore}")
    public ResponseEntity<List<ReservationDTO>> getStoreReservations(
            @PathVariable Long seqStore,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ReservationDTO> list = reservationService.getStoreReservations(seqStore, date);
        return ResponseEntity.ok(list);
    }

    /**
     * 특정 매장 + 테이블 타입 + 특정 날짜 예약 목록
     * GET /api/reservation/stores/6/tables/3?date=2025-12-04
     */
    @GetMapping("/stores/{seqStore}/tables/{seqStoreTable}")
    public ResponseEntity<List<ReservationDTO>> getStoreTableReservations(
            @PathVariable Long seqStore,
            @PathVariable Long seqStoreTable,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ReservationDTO> list =
                reservationService.getStoreTableReservations(seqStore, seqStoreTable, date);
        return ResponseEntity.ok(list);
    }
}
