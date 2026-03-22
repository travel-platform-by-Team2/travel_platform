package com.example.travel_platform.board;

import java.util.Arrays;

import com.example.travel_platform._core.validation.EnumCode;

public enum BoardCategory implements EnumCode {

    TIPS("tips", "여행 팁", "cat-tips"),
    PLAN("plan", "여행 계획", "cat-plan"),
    FOOD("food", "맛집/카페", "cat-food"),
    REVIEW("review", "숙소 후기", "cat-review"),
    QNA("qna", "질문/답변", "cat-qna");

    private final String code;
    private final String label;
    private final String cssClass;

    BoardCategory(String code, String label, String cssClass) {
        this.code = code;
        this.label = label;
        this.cssClass = cssClass;
    }

    @Override
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static BoardCategory fromCode(String code) {
        BoardCategory boardCategory = fromCodeOrNull(code);
        if (boardCategory == null) {
            throw new IllegalArgumentException("유효하지 않은 게시글 카테고리입니다: " + code);
        }
        return boardCategory;
    }

    public static BoardCategory fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(boardCategory -> boardCategory.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValidCode(String code) {
        return fromCodeOrNull(code) != null;
    }
}
