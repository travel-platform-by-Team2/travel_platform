package com.example.travel_platform.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class WeatherServiceTest {

    @Test
    void getWeather_returnsShortTermAndMidTermRangeTogether() {
        WeatherRepository weatherRepository = mock(WeatherRepository.class);
        WeatherService weatherService = new WeatherService(weatherRepository);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        WeatherRepository.ShortTermForecastRaw shortTermForecast =
                WeatherRepository.ShortTermForecastRaw.createShortTermForecast(
                        "20260324",
                        "0500",
                        List.of(
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMN", targetDate, "0600", "9"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMX", targetDate, "1500", "17"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("SKY", targetDate, "0900", "1"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("SKY", targetDate, "1500", "3"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("POP", targetDate, "0900", "20"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("POP", targetDate, "1500", "40"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMN", targetDate.plusDays(1), "0600", "10"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMX", targetDate.plusDays(1), "1500", "18"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("SKY", targetDate.plusDays(1), "0900", "4"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMN", targetDate.plusDays(2), "0600", "11"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("TMX", targetDate.plusDays(2), "1500", "19"),
                                WeatherRepository.ShortTermForecastItem.createShortTermForecastItem("PTY", targetDate.plusDays(2), "1500", "1")));

        WeatherRepository.LandForecastRaw landForecast = WeatherRepository.LandForecastRaw.createLandForecast(Map.of(
                "wf4Am", "맑음",
                "wf4Pm", "구름많음",
                "rnSt4Am", "10",
                "rnSt4Pm", "20"));
        WeatherRepository.TemperatureForecastRaw temperatureForecast =
                WeatherRepository.TemperatureForecastRaw.createTemperatureForecast(Map.of(
                        "taMin4", 12,
                        "taMax4", 19));

        given(weatherRepository.fetchShortTermForecast(eq(52), eq(38), anyString(), anyString()))
                .willReturn(shortTermForecast);
        given(weatherRepository.fetchLandForecast(eq("11G00000"), anyString()))
                .willReturn(landForecast);
        given(weatherRepository.fetchTemperatureForecast(eq("11G00201"), anyString()))
                .willReturn(temperatureForecast);

        WeatherResponse.WeatherDTO response = weatherService.getWeather("jeju", targetDate);

        assertEquals("jeju", response.getRegionCode());
        assertTrue(response.isHasForecasts());
        assertNotNull(response.getTargetForecast());
        assertEquals(targetDate, response.getTargetForecast().getForecastDate());
        assertEquals(4, response.getForecasts().size());
        assertEquals(targetDate.plusDays(3), response.getEndDate());
    }

    @Test
    void getWeather_limitsForecastRangeToAvailableDate() {
        WeatherRepository weatherRepository = mock(WeatherRepository.class);
        WeatherService weatherService = new WeatherService(weatherRepository);

        LocalDate targetDate = LocalDate.now().plusDays(9);

        WeatherRepository.LandForecastRaw landForecast = WeatherRepository.LandForecastRaw.createLandForecast(Map.of(
                "wf9Am", "맑음",
                "wf9Pm", "맑음",
                "wf10Am", "흐림",
                "wf10Pm", "비"));
        WeatherRepository.TemperatureForecastRaw temperatureForecast =
                WeatherRepository.TemperatureForecastRaw.createTemperatureForecast(Map.of(
                        "taMin9", 8,
                        "taMax9", 15,
                        "taMin10", 7,
                        "taMax10", 13));

        given(weatherRepository.fetchLandForecast(eq("11G00000"), anyString()))
                .willReturn(landForecast);
        given(weatherRepository.fetchTemperatureForecast(eq("11G00201"), anyString()))
                .willReturn(temperatureForecast);

        WeatherResponse.WeatherDTO response = weatherService.getWeather("jeju", targetDate);

        assertFalse(response.getForecasts().isEmpty());
        assertEquals(2, response.getForecasts().size());
        assertEquals(targetDate.plusDays(1), response.getEndDate());
    }
}
