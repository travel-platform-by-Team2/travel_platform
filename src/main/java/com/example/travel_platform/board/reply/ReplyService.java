package com.example.travel_platform.board.reply;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reply createReply(Integer sessionUserId, Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        Reply reply = new Reply();
        reply.setBoard(board);
        reply.setUser(sessionUser);
        reply.setContent(reqDTO.getContent());
        return replyRepository.save(reply);
    }

    @Transactional
    public void updateReply(Integer sessionUserId, Integer replyId, ReplyRequest.UpdateDTO reqDTO) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, reply);
        reply.setContent(reqDTO.getContent());
    }

    @Transactional
    public void deleteReply(Integer sessionUserId, Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, reply);
        replyRepository.delete(reply);
    }

    private void validateOwner(Integer sessionUserId, Reply reply) {
        if (!reply.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("본인 댓글만 수정/삭제할 수 있습니다.");
        }
    }
}
