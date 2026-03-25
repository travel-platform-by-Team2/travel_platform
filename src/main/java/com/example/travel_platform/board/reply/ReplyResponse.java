package com.example.travel_platform.board.reply;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

public class ReplyResponse {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Data
    @Builder
    public static class CreatedDTO {
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private String createdAtDisplay;
        @JsonProperty("isOwner")
        private boolean isOwner;

        public static CreatedDTO fromReply(Reply reply) {
            LocalDateTime createdAt = reply.getCreatedAt() == null ? LocalDateTime.now() : reply.getCreatedAt();

            return CreatedDTO.builder()
                    .id(reply.getId())
                    .boardId(reply.getBoard().getId())
                    .username(reply.getUser().getUsername())
                    .content(reply.getContent())
                    .createdAtDisplay(createdAt.format(DATE_TIME_FORMATTER))
                    .isOwner(true)
                    .build();
        }
    }

    @Data
    @Builder
    public static class UpdatedDTO {
        private Integer boardId;
        private Integer replyId;
        private String content;

        public static UpdatedDTO fromReply(Reply reply) {
            return UpdatedDTO.builder()
                    .boardId(reply.getBoard().getId())
                    .replyId(reply.getId())
                    .content(reply.getContent())
                    .build();
        }
    }
}
