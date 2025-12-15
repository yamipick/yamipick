package com.project.yamipick.waiting.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.user.repository.UserRepository;
import com.project.yamipick.waiting.domain.WaitingLog;
import com.project.yamipick.waiting.dto.WaitingSessionUserDTO;
import com.project.yamipick.waiting.dto.StoreInfoDTO;
import com.project.yamipick.waiting.dto.StoreScheduleDTO;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.dto.WaitingNoticeDTO;
import com.project.yamipick.waiting.repository.WaitingStoreRepository;
import com.project.yamipick.waiting.service.WaitingService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreWaitingController {

    private final WaitingService waitingService;
    private final UserRepository userRepository;
    private final WaitingStoreRepository storeRepository;

private Long getStoreId(HttpSession session, Authentication auth) {
        
        // 1. 세션에서 우리가 넣어둔 통합 DTO를 꺼냅니다.
        WaitingSessionUserDTO dto = (WaitingSessionUserDTO) session.getAttribute("waitingSession");
        
        // 2. DTO가 있고 + 가게가 있는 사장님이라면? -> 바로 ID 반환 (DB 조회 X)
        if (dto != null && dto.isHasStore()) {
            return dto.getSeqStore();
        }

        // 3. (안전장치) 세션이 만료됐거나 꼬였을 경우를 대비해 기존 로직 유지
        //    팀원이 다른 로그인 방식을 썼을 때를 대비한 방어 코드입니다.
        if (auth != null && auth.isAuthenticated()) {
            // ... (기존 DB 조회 로직: User 찾고 Store 찾기) ...
            // 여기는 기존 코드를 비상용으로 남겨두셔도 됩니다.
        }

        return 1L; // 혹은 예외 발생
    }

    @GetMapping("/waiting/list")
    public List<WaitingDTO> list(HttpSession session, Authentication auth) {
        Long storeId = getStoreId(session, auth);
        return waitingService.getStoreList(storeId);
    }

    @GetMapping("/waiting/store-info")
    public StoreInfoDTO storeInfo(
            @RequestParam(value = "storeId", required = false) Long storeIdParam,
            HttpSession session, 
            Authentication auth
    ) {
        Long targetId;

        // 1. URL 파라미터로 storeId가 왔다면 그걸 씁니다. (손님 화면용)
        if (storeIdParam != null) {
            targetId = storeIdParam;
        } 
        // 2. 파라미터가 없다면 로그인한 점주의 가게를 찾습니다. (점주 화면용)
        else {
            targetId = getStoreId(session, auth);
        }

        return waitingService.getStoreInfo(targetId);
    }

    @PostMapping("/waiting/toggle")
    public boolean toggle(HttpSession session, Authentication auth) {
        Long storeId = getStoreId(session, auth);
        return waitingService.toggleWaitingOpen(storeId);
    }

    @PostMapping("/waiting/call/{id}")
    public String call(@PathVariable("id") Long id) {
        waitingService.call(id);
        return "ok";
    }

    @PostMapping("/waiting/enter/{id}")
    public String enter(@PathVariable("id") Long id) {
        waitingService.enter(id);
        return "ok";
    }

    @PostMapping("/waiting/reject/{id}")
    public String reject(@PathVariable("id") Long id) {
        waitingService.cancel(id, true);
        return "ok";
    }

    @PostMapping("/waiting/notice")
    public String notice(@RequestParam("content") String content) {
        waitingService.notice(content);
        return "ok";
    }

    @GetMapping("/waiting/notices")
    public List<WaitingNoticeDTO> getNotices(
            @RequestParam(value = "storeId", required = false) Long storeIdParam,
            HttpSession session, 
            Authentication auth
    ) {
        Long targetId;

        // 1. 손님 화면에서 요청 시 (URL 파라미터 사용)
        if (storeIdParam != null) {
            targetId = storeIdParam;
        } 
        // 2. 점주 화면에서 요청 시 (내 가게 찾기)
        else {
            targetId = getStoreId(session, auth);
        }

        return waitingService.getNoticeList(targetId);
    }

    @PostMapping("/waiting/notice/create")
    public String createNotice(@RequestBody Map<String, Object> payload, 
                               HttpSession session, Authentication auth) {
        Long storeId = getStoreId(session, auth);
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        boolean isPinned = Boolean.TRUE.equals(payload.get("isPinned"));
        waitingService.createNotice(storeId, title, content, isPinned);
        return "ok";
    }

    @PostMapping("/waiting/notice/update")
    public String updateNotice(@RequestBody Map<String, Object> payload) {
        Long id = Long.valueOf(String.valueOf(payload.get("id")));
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        boolean isPinned = Boolean.TRUE.equals(payload.get("isPinned"));
        waitingService.updateNotice(id, title, content, isPinned);
        return "ok";
    }

    @PostMapping("/waiting/notice/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        waitingService.deleteNotice(id);
        return "ok";
    }

    @GetMapping("/waiting/logs")
    public List<WaitingLog> getLogs(HttpSession session, Authentication auth) {
        Long storeId = getStoreId(session, auth);
        return waitingService.getStoreLogs(storeId);
    }

    @GetMapping("/waiting/logs/daily")
    public Map<String, Object> getDailyLogs(
            @RequestParam(value = "date") String date,
            HttpSession session, Authentication auth
    ) {
        Long storeId = getStoreId(session, auth);
        return waitingService.getDailyReport(storeId, date);
    }

    @PostMapping("/waiting/schedule/update")
    public String updateSchedule(@RequestBody StoreScheduleDTO dto,
                                  HttpSession session, Authentication auth) {
        Long storeId = getStoreId(session, auth);
        dto.setStoreId(storeId);
        waitingService.updateSchedule(dto);
        return "ok";
    }

    @GetMapping("/waiting/schedule/all")
    public List<StoreScheduleDTO> getAllSchedules(
            @RequestParam(value = "storeId", required = false) Long storeIdParam,
            HttpSession session, Authentication auth
    ) {
        Long targetId = (storeIdParam != null) ? storeIdParam : getStoreId(session, auth);
        return waitingService.getAllSchedules(targetId);
    }

    @GetMapping("/waiting/schedule/info")
    public ResponseEntity<StoreScheduleDTO> getScheduleInfo(
            @RequestParam(value = "storeId", required = false) Long storeIdParam,
            @RequestParam("day") int day,
            HttpSession session, Authentication auth
    ) {
        Long targetId = (storeIdParam != null) ? storeIdParam : getStoreId(session, auth);
        return ResponseEntity.ok(waitingService.getScheduleInfo(targetId, day));
    }
}