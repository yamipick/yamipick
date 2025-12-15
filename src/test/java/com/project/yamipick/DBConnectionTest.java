package com.project.yamipick;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.yamipick.user.repository.UserRepository;

@SpringBootTest
public class DBConnectionTest {
	
	@Autowired
    private UserRepository userRepository;

    @Test
    void testDbConnection() {
        long count = userRepository.count();
        System.out.println("유저 테이블 레코드 수: " + count);
    }

}
