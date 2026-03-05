package com.example.travel_platform.board;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.reply.Reply;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBoard(Integer sessionUserId, BoardRequest.CreateBoardDTO reqDTO) {
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));

        Board board = new Board();
        board.setUser(sessionUser);
        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        board.setViewCount(0);

        boardRepository.save(board);
    }

    @Transactional
    public void updateBoard(Integer sessionUserId, Integer boardId, BoardRequest.UpdateBoardDTO reqDTO) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, board);

        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
    }

    @Transactional
    public void deleteBoard(Integer sessionUserId, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, board);
        boardRepository.delete(board);
    }

    public List<BoardResponse.BoardSummaryDTO> getBoardList() {
        return boardRepository.findAll().stream()
                .map(board -> BoardResponse.BoardSummaryDTO.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .username(board.getUser().getUsername())
                        .viewCount(board.getViewCount())
                        .createdAt(board.getCreatedAt())
                        .build())
                .toList();
    }

    public BoardResponse.BoardDetailDTO getBoardDetail(Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        List<BoardResponse.ReplyDTO> replies = board.getReplies().stream()
                .map(this::toReplyDTO)
                .toList();

        return BoardResponse.BoardDetailDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .username(board.getUser().getUsername())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .replies(replies)
                .build();
    }

    private BoardResponse.ReplyDTO toReplyDTO(Reply reply) {
        return BoardResponse.ReplyDTO.builder()
                .id(reply.getId())
                .username(reply.getUser().getUsername())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    private void validateOwner(Integer sessionUserId, Board board) {
        if (!board.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }
}
