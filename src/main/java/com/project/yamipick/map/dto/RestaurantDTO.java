package com.project.yamipick.map.dto;

import java.util.List; // List import 필요
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    
    private String id;
    private String name;
    private String address;
    private String roadAddress;
    private String phone;
    private String x;
    private String y;
    private String url;
    private String category;
    
    private int reviewCount;     
    private double rating;
    private List<String> tags;

    private boolean parking;      // 주차 가능 여부
    private boolean reservable;   // 예약 가능 여부
    private boolean corkageFree;  // 콜키지 프리 여부
    private String priceRange;    // 가격대 (예: "1만원대", "3~5만원")

    
    private boolean isBookmarked;
}