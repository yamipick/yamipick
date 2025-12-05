package com.project.yamipick;

import java.util.TimeZone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.project.yamipick.reservation.entity.StoreSchedule;
import com.project.yamipick.reservation.entity.StoreTableType;
import com.project.yamipick.reservation.repository.StoreScheduleRepository;
import com.project.yamipick.reservation.repository.StoreTableTypeRepository;
import com.project.yamipick.store.entity.Store;
import com.project.yamipick.store.repository.StoreRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class YamipickApplicationReservation {

    public static void main(String[] args) {
        SpringApplication.run(YamipickApplication.class, args);
    }

    // ★ 이 부분이 핵심! (서버 켜질 때 딱 한 번 실행됨)
    @PostConstruct
    public void init() {
        // 서버의 시간대를 '아시아/서울'로 강제 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("⏰ 서버 시간이 KST(Asia/Seoul)로 설정되었습니다.");
    }
    
    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            StoreRepository storeRepository,
            StoreTableTypeRepository tableTypeRepository,
            StoreScheduleRepository scheduleRepository
    ) {
        return args -> {

	        // 1) 기존 유저 가져오기 (예: seqUser = 1번)
	        User user = userRepository.findById(8L)
	                .orElseThrow(() -> new IllegalArgumentException("기존 매장유저가 없습니다."));
	
	        // 또는 id 기준으로 가져오고 싶으면:
	        // User user = userRepository.findById("owner1") ... 이런 식으로 (레포지토리에 메서드 추가 필요)
	
	        // 2) 매장
	        Store store = storeRepository.findById(6L)
                .orElseThrow(() -> new IllegalArgumentException("기존 매장이 없습니다."));

            // 3) 테이블 타입 (창가 2인석 5개)
            StoreTableType tableType = StoreTableType.builder()
                    .name("바 1인석")
                    .capacity(1)
                    .quantity(9)
                    .store(store)            // ★ FK 연관
                    .build();
            tableTypeRepository.save(tableType);

            // 4) 요일별 스케줄 (월요일, 09~21시, 브레이크 15~17)
//            StoreSchedule schedule = StoreSchedule.builder()
//                    .dayOfWeek(1)           // 월요일
//                    .isOpen("Y")
//                    .openTime("09:00")
//                    .closeTime("21:00")
//                    .breakStart("15:00")
//                    .breakEnd("17:00")
//                    .store(store)           // ★ FK 연관
//                    .build();
//            scheduleRepository.save(schedule);

            System.out.println("==== initData 완료 ====");
        };
    }
}