package com.example.travel_platform.board.reply;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Data;

public class ReplyResponse {

    @Data
    @Builder
    public static class CreatedDTO {
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private String createdAtDisplay;
        private boolean isOwner;

        public static CreatedDTO from(Reply reply) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime createdAt = reply.getCreatedAt() == null ? LocalDateTime.now() : reply.getCreatedAt();

            return CreatedDTO.builder()
                    .id(reply.getId())
                    .boardId(reply.getBoard().getId())
                    .username(reply.getUser().getUsername())
                    .content(reply.getContent())
                    .createdAtDisplay(createdAt.format(formatter))
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

        public static UpdatedDTO from(Reply reply) {
            return UpdatedDTO.builder()
                    .boardId(reply.getBoard().getId())
                    .replyId(reply.getId())
                    .content(reply.getContent())
                    .build();
        }
    }
}
