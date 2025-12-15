package com.project.yamipick.reservation.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yamipick.reservation.dto.HolidayDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HolidayService {

    @Value("${holiday.api.service-key}")
    private String serviceKey;

    private static final String API_URL =
            "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<HolidayDTO> getHolidays(int year, int month) {

        List<HolidayDTO> holidays = new ArrayList<>();

        try {
            String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("solYear", year)
                    .queryParam("solMonth", String.format("%02d", month))
                    .queryParam("_type", "json")
                    .build(false)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode items = root
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (items.isMissingNode() || items.isNull()) {
                return holidays;
            }

            if (items.isObject()) {
                holidays.add(convert(items));
            } else if (items.isArray()) {
                for (JsonNode item : items) {
                    holidays.add(convert(item));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return holidays;
    }

    private HolidayDTO convert(JsonNode node) {

        String dateStr = node.path("locdate").asText(); // yyyyMMdd
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);

        HolidayDTO dto = new HolidayDTO();
        dto.setDate(date.toString()); // yyyy-MM-dd
        dto.setName(node.path("dateName").asText());
        return dto;
    }
}
