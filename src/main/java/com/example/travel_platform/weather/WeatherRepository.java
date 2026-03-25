package com.example.travel_platform.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Repository
public class WeatherRepository {

    private static final String LAND_FORECAST_PATH = "/getMidLandFcst";
    private static final String TEMPERATURE_FORECAST_PATH = "/getMidTa";
    private static final String SHORT_TERM_FORECAST_PATH = "/getVilageFcst";
    private static final DateTimeFormatter SHORT_TERM_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String serviceKey;
    private final String midTermEndpoint;
    private final String shortTermEndpoint;

    public WeatherRepository(
            @Value("${WEATHER_API_KEY:}") String serviceKey,
            @Value("${WEATHER_API_ENDPOINT:http://apis.data.go.kr/1360000/MidFcstInfoService}") String midTermEndpoint,
            @Value("${WEATHER_SHORT_TERM_API_ENDPOINT:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}") String shortTermEndpoint) {
        this.serviceKey = serviceKey;
        this.midTermEndpoint = midTermEndpoint;
        this.shortTermEndpoint = shortTermEndpoint;
    }

    public LandForecastRaw fetchLandForecast(String regId, String tmFc) {
        JsonObject item = requestSingleItem(midTermEndpoint + LAND_FORECAST_PATH, createMidTermParams(regId, tmFc));
        Map<String, String> values = new HashMap<>();
        item.entrySet().forEach(entry -> values.put(entry.getKey(), readText(entry.getValue())));
        return LandForecastRaw.createLandForecast(values);
    }

    public TemperatureForecastRaw fetchTemperatureForecast(String regId, String tmFc) {
        JsonObject item = requestSingleItem(midTermEndpoint + TEMPERATURE_FORECAST_PATH, createMidTermParams(regId, tmFc));
        Map<String, Integer> values = new HashMap<>();
        item.entrySet().forEach(entry -> values.put(entry.getKey(), readInteger(entry.getValue())));
        return TemperatureForecastRaw.createTemperatureForecast(values);
    }

    public ShortTermForecastRaw fetchShortTermForecast(int nx, int ny, String baseDate, String baseTime) {
        JsonArray items = requestItems(shortTermEndpoint + SHORT_TERM_FORECAST_PATH, createShortTermParams(nx, ny, baseDate, baseTime));
        List<ShortTermForecastItem> forecastItems = new ArrayList<>();

        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject item = element.getAsJsonObject();
            String category = readText(item.get("category"));
            String forecastDate = readText(item.get("fcstDate"));
            String forecastTime = readText(item.get("fcstTime"));
            String forecastValue = readText(item.get("fcstValue"));

            if (category == null || forecastDate == null || forecastTime == null || forecastValue == null) {
                continue;
            }

            forecastItems.add(ShortTermForecastItem.createShortTermForecastItem(
                    category,
                    LocalDate.parse(forecastDate, SHORT_TERM_DATE_FORMAT),
                    forecastTime,
                    forecastValue));
        }

        return ShortTermForecastRaw.createShortTermForecast(baseDate, baseTime, forecastItems);
    }

    private Map<String, String> createMidTermParams(String regId, String tmFc) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNo", "1");
        params.put("numOfRows", "10");
        params.put("dataType", "JSON");
        params.put("regId", regId);
        params.put("tmFc", tmFc);
        return params;
    }

    private Map<String, String> createShortTermParams(int nx, int ny, String baseDate, String baseTime) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNo", "1");
        params.put("numOfRows", "1000");
        params.put("dataType", "JSON");
        params.put("base_date", baseDate);
        params.put("base_time", baseTime);
        params.put("nx", String.valueOf(nx));
        params.put("ny", String.valueOf(ny));
        return params;
    }

    private JsonObject requestSingleItem(String endpoint, Map<String, String> params) {
        JsonArray items = requestItems(endpoint, params);
        if (items.isEmpty()) {
            throw new ApiException(
                    "WEATHER_API_ERROR",
                    "\uAE30\uC0C1\uCCAD \uB0A0\uC528 API \uC751\uB2F5\uC5D0 \uC608\uBCF4 \uB370\uC774\uD130\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return items.get(0).getAsJsonObject();
    }

    private JsonArray requestItems(String endpoint, Map<String, String> params) {
        String requestUrl = buildRequestUrl(endpoint, params);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) URI.create(requestUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, statusCode);

            if (statusCode >= 400) {
                throw new ApiException(
                        "WEATHER_API_ERROR",
                        "\uAE30\uC0C1\uCCAD \uB0A0\uC528 API \uD638\uCD9C\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4. status=" + statusCode,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject response = root.getAsJsonObject("response");
            JsonObject header = response.getAsJsonObject("header");

            String resultCode = readText(header.get("resultCode"));
            String resultMsg = readText(header.get("resultMsg"));
            if (!"00".equals(resultCode)) {
                throw new ApiException(
                        "WEATHER_API_ERROR",
                        "\uAE30\uC0C1\uCCAD \uB0A0\uC528 API \uC751\uB2F5 \uC624\uB958\uC785\uB2C8\uB2E4. " + resultMsg,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JsonObject body = response.getAsJsonObject("body");
            JsonObject items = body.getAsJsonObject("items");
            JsonElement itemElement = items.get("item");
            if (itemElement == null || itemElement.isJsonNull()) {
                throw new ApiException(
                        "WEATHER_API_ERROR",
                        "\uAE30\uC0C1\uCCAD \uB0A0\uC528 API \uC751\uB2F5\uC5D0 \uC608\uBCF4 \uB370\uC774\uD130\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (itemElement.isJsonArray()) {
                return itemElement.getAsJsonArray();
            }

            JsonArray array = new JsonArray();
            array.add(itemElement.getAsJsonObject());
            return array;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "WEATHER_API_ERROR",
                    "\uAE30\uC0C1\uCCAD \uB0A0\uC528 API \uCC98\uB9AC \uC911 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildRequestUrl(String endpoint, Map<String, String> params) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new ApiException(
                    "WEATHER_API_ERROR",
                    "WEATHER_API_KEY\uAC00 \uC124\uC815\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String encodedServiceKey = serviceKey.contains("%")
                ? serviceKey
                : URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        StringBuilder url = new StringBuilder(endpoint)
                .append("?serviceKey=").append(encodedServiceKey);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append("&")
                    .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    private String readResponseBody(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        }
    }

    private String readText(JsonElement node) {
        if (node == null || node.isJsonNull()) {
            return null;
        }
        return node.getAsString();
    }

    private Integer readInteger(JsonElement node) {
        if (node == null || node.isJsonNull()) {
            return null;
        }
        try {
            return node.getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    public static class LandForecastRaw {
        private final Map<String, String> values;

        private LandForecastRaw(Map<String, String> values) {
            this.values = values;
        }

        public static LandForecastRaw createLandForecast(Map<String, String> values) {
            return new LandForecastRaw(values);
        }

        public String getText(String key) {
            return values.get(key);
        }
    }

    public static class TemperatureForecastRaw {
        private final Map<String, Integer> values;

        private TemperatureForecastRaw(Map<String, Integer> values) {
            this.values = values;
        }

        public static TemperatureForecastRaw createTemperatureForecast(Map<String, Integer> values) {
            return new TemperatureForecastRaw(values);
        }

        public Integer getNumber(String key) {
            return values.get(key);
        }
    }

    public static class ShortTermForecastRaw {
        private final String baseDate;
        private final String baseTime;
        private final List<ShortTermForecastItem> items;

        private ShortTermForecastRaw(String baseDate, String baseTime, List<ShortTermForecastItem> items) {
            this.baseDate = baseDate;
            this.baseTime = baseTime;
            this.items = items;
        }

        public static ShortTermForecastRaw createShortTermForecast(
                String baseDate,
                String baseTime,
                List<ShortTermForecastItem> items) {
            return new ShortTermForecastRaw(baseDate, baseTime, items);
        }

        public String getBaseDate() {
            return baseDate;
        }

        public String getBaseTime() {
            return baseTime;
        }

        public List<ShortTermForecastItem> getItems() {
            return items;
        }
    }

    public static class ShortTermForecastItem {
        private final String category;
        private final LocalDate forecastDate;
        private final String forecastTime;
        private final String forecastValue;

        private ShortTermForecastItem(String category, LocalDate forecastDate, String forecastTime, String forecastValue) {
            this.category = category;
            this.forecastDate = forecastDate;
            this.forecastTime = forecastTime;
            this.forecastValue = forecastValue;
        }

        public static ShortTermForecastItem createShortTermForecastItem(
                String category,
                LocalDate forecastDate,
                String forecastTime,
                String forecastValue) {
            return new ShortTermForecastItem(category, forecastDate, forecastTime, forecastValue);
        }

        public String getCategory() {
            return category;
        }

        public LocalDate getForecastDate() {
            return forecastDate;
        }

        public String getForecastTime() {
            return forecastTime;
        }

        public String getForecastValue() {
            return forecastValue;
        }
    }
}
