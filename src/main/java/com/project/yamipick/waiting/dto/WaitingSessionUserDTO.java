package com.project.yamipick.waiting.dto;

import com.project.yamipick.user.entity.User;
import com.project.yamipick.waiting.domain.WaitingStore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter
@ToString
@NoArgsConstructor
public class WaitingSessionUserDTO {

    // [User 정보]
    private Long seqUser;       // DB PK
    private String userId;      // 로그인 ID
    private String name;        // 실명
    private String nickname;    // 닉네임
    private String email;       // 이메일
    private String phone;       // 전화번호
    private String role;        // 권한

    // [Store 정보] (점주가 아니면 null)
    private Long seqStore;      // 매장 PK
    private String storeName;   // 매장 이름
    private String storePhone;  // 매장 전화번호
    private boolean hasStore;   // 매장 보유 여부

    // ✅ User 기반 생성자
    public WaitingSessionUserDTO(User user, WaitingStore store) {
        this.seqUser = user.getSeqUser();
        this.userId = user.getUserId();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();

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