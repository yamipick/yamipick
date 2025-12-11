package com.project.yamipick.security.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		log.info("로그인 시도 아이디 {} ", username );
		
		// 1. DB에서 아이디로 회원 찾기
		User userData = userRepository.findByUserId(username)
				.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다." + username));
		
		// 1) 정지 회원(SUSPENDED) 체크
		if ("SUSPENDED".equals(userData.getStatusUser())) {
			
			// 정지 해제일이 아직 안 지났는지 확인 (현재 시간보다 미래인지)
			if (userData.getSuspendedUntil() != null && userData.getSuspendedUntil().isAfter(LocalDateTime.now())) {
				
				// 정지 상태임 -> 로그인 차단 (LockedException 발생)
				throw new LockedException("정지된 계정입니다. (해제일: " + userData.getSuspendedUntil().toLocalDate() + ")");
				
			} else {
				// 기간이 지났음 -> 자동으로 풀어주고 로그인 통과! (Auto Reset)
				log.info("정지 기간 만료됨. 계정 상태를 ACTIVE로 복구합니다. User: {}", username);
				userData.changeStatus("ACTIVE", null);
				userRepository.save(userData); // DB에 변경사항 저장
			}
		}

        // 2) 탈퇴 회원(WITHDRAWN) 체크
        if ("WITHDRAWN".equals(userData.getStatusUser())) {
            throw new org.springframework.security.authentication.DisabledException("탈퇴한 계정입니다.");
        }
		
		log.info("✅ 회원 확인됨: {} (권한: {})", userData.getUserId(), userData.getRole());
		
		// 2. 시큐리티한테 사람 확인하고 데이터 넘겨주기
		// User엔티티를 import하고 있기 때문에 eclipse 헷갈리지 말라고 풀네임으로 user부르기
		return org.springframework.security.core.userdetails.User.builder()
				.username(userData.getUserId())
				.password(userData.getPassword())
				.roles(userData.getRole().replace("ROLE_", ""))
				.build();
	}
	
	
	
}