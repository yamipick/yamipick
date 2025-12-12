/**
 * Yamipick 관리자 페이지 공통 스크립트
 */

// 1. 공통: 삭제 확인 (onsubmit="return confirmDelete()")
function confirmDelete() {
    return confirm('정말 삭제하시겠습니까? 삭제된 데이터는 복구할 수 없습니다.');
}

// 2. 회원 관리: 상태 변경 모달 열기
function openUserStatusModal(seqUser, userId, status) {
    const modal = document.getElementById('statusModal');
    if (!modal) return;

    // input 값 채우기
    document.getElementById('modalSeqUser').value = seqUser;
    document.getElementById('modalUserId').value = userId;
    document.getElementById('modalStatus').value = status;

    // 모달 띄우기 (Bootstrap 5)
    new bootstrap.Modal(modal).show();
}

// 3. 신고 관리: 처리 모달 열기
function openReportProcessModal(seqReport, reason) {
    const modal = document.getElementById('processModal');
    if (!modal) return;

    document.getElementById('modalSeqReport').value = seqReport;
    document.getElementById('modalReason').value = reason;
    
    // 초기화
    const penaltySelect = document.getElementById('modalPenaltyType');
    if (penaltySelect) {
        penaltySelect.value = 'NONE';
        toggleDurationBox(); // 기간 박스 숨기기
    }
    
    new bootstrap.Modal(modal).show();
}

// 4. 신고 관리: 제재 기간 박스 토글
function toggleDurationBox() {
    const penaltyType = document.getElementById('modalPenaltyType').value;
    const durationBox = document.getElementById('penaltyDurationBox');
    
    if (durationBox) {
        if (penaltyType === 'BLIND_USER') {
            durationBox.style.display = 'block';
        } else {
            durationBox.style.display = 'none';
        }
    }
}