package com.example.travel_platform.board;

import java.util.List;

import org.jsoup.Jsoup;
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

    public List<BoardResponse.BoardSummaryDTO> getBoardList() { // 서머노트 글자 깨짐으로 jsoup빼고 설정
        return boardRepository.findAll().stream()
                .map(board -> {
                    String plainText = Jsoup.parse(board.getContent()).text();
                    String summary = plainText.substring(0, Math.min(80, plainText.length()));

                    return BoardResponse.BoardSummaryDTO.builder()
                            .id(board.getId())
                            .title(board.getTitle())
                            .summary(summary)
                            .username(board.getUser().getUsername())
                            .viewCount(board.getViewCount())
                            .replyCount(board.getReplies().size())
                            .createdAt(board.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public BoardResponse.BoardDetailDTO getBoardDetail(Integer sessionUserId, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        List<BoardResponse.ReplyDTO> replies = board.getReplies().stream()
                .map(reply -> toReplyDTO(sessionUserId, reply))
                .toList();

        boolean isOwner = false;
        if (sessionUserId != null) {
            isOwner = board.getUser().getId().equals(sessionUserId);
        }

        return BoardResponse.BoardDetailDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .username(board.getUser().getUsername())
                .viewCount(board.getViewCount())
                .replyCount(board.getReplies().size())
                .createdAt(board.getCreatedAt())
                .replies(replies)
                .isOwner(isOwner)
                .build();
    }

    private BoardResponse.ReplyDTO toReplyDTO(Integer sessionUserId, Reply reply) {
        boolean isOwner = false;
        if (sessionUserId != null) {
            isOwner = reply.getUser().getId().equals(sessionUserId);
        }
        return BoardResponse.ReplyDTO.builder()
                .id(reply.getId())
                .boardId(reply.getBoard().getId())
                .username(reply.getUser().getUsername())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .isOwner(isOwner)
                .build();
    }

    private void validateOwner(Integer sessionUserId, Board board) {
        if (!board.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }
}
