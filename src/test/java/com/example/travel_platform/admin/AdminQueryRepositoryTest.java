package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.board.BoardSort;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

@SpringBootTest
@Transactional
class AdminQueryRepositoryTest {

    @Autowired
    private AdminQueryRepository adminQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Test
    void userKw() {
        User user = User.create("CaseUser", "1234", "CaseUser@Example.com", "010", "USER");
        userRepository.save(user);

        List<AdminUserSummaryRow> rows = adminQueryRepository.findUserSummaryRowsByKeyword("caseuser");

        assertTrue(rows.stream().anyMatch(row -> row.userId().equals(user.getId())));
    }

    @Test
    void boardKw() {
        User user = User.create("boardcase", "1234", "boardcase@example.com", "010", "USER");
        userRepository.save(user);

        Board board = Board.create(user, "서울 후기", "tips", "BUSAN SPOT review");
        boardRepository.save(board);

        List<AdminBoardSummaryRow> rows = adminQueryRepository.findBoardSummaryRows(
                null,
                new String[] { "busan" },
                BoardSort.LATEST,
                0,
                50);

        assertTrue(rows.stream().anyMatch(row -> row.boardId().equals(board.getId())));
    }
}
