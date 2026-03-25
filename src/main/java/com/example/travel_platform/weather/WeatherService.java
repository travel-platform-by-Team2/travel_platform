package com.example.travel_platform.weather;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherService {

    private static final DateTimeFormatter MID_TERM_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter SHORT_TERM_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter SHORT_TERM_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private static final List<LocalTime> SHORT_TERM_BASE_TIMES = List.of(
            LocalTime.of(2, 0),
            LocalTime.of(5, 0),
            LocalTime.of(8, 0),
            LocalTime.of(11, 0),
            LocalTime.of(14, 0),
            LocalTime.of(17, 0),
            LocalTime.of(20, 0),
            LocalTime.of(23, 0));

    private final WeatherRepository weatherRepository;

    public WeatherResponse.WeatherDTO getWeather(String regionInput, LocalDate targetDate) {
        WeatherRegion region = resolveRegion(regionInput);
        validateTargetDate(targetDate);

        LocalDate today = LocalDate.now();
        LocalDate endDate = targetDate.plusDays(3);
        LocalDate maxDate = today.plusDays(10);
        if (endDate.isAfter(maxDate)) {
            endDate = maxDate;
        }

        Map<LocalDate, WeatherResponse.DailyForecastDTO> forecastMap = new LinkedHashMap<>();
        mergeShortTermForecasts(region, targetDate, endDate, forecastMap);
        mergeMidTermForecasts(region, targetDate, endDate, forecastMap);

        List<WeatherResponse.DailyForecastDTO> forecasts = new ArrayList<>();
        for (LocalDate date = targetDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            WeatherResponse.DailyForecastDTO forecast = forecastMap.get(date);
            if (forecast != null) {
                forecasts.add(forecast);
            }
        }

        if (forecasts.isEmpty()) {
            throw new Exception400("조회 가능한 날짜의 날씨 예보가 없습니다.");
        }

        return WeatherResponse.WeatherDTO.createWeather(region, targetDate, forecasts);
    }

    private void mergeShortTermForecasts(
            WeatherRegion region,
            LocalDate startDate,
            LocalDate endDate,
            Map<LocalDate, WeatherResponse.DailyForecastDTO> forecastMap) {
        LocalDate today = LocalDate.now();
        LocalDate shortTermEndDate = today.plusDays(3);
        LocalDate actualStart = startDate.isBefore(today) ? today : startDate;
        LocalDate actualEnd = endDate.isAfter(shortTermEndDate) ? shortTermEndDate : endDate;

        if (actualStart.isAfter(actualEnd)) {
            return;
        }

        LocalDateTime baseDateTime = resolveShortTermBaseDateTime(LocalDateTime.now());
        WeatherRepository.ShortTermForecastRaw shortTermForecast = weatherRepository.fetchShortTermForecast(
                region.getShortTermNx(),
                region.getShortTermNy(),
                baseDateTime.toLocalDate().format(SHORT_TERM_DATE_FORMAT),
                baseDateTime.toLocalTime().format(SHORT_TERM_TIME_FORMAT));

        for (LocalDate date = actualStart; !date.isAfter(actualEnd); date = date.plusDays(1)) {
            WeatherResponse.DailyForecastDTO forecast = createShortTermDailyForecast(date, shortTermForecast);
            if (forecast != null) {
                forecastMap.put(date, forecast);
            }
        }
    }

    private void mergeMidTermForecasts(
            WeatherRegion region,
            LocalDate startDate,
            LocalDate endDate,
            Map<LocalDate, WeatherResponse.DailyForecastDTO> forecastMap) {
        LocalDate today = LocalDate.now();
        LocalDate midTermStartDate = today.plusDays(4);
        LocalDate actualStart = startDate.isBefore(midTermStartDate) ? midTermStartDate : startDate;

        if (actualStart.isAfter(endDate)) {
            return;
        }

        LocalDateTime announcementTime = resolveMidTermAnnouncementTime(LocalDateTime.now());
        String tmFc = announcementTime.format(MID_TERM_TIMESTAMP_FORMAT);

        WeatherRepository.LandForecastRaw landForecast = weatherRepository.fetchLandForecast(region.getLandRegId(), tmFc);
        WeatherRepository.TemperatureForecastRaw temperatureForecast =
                weatherRepository.fetchTemperatureForecast(region.getTemperatureRegId(), tmFc);

        List<WeatherResponse.DailyForecastDTO> midTermForecasts =
                createMidTermDailyForecasts(announcementTime.toLocalDate(), landForecast, temperatureForecast);

        for (WeatherResponse.DailyForecastDTO forecast : midTermForecasts) {
            LocalDate forecastDate = forecast.getForecastDate();
            if (forecastDate.isBefore(actualStart) || forecastDate.isAfter(endDate)) {
                continue;
            }
            forecastMap.put(forecastDate, forecast);
        }
    }

    private WeatherRegion resolveRegion(String regionInput) {
        WeatherRegion region = WeatherRegion.fromInput(regionInput);
        if (region == null) {
            throw new Exception400("지원하지 않는 지역입니다.");
        }
        return region;
    }

    private void validateTargetDate(LocalDate targetDate) {
        if (targetDate == null) {
            throw new Exception400("targetDate는 필수입니다.");
        }
        if (targetDate.isBefore(LocalDate.now())) {
            throw new Exception400("과거 날짜 예보는 조회할 수 없습니다.");
        }
        if (targetDate.isAfter(LocalDate.now().plusDays(10))) {
            throw new Exception400("예보 가능 범위를 벗어난 날짜입니다.");
        }
    }

    private LocalDateTime resolveMidTermAnnouncementTime(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (now.getHour() >= 18) {
            return today.atTime(18, 0);
        }
        if (now.getHour() >= 6) {
            return today.atTime(6, 0);
        }
        return today.minusDays(1).atTime(18, 0);
    }

    private LocalDateTime resolveShortTermBaseDateTime(LocalDateTime now) {
        LocalDateTime adjustedNow = now.minusMinutes(10);
        LocalDate date = adjustedNow.toLocalDate();
        LocalTime time = adjustedNow.toLocalTime();

        for (int i = SHORT_TERM_BASE_TIMES.size() - 1; i >= 0; i--) {
            LocalTime baseTime = SHORT_TERM_BASE_TIMES.get(i);
            if (!time.isBefore(baseTime)) {
                return LocalDateTime.of(date, baseTime);
            }
        }

        return LocalDateTime.of(date.minusDays(1), LocalTime.of(23, 0));
    }

    private List<WeatherResponse.DailyForecastDTO> createMidTermDailyForecasts(
            LocalDate announcementDate,
            WeatherRepository.LandForecastRaw landForecast,
            WeatherRepository.TemperatureForecastRaw temperatureForecast) {
        List<WeatherResponse.DailyForecastDTO> forecasts = new ArrayList<>();

        for (int dayOffset = 4; dayOffset <= 10; dayOffset++) {
            WeatherResponse.DailyForecastDTO forecast = createMidTermDailyForecast(
                    announcementDate.plusDays(dayOffset),
                    dayOffset,
                    landForecast,
                    temperatureForecast);
            if (forecast != null) {
                forecasts.add(forecast);
            }
        }

        return forecasts;
    }

    private WeatherResponse.DailyForecastDTO createMidTermDailyForecast(
            LocalDate forecastDate,
            int dayOffset,
            WeatherRepository.LandForecastRaw landForecast,
            WeatherRepository.TemperatureForecastRaw temperatureForecast) {
        String weatherAm = landForecast.getText("wf" + dayOffset + "Am");
        String weatherPm = landForecast.getText("wf" + dayOffset + "Pm");
        String weather = landForecast.getText("wf" + dayOffset);

        Integer rainProbabilityAm = readLandRainProbability(landForecast, "rnSt" + dayOffset + "Am");
        Integer rainProbabilityPm = readLandRainProbability(landForecast, "rnSt" + dayOffset + "Pm");
        Integer rainProbability = readLandRainProbability(landForecast, "rnSt" + dayOffset);

        Integer minTemperature = temperatureForecast.getNumber("taMin" + dayOffset);
        Integer maxTemperature = temperatureForecast.getNumber("taMax" + dayOffset);

        if (weatherAm == null && weatherPm == null && weather == null && minTemperature == null && maxTemperature == null) {
            return null;
        }

        return WeatherResponse.DailyForecastDTO.createDailyForecast(
                forecastDate,
                toKoreanDayOfWeek(forecastDate.getDayOfWeek()),
                minTemperature,
                maxTemperature,
                weatherAm,
                weatherPm,
                resolveWeatherSummary(weatherAm, weatherPm, weather),
                rainProbabilityAm,
                rainProbabilityPm,
                rainProbability);
    }

    private WeatherResponse.DailyForecastDTO createShortTermDailyForecast(
            LocalDate targetDate,
            WeatherRepository.ShortTermForecastRaw shortTermForecast) {
        List<WeatherRepository.ShortTermForecastItem> targetItems = shortTermForecast.getItems().stream()
                .filter(item -> targetDate.equals(item.getForecastDate()))
                .toList();

        if (targetItems.isEmpty()) {
            return null;
        }

        Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory = targetItems.stream()
                .collect(Collectors.groupingBy(WeatherRepository.ShortTermForecastItem::getCategory));

        Integer minTemperature = resolveShortTermMinTemperature(itemsByCategory);
        Integer maxTemperature = resolveShortTermMaxTemperature(itemsByCategory);
        String weatherAm = resolveShortTermWeather(itemsByCategory, true);
        String weatherPm = resolveShortTermWeather(itemsByCategory, false);
        Integer rainProbabilityAm = resolveShortTermRainProbability(itemsByCategory, true);
        Integer rainProbabilityPm = resolveShortTermRainProbability(itemsByCategory, false);
        Integer rainProbability = maxNumber(rainProbabilityAm, rainProbabilityPm, resolveMaxRainProbability(itemsByCategory));

        return WeatherResponse.DailyForecastDTO.createDailyForecast(
                targetDate,
                toKoreanDayOfWeek(targetDate.getDayOfWeek()),
                minTemperature,
                maxTemperature,
                weatherAm,
                weatherPm,
                resolveWeatherSummary(weatherAm, weatherPm, null),
                rainProbabilityAm,
                rainProbabilityPm,
                rainProbability);
    }

    private Integer resolveShortTermMinTemperature(Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory) {
        Integer tmn = readFirstInteger(itemsByCategory.get("TMN"));
        if (tmn != null) {
            return tmn;
        }
        return itemsByCategory.getOrDefault("TMP", List.of()).stream()
                .map(item -> readInteger(item.getForecastValue()))
                .filter(value -> value != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private Integer resolveShortTermMaxTemperature(Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory) {
        Integer tmx = readFirstInteger(itemsByCategory.get("TMX"));
        if (tmx != null) {
            return tmx;
        }
        return itemsByCategory.getOrDefault("TMP", List.of()).stream()
                .map(item -> readInteger(item.getForecastValue()))
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private String resolveShortTermWeather(
            Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory,
            boolean morning) {
        return itemsByCategory.getOrDefault("SKY", List.of()).stream()
                .filter(item -> isInPeriod(item.getForecastTime(), morning))
                .sorted(Comparator.comparing(WeatherRepository.ShortTermForecastItem::getForecastTime))
                .map(item -> {
                    String precipitationCode = findPrecipitationCode(itemsByCategory, item.getForecastTime(), morning);
                    return convertWeatherCode(item.getForecastValue(), precipitationCode);
                })
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseGet(() -> {
                    String precipitationOnly = findAnyPrecipitationCode(itemsByCategory, morning);
                    return convertWeatherCode(null, precipitationOnly);
                });
    }

    private String findPrecipitationCode(
            Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory,
            String forecastTime,
            boolean morning) {
        return itemsByCategory.getOrDefault("PTY", List.of()).stream()
                .filter(item -> isInPeriod(item.getForecastTime(), morning))
                .filter(item -> forecastTime.equals(item.getForecastTime()))
                .map(WeatherRepository.ShortTermForecastItem::getForecastValue)
                .findFirst()
                .orElse("0");
    }

    private String findAnyPrecipitationCode(
            Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory,
            boolean morning) {
        return itemsByCategory.getOrDefault("PTY", List.of()).stream()
                .filter(item -> isInPeriod(item.getForecastTime(), morning))
                .map(WeatherRepository.ShortTermForecastItem::getForecastValue)
                .filter(value -> value != null && !"0".equals(value))
                .findFirst()
                .orElse(null);
    }

    private Integer resolveShortTermRainProbability(
            Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory,
            boolean morning) {
        return itemsByCategory.getOrDefault("POP", List.of()).stream()
                .filter(item -> isInPeriod(item.getForecastTime(), morning))
                .map(item -> readInteger(item.getForecastValue()))
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Integer resolveMaxRainProbability(Map<String, List<WeatherRepository.ShortTermForecastItem>> itemsByCategory) {
        return itemsByCategory.getOrDefault("POP", List.of()).stream()
                .map(item -> readInteger(item.getForecastValue()))
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Integer readFirstInteger(List<WeatherRepository.ShortTermForecastItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .map(item -> readInteger(item.getForecastValue()))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }

    private Integer readInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isInPeriod(String forecastTime, boolean morning) {
        if (forecastTime == null || forecastTime.length() < 2) {
            return false;
        }
        int hour = Integer.parseInt(forecastTime.substring(0, 2));
        return morning ? hour < 12 : hour >= 12;
    }

    private String convertWeatherCode(String skyCode, String precipitationCode) {
        if (precipitationCode != null && !precipitationCode.isBlank() && !"0".equals(precipitationCode)) {
            return switch (precipitationCode) {
                case "1" -> "비";
                case "2" -> "비/눈";
                case "3" -> "눈";
                case "4" -> "소나기";
                case "5" -> "빗방울";
                case "6" -> "빗방울/눈날림";
                case "7" -> "눈날림";
                default -> "비";
            };
        }
        if (skyCode == null || skyCode.isBlank()) {
            return null;
        }
        return switch (skyCode) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> null;
        };
    }

    private Integer readLandRainProbability(WeatherRepository.LandForecastRaw landForecast, String key) {
        String value = landForecast.getText(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveWeatherSummary(String weatherAm, String weatherPm, String weather) {
        if (weather != null && !weather.isBlank()) {
            return weather;
        }
        if (weatherAm == null || weatherAm.isBlank()) {
            return weatherPm;
        }
        if (weatherPm == null || weatherPm.isBlank()) {
            return weatherAm;
        }
        if (weatherAm.equals(weatherPm)) {
            return weatherAm;
        }
        return weatherAm + " / " + weatherPm;
    }

    private Integer maxNumber(Integer... values) {
        Integer max = null;
        for (Integer value : values) {
            if (value == null) {
                continue;
            }
            if (max == null || value > max) {
                max = value;
            }
        }
        return max;
    }

    private String toKoreanDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
