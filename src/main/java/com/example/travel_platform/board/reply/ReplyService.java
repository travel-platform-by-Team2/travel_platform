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
    public ReplyResponse.CreatedDTO createReply(Integer sessionUserId, Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        ReplyActor actor = requireActor(sessionUserId);
        Board board = findBoard(boardId);
        Reply savedReply = saveReply(board, actor, reqDTO);
        return ReplyResponse.CreatedDTO.from(savedReply);
    }

    @Transactional
    public ReplyResponse.UpdatedDTO updateReply(Integer sessionUserId, Integer boardId, Integer replyId,
            ReplyRequest.UpdateDTO reqDTO) {
        Reply reply = findReplyInBoard(boardId, replyId);
        ReplyActor actor = requireActor(sessionUserId);
        validateReplyEditor(actor, reply);
        reply.updateContent(reqDTO.getContent());
        return ReplyResponse.UpdatedDTO.from(reply);
    }

    @Transactional
    public void deleteReply(Integer sessionUserId, Integer boardId, Integer replyId) {
        Reply reply = findReplyInBoard(boardId, replyId);
        ReplyActor actor = requireActor(sessionUserId);
        validateReplyEditor(actor, reply);
        replyRepository.delete(reply);
    }

    private Reply saveReply(Board board, ReplyActor actor, ReplyRequest.CreateDTO reqDTO) {
        Reply reply = Reply.create(board, actor.requireUser(), reqDTO.getContent());
        return replyRepository.save(reply);
    }

    private Reply findReplyInBoard(Integer boardId, Integer replyId) {
        Reply reply = findReply(replyId);
        validateBoardMatch(boardId, reply);
        return reply;
    }

    private void validateReplyEditor(ReplyActor actor, Reply reply) {
        if (!actor.canManage(reply)) {
            throw new Exception403("본인 댓글만 수정/삭제할 수 있습니다.");
        }
    }

    private void validateBoardMatch(Integer boardId, Reply reply) {
        if (!reply.getBoard().getId().equals(boardId)) {
            throw new Exception404("게시글과 댓글 정보가 일치하지 않습니다.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
    }

    private ReplyActor requireActor(Integer sessionUserId) {
        return ReplyActor.of(findUser(sessionUserId));
    }

    private Board findBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
    }

    private Reply findReply(Integer replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));
    }

    private static final class ReplyActor {

        private final User user;

        private ReplyActor(User user) {
            this.user = user;
        }

        private static ReplyActor of(User user) {
            return new ReplyActor(user);
        }

        private boolean canManage(Reply reply) {
            return reply.getUser().getId().equals(user.getId());
        }

        private User requireUser() {
            return user;
        }
    }
}
