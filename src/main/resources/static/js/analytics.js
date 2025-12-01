/**
 * 📊 Yamipick 로그 수집 라이브러리 (Team 2조 전용)
 * * [사용법]
 * 1. HTML 상단에 추가: <script src="/js/analytics.js"></script>
 * 2. 이벤트 발생 시 호출: Analytics.trackClick('지도마커', '강남점', '마커 클릭함');
 */

const Analytics = {
    
    // [핵심] 서버로 로그 전송하는 함수
    track: function(actionType, targetType, targetId, message) {
        
        const logData = {
            actionType: actionType, // 예: CLICK, SEARCH, RESERVE
            targetType: targetType, // 예: MAP_MARKER, BUTTON
            targetId: targetId,     // 예: store_101, 강남
            message: message || ''  // 상세 설명
        };

        // API 호출 (비동기)
        fetch('/api/log/collect', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(logData)
        })
        .then(response => {
            if(response.ok) {
                console.log("✅ 로그 전송 성공:", logData);
            } else {
                console.warn("⚠️ 로그 전송 실패");
            }
        })
        .catch(error => console.error("❌ 로그 에러:", error));
    },

    // --- 아래는 팀원들이 쓰기 편하게 만든 단축 함수들 ---

    /**
     * 클릭 로그 남길 때 사용
     * 예: Analytics.trackClick('예약버튼', 'store_55', '예약하기 누름');
     */
    trackClick: function(elementName, targetId, detail) {
        this.track('CLICK', elementName, targetId, detail);
    },

    /**
     * 검색 로그 남길 때 사용
     * 예: Analytics.trackSearch('파스타');
     */
    trackSearch: function(keyword) {
        this.track('SEARCH', 'KEYWORD', keyword, '사용자 검색: ' + keyword);
    },
    
    /**
     * 페이지 방문(상세보기) 로그
     * 예: Analytics.trackView('STORE_DETAIL', 'store_55');
     */
    trackView: function(pageName, targetId) {
        this.track('VIEW', pageName, targetId, '페이지 조회');
    }
};