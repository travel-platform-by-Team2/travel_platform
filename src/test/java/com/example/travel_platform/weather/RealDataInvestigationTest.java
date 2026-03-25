package com.example.travel_platform.weather;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class RealDataInvestigationTest {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherRepository weatherRepository;

    @Test
    public void investigateBusanData() {
        System.out.println("=== Investigating Busan Data for 2026-03-24 ===");
        
        // 부산의 좌표 (NX: 98, NY: 76)
        int nx = 98;
        int ny = 76;
        String baseDate = "20260324";
        String baseTime = "0500";
        
        try {
            WeatherRepository.ShortTermForecastRaw rawData = weatherRepository.fetchShortTermForecast(nx, ny, baseDate, baseTime);
            
            System.out.println("Total Items: " + rawData.getItems().size());
            
            // PTY(강수형태) 항목만 필터링하여 출력
            rawData.getItems().stream()
                .filter(item -> "PTY".equals(item.getCategory()))
                .filter(item -> LocalDate.of(2026, 3, 24).equals(item.getForecastDate()))
                .forEach(item -> {
                    System.out.println(String.format("[%s] Category: %s, Time: %s, Value: %s", 
                        item.getForecastDate(), item.getCategory(), item.getForecastTime(), item.getForecastValue()));
                });
                
            // SKY(하늘상태) 항목도 출력
            rawData.getItems().stream()
                .filter(item -> "SKY".equals(item.getCategory()))
                .filter(item -> LocalDate.of(2026, 3, 24).equals(item.getForecastDate()))
                .limit(5)
                .forEach(item -> {
                    System.out.println(String.format("[%s] Category: %s, Time: %s, Value: %s", 
                        item.getForecastDate(), item.getCategory(), item.getForecastTime(), item.getForecastValue()));
                });
        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
