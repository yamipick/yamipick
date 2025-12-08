package com.project.yamipick.waiting.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.waiting.domain.WaitingLog;
import com.project.yamipick.waiting.dto.StoreInfoDTO;
import com.project.yamipick.waiting.dto.StoreScheduleDTO;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.dto.WaitingNoticeDTO;
import com.project.yamipick.waiting.service.WaitingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreWaitingController {

    private final WaitingService waitingService;

    @GetMapping("/waiting/list")
    public List<WaitingDTO> list(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) {
        return waitingService.getStoreList(storeId);
    }

    @GetMapping("/waiting/store-info")
//    public WaitingStore storeInfo(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) { return waitingService.getStoreInfo(storeId); }
    public StoreInfoDTO storeInfo(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) { 
        return waitingService.getStoreInfo(storeId); 
    }

    // ★ 수정: 서비스 메소드 이름(toggleWaitingOpen)과 일치시킴
    @PostMapping("/waiting/toggle")
    public boolean toggle(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) {
        return waitingService.toggleWaitingOpen(storeId);
    }

    @PostMapping("/waiting/call/{id}")
    public String call(@PathVariable("id") Long id) { waitingService.call(id); return "ok"; }

    @PostMapping("/waiting/enter/{id}")
    public String enter(@PathVariable("id") Long id) { waitingService.enter(id); return "ok"; }

    @PostMapping("/waiting/reject/{id}")
    public String reject(@PathVariable("id") Long id) { waitingService.cancel(id, true); return "ok"; }

    @PostMapping("/waiting/notice")
    public String notice(@RequestParam("content") String content) { waitingService.notice(content); return "ok"; }
    
    
 // 공지사항 조회 API
    @GetMapping("/waiting/notices")
    public List<WaitingNoticeDTO> getNotices(@RequestParam(value="storeId", defaultValue="1") Long storeId) {
        return waitingService.getNoticeList(storeId);
    }

    // 공지사항 등록 API
    @PostMapping("/waiting/notice/create")
    public String createNotice(@RequestBody Map<String, Object> payload) {
        //Long storeId = 1L; // 테스트용 고정
        Long storeId = Long.valueOf(String.valueOf(payload.get("storeId")));
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        boolean isPinned = Boolean.TRUE.equals(payload.get("isPinned")); // 체크박스 값
        
        waitingService.createNotice(storeId, title, content, isPinned);
        return "ok";
    }
    
    // 공지사항 수정 API
    @PostMapping("/waiting/notice/update")
    public String updateNotice(@RequestBody Map<String, Object> payload) {
        Long id = Long.valueOf(String.valueOf(payload.get("id")));
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        boolean isPinned = Boolean.TRUE.equals(payload.get("isPinned"));

        waitingService.updateNotice(id, title, content, isPinned);
        return "ok";
    }

    // 공지사항 삭제 API
    @PostMapping("/waiting/notice/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        waitingService.deleteNotice(id);
        return "ok";
    }
    
 // 로그 조회 API
    @GetMapping("/waiting/logs")
    public List<WaitingLog> getLogs(@RequestParam(value="storeId", defaultValue="1") Long storeId) {
        return waitingService.getStoreLogs(storeId);
    }
    
    @GetMapping("/waiting/logs/daily")
    public Map<String, Object> getDailyLogs(
            @RequestParam(value = "storeId", defaultValue = "1") Long storeId,
            @RequestParam(value = "date") String date // yyyy-MM-dd
    ) {
        return waitingService.getDailyReport(storeId, date);
    }
    
    @PostMapping("/waiting/schedule/update")
    public String updateSchedule(@RequestBody StoreScheduleDTO dto) {
        waitingService.updateSchedule(dto);
        return "ok";
    }
    
    @GetMapping("/waiting/schedule/all")
    public List<StoreScheduleDTO> getAllSchedules(@RequestParam("storeId") Long storeId) {
        return waitingService.getAllSchedules(storeId);
    }
    
 // ★ [추가] 특정 요일의 스케줄 상세 조회 (설정창 채우기용)
    @GetMapping("/waiting/schedule/info")
    public ResponseEntity<?> getScheduleInfo(
            @RequestParam("storeId") Long storeId,
            @RequestParam("day") int day // 0:일 ~ 6:토
    ) {
        // 서비스에 메소드 추가가 필요하지만, 간단하게 Repository 직접 호출 혹은 서비스 위임
        // 여기서는 서비스에 위임하는 정석 코드로 작성하겠습니다.
        return ResponseEntity.ok(waitingService.getScheduleInfo(storeId, day));
    }
    
    
    
}