package com.example.travel_platform.board.reply;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Data;

public class ReplyResponse {

    @Data
    @Builder
    public static class CreateAjaxDTO {
        private boolean success;
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private String createdAtDisplay;
        private boolean isOwner;

        public static CreateAjaxDTO from(Reply reply, Integer boardId) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime createdAt = reply.getCreatedAt() == null ? LocalDateTime.now() : reply.getCreatedAt();

            return CreateAjaxDTO.builder()
                    .success(true)
                    .id(reply.getId())
                    .boardId(boardId)
                    .username(reply.getUser().getUsername())
                    .content(reply.getContent())
                    .createdAtDisplay(createdAt.format(formatter))
                    .isOwner(true)
                    .build();
        }
    }

    @Data
    @Builder
    public static class UpdateAjaxDTO {
        private boolean success;
        private Integer boardId;
        private Integer replyId;
        private String content;

        public static UpdateAjaxDTO of(Integer boardId, Integer replyId, String content) {
            return UpdateAjaxDTO.builder()
                    .success(true)
                    .boardId(boardId)
                    .replyId(replyId)
                    .content(content)
                    .build();
        }
    }
}
