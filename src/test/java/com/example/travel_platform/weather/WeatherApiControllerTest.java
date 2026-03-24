package com.example.travel_platform.weather;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.travel_platform._core.handler.ApiExceptionHandler;

class WeatherApiControllerTest {

    private MockMvc mockMvc;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = mock(WeatherService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WeatherApiController(weatherService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void getWeather_returnsForecastRangeBody() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 3, 25);
        WeatherResponse.DailyForecastDTO firstForecast = WeatherResponse.DailyForecastDTO.createDailyForecast(
                targetDate,
                "수",
                7,
                15,
                "맑음",
                "구름많음",
                "맑음 / 구름많음",
                10,
                30,
                30);
        WeatherResponse.DailyForecastDTO secondForecast = WeatherResponse.DailyForecastDTO.createDailyForecast(
                targetDate.plusDays(1),
                "목",
                8,
                16,
                "흐림",
                "비",
                "흐림 / 비",
                40,
                60,
                60);

        WeatherResponse.WeatherDTO response = WeatherResponse.WeatherDTO.createWeather(
                WeatherRegion.JEJU,
                targetDate,
                List.of(firstForecast, secondForecast));

        given(weatherService.getWeather(eq("jeju"), eq(targetDate))).willReturn(response);

        mockMvc.perform(get("/api/weather")
                .param("region", "jeju")
                .param("targetDate", "2026-03-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.regionCode").value("jeju"))
                .andExpect(jsonPath("$.body.hasForecasts").value(true))
                .andExpect(jsonPath("$.body.targetForecast.forecastDate").value("2026-03-25"))
                .andExpect(jsonPath("$.body.forecasts.length()").value(2));
    }
}
