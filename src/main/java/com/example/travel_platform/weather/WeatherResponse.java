package com.example.travel_platform.weather;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

public class WeatherResponse {

    @Data
    @Builder
    public static class WeatherDTO {
        private String regionCode;
        private String regionLabel;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean hasForecasts;
        private DailyForecastDTO targetForecast;
        private List<DailyForecastDTO> forecasts;

        public static WeatherDTO createWeather(
                WeatherRegion region,
                LocalDate startDate,
                List<DailyForecastDTO> forecasts) {
            DailyForecastDTO targetForecast = forecasts.isEmpty() ? null : forecasts.getFirst();
            LocalDate endDate = forecasts.isEmpty() ? null : forecasts.getLast().getForecastDate();

            return WeatherDTO.builder()
                    .regionCode(region.getCode())
                    .regionLabel(region.getLabel())
                    .startDate(startDate)
                    .endDate(endDate)
                    .hasForecasts(!forecasts.isEmpty())
                    .targetForecast(targetForecast)
                    .forecasts(forecasts)
                    .build();
        }
    }

    @Data
    @Builder
    public static class DailyForecastDTO {
        private LocalDate forecastDate;
        private String dayOfWeek;
        private Integer minTemperature;
        private Integer maxTemperature;
        private String weatherAm;
        private String weatherPm;
        private String weather;
        private Integer rainProbabilityAm;
        private Integer rainProbabilityPm;
        private Integer rainProbability;

        public static DailyForecastDTO createDailyForecast(
                LocalDate forecastDate,
                String dayOfWeek,
                Integer minTemperature,
                Integer maxTemperature,
                String weatherAm,
                String weatherPm,
                String weather,
                Integer rainProbabilityAm,
                Integer rainProbabilityPm,
                Integer rainProbability) {
            return DailyForecastDTO.builder()
                    .forecastDate(forecastDate)
                    .dayOfWeek(dayOfWeek)
                    .minTemperature(minTemperature)
                    .maxTemperature(maxTemperature)
                    .weatherAm(weatherAm)
                    .weatherPm(weatherPm)
                    .weather(weather)
                    .rainProbabilityAm(rainProbabilityAm)
                    .rainProbabilityPm(rainProbabilityPm)
                    .rainProbability(rainProbability)
                    .build();
        }
    }
}
