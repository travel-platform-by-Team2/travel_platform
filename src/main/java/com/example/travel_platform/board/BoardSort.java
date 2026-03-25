package com.example.travel_platform.board;

import java.util.Arrays;

public enum BoardSort {

    LATEST("latest", "날짜 최신 순", "b.createdAt desc, b.id desc", "date", "날짜", false, "date"),
    DATE("date", "날짜 오래된 순", "b.createdAt asc, b.id asc", "date", "날짜", true, "latest"),
    VIEW("view", "조회수 많은 순", "b.viewCount desc, b.createdAt desc, b.id desc", "view", "조회수", false, "downview"),
    DOWNVIEW("downview", "조회수 적은 순", "b.viewCount asc, b.createdAt asc, b.id asc", "view", "조회수", true, "view"),
    LIKES(
            "likes",
            "좋아요 많은 순",
            "(select count(bl) from BoardLike bl where bl.board = b) desc, b.createdAt desc, b.id desc",
            "likes",
            "좋아요수",
            false,
            "downlikes"),
    DOWNLIKES(
            "downlikes",
            "좋아요 적은 순",
            "(select count(bl) from BoardLike bl where bl.board = b) asc, b.createdAt asc, b.id asc",
            "likes",
            "좋아요수",
            true,
            "likes");

    private final String code;
    private final String label;
    private final String orderBy;
    private final String field;
    private final String fieldLabel;
    private final boolean ascending;
    private final String toggleCode;

    BoardSort(
            String code,
            String label,
            String orderBy,
            String field,
            String fieldLabel,
            boolean ascending,
            String toggleCode) {
        this.code = code;
        this.label = label;
        this.orderBy = orderBy;
        this.field = field;
        this.fieldLabel = fieldLabel;
        this.ascending = ascending;
        this.toggleCode = toggleCode;
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
        return fieldLabel;
    }

    public String getDirectionLabel() {
        return ascending ? "오름차순" : "내림차순";
    }

    public boolean isAscending() {
        return ascending;
    }

    public BoardSort toggleDirection() {
        return fromCodeOrDefault(toggleCode);
    }

    public static BoardSort fieldDefault(String field) {
        if (field == null || field.isBlank()) {
            return LATEST;
        }

        return Arrays.stream(values())
                .filter(boardSort -> boardSort.field.equals(field))
                .filter(boardSort -> !boardSort.ascending)
                .findFirst()
                .orElse(LATEST);
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
