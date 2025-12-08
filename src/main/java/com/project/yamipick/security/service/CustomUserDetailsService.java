package com.project.yamipick.security.service;

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
