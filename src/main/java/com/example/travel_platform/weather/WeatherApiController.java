package com.example.travel_platform.weather;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherApiController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<Resp<WeatherResponse.WeatherDTO>> getWeather(
            @RequestParam(name = "region") String region,
            @RequestParam(name = "targetDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return Resp.ok(weatherService.getWeather(region, targetDate));
    }
}
