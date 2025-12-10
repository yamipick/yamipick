package com.project.yamipick.login.dto;

import com.project.yamipick.waiting.domain.WaitingMember;
import com.project.yamipick.waiting.domain.WaitingStore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@NoArgsConstructor
public class SessionUserDTO {
    // [User 정보]
    private Long seqUser;       // DB PK
    private String loginId;     // 로그인 ID
    private String name;        // 실명
    private String nickname;    // 닉네임 (화면 표시용)
    private String email;       // 이메일
    private String phone;       // 전화번호
    private String role;        // 권한 (USER, STORE, ADMIN)

    // [Store 정보] (점주가 아니면 null)
    private Long seqStore;      // 매장 PK
    private String storeName;   // 매장 이름
    private String storePhone;  // 매장 전화번호
    private boolean hasStore;   // 매장 보유 여부 (편의성 필드)

    // 엔티티 -> DTO 변환 (생성자에서 처리)
    public SessionUserDTO(WaitingMember member, WaitingStore store) {
        // 회원 정보 매핑
        this.seqUser = member.getId();
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.phone = member.getPhoneNumber();
        this.role = member.getRole();

        // 매장 정보 매핑 (있을 경우에만)
        if (store != null) {
            this.seqStore = store.getId();
            this.storeName = store.getName();
            this.storePhone = store.getPhone();
            this.hasStore = true;
        } else {
            this.hasStore = false;
        }
    }
}