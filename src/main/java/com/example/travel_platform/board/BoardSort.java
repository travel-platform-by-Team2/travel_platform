package com.example.travel_platform.board;

import java.util.Arrays;

public enum BoardSort {

    LATEST("latest", "날짜 최신 순", "b.createdAt desc, b.id desc"),
    DATE("date", "날짜 오래된 순", "b.createdAt asc, b.id asc"),
    VIEW("view", "조회수 많은 순", "b.viewCount desc, b.createdAt desc, b.id desc"),
    DOWNVIEW("downview", "조회수 적은 순", "b.viewCount asc, b.createdAt asc, b.id asc"),
    LIKES("likes", "좋아요 많은 순",
            "(select count(bl) from BoardLike bl where bl.board = b) desc, b.createdAt desc, b.id desc"),
    DOWNLIKES("downlikes", "좋아요 적은 순",
            "(select count(bl) from BoardLike bl where bl.board = b) asc, b.createdAt asc, b.id asc");

    private final String code;
    private final String label;
    private final String orderBy;

    BoardSort(String code, String label, String orderBy) {
        this.code = code;
        this.label = label;
        this.orderBy = orderBy;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getFieldLabel() {
        if (this == VIEW || this == DOWNVIEW) {
            return "조회수";
        }
        if (this == LIKES || this == DOWNLIKES) {
            return "좋아요수";
        }
        return "날짜순";
    }

    public String getDirectionLabel() {
        return isAscending() ? "오름차순" : "내림차순";
    }

    public boolean isAscending() {
        return this == DATE || this == DOWNVIEW || this == DOWNLIKES;
    }

    public BoardSort toggleDirection() {
        return switch (this) {
            case LATEST -> DATE;
            case DATE -> LATEST;
            case VIEW -> DOWNVIEW;
            case DOWNVIEW -> VIEW;
            case LIKES -> DOWNLIKES;
            case DOWNLIKES -> LIKES;
        };
    }

    public static BoardSort fieldDefault(String field) {
        if ("view".equals(field)) {
            return VIEW;
        }
        if ("likes".equals(field)) {
            return LIKES;
        }
        return LATEST;
    }

    public static BoardSort forField(String field, boolean ascending) {
        BoardSort defaultSort = fieldDefault(field);
        return ascending ? defaultSort.toggleDirection() : defaultSort;
    }

    public static BoardSort fromCodeOrDefault(String code) {
        if (code == null || code.isBlank()) {
            return LATEST;
        }

        return Arrays.stream(values())
                .filter(boardSort -> boardSort.code.equals(code))
                .findFirst()
                .orElse(LATEST);
    }
}
