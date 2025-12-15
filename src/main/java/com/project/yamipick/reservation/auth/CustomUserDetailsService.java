/*
 * package com.project.yamipick.reservation.auth;
 * 
 * import org.springframework.security.core.userdetails.UserDetails; import
 * org.springframework.security.core.userdetails.UserDetailsService; import
 * org.springframework.security.core.userdetails.UsernameNotFoundException;
 * import org.springframework.stereotype.Service;
 * 
 * import com.project.yamipick.user.entity.User; import
 * com.project.yamipick.user.repository.UserRepository;
 * 
 * import lombok.RequiredArgsConstructor;
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class CustomUserDetailsService implements
 * UserDetailsService {
 * 
 * private final UserRepository userRepository;
 * 
 *//**
	 * username == 로그인 폼에서 넘어온 name="username" 값 우리는 이걸 userId로 사용
	 *//*
		 * @Override public UserDetails loadUserByUsername(String username) throws
		 * UsernameNotFoundException {
		 * 
		 * User user = userRepository.findByUserId(username) .orElseThrow(() -> new
		 * UsernameNotFoundException("존재하지 않는 아이디: " + username) );
		 * 
		 * return new CustomUserDetails(user); } }
		 */