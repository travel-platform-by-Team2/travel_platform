package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.board.BoardLikeRepository;
import com.example.travel_platform.board.BoardCategory;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

class AdminServiceTest {

    @Test
    void del401() {
        AdminService service = service(mock(AdminUserQueryRepository.class), mock(AdminBoardQueryRepository.class),
                mock(UserRepository.class), mock(BoardRepository.class), mock(BoardLikeRepository.class));

        assertThrows(Exception401.class, () -> service.deleteBoardByAdmin(null, 7));
    }

    @Test
    void del403() {
        AdminService service = service(mock(AdminUserQueryRepository.class), mock(AdminBoardQueryRepository.class),
                mock(UserRepository.class), mock(BoardRepository.class), mock(BoardLikeRepository.class));
        SessionUser sessionUser = new SessionUser(1, "ssar", "ssar@nate.com", "010", "USER");

        assertThrows(Exception403.class, () -> service.deleteBoardByAdmin(sessionUser, 7));
    }

    @Test
    void del404() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        AdminService service = service(mock(AdminUserQueryRepository.class), mock(AdminBoardQueryRepository.class),
                mock(UserRepository.class), boardRepository, mock(BoardLikeRepository.class));
        SessionUser sessionUser = new SessionUser(1, "admin", "admin@nate.com", "010", "ADMIN");

        when(boardRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(Exception404.class, () -> service.deleteBoardByAdmin(sessionUser, 7));
    }

    @Test
    void del() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        AdminService service = service(mock(AdminUserQueryRepository.class), mock(AdminBoardQueryRepository.class),
                mock(UserRepository.class), boardRepository, boardLikeRepository);
        SessionUser sessionUser = new SessionUser(1, "admin", "admin@nate.com", "010", "ADMIN");
        Board board = board(7, "tips", "Busan tips");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));

        service.deleteBoardByAdmin(sessionUser, 7);

        verify(boardLikeRepository).deleteByBoardId(7);
        verify(boardRepository).delete(board);
    }

    @Test
    void boardsSearch() {
        AdminBoardQueryRepository adminBoardQueryRepository = mock(AdminBoardQueryRepository.class);
        AdminService service = service(mock(AdminUserQueryRepository.class), adminBoardQueryRepository,
                mock(UserRepository.class), mock(BoardRepository.class), mock(BoardLikeRepository.class));

        AdminBoardSummaryRow boardRow = new AdminBoardSummaryRow(
                3,
                "Busan travel",
                "admin",
                LocalDateTime.of(2026, 3, 20, 12, 0),
                12,
                BoardCategory.TIPS);

        when(adminBoardQueryRepository.findBoardSummaryRows(eq(BoardCategory.TIPS), any(String[].class), eq("view"), eq(10), eq(10)))
                .thenReturn(List.of(boardRow));
        when(adminBoardQueryRepository.countBoardSummaryRows(eq(BoardCategory.TIPS), any(String[].class)))
                .thenReturn(1L);
        when(adminBoardQueryRepository.countBoards()).thenReturn(4L);

        AdminResponse.BoardListViewDTO view = service.getBoardListView("tips", " busan travel ", "view", 1);

        assertEquals("tips", view.getModel().getAllCategory());
        assertEquals("busan travel", view.getModel().getKeyword());
        assertEquals("view", view.getModel().getSort());
        assertEquals(1L, view.getModel().getTotalCount());
        assertEquals(4L, view.getModel().getAllCount());
        assertEquals(1, view.getModels().size());
        assertEquals("Busan travel", view.getModels().get(0).getTitle());
        assertEquals(boardRow.createdAt().toLocalDate(), view.getModels().get(0).getCreatedDate());
        assertEquals("여행 팁", view.getModels().get(0).getCategory());
        assertEquals(0, view.getModel().getPrevPage());
        assertNull(view.getModel().getNextPage());
    }

    @Test
    void boardsDefault() {
        AdminBoardQueryRepository adminBoardQueryRepository = mock(AdminBoardQueryRepository.class);
        AdminService service = service(mock(AdminUserQueryRepository.class), adminBoardQueryRepository,
                mock(UserRepository.class), mock(BoardRepository.class), mock(BoardLikeRepository.class));

        when(adminBoardQueryRepository.findBoardSummaryRows(isNull(), any(String[].class), eq("latest"), eq(0), eq(10)))
                .thenReturn(List.of());
        when(adminBoardQueryRepository.countBoardSummaryRows(isNull(), any(String[].class))).thenReturn(0L);
        when(adminBoardQueryRepository.countBoards()).thenReturn(0L);

        AdminResponse.BoardListViewDTO view = service.getBoardListView(null, "", "wrong", 0);

        assertEquals("all", view.getModel().getAllCategory());
        assertTrue(view.getModel().isAllCategoryTab());
        assertEquals("latest", view.getModel().getSort());
        assertEquals(1, view.getModel().getTotalPages());
        assertNull(view.getModel().getPrevPage());
        assertNull(view.getModel().getNextPage());
    }

    private AdminService service(
            AdminUserQueryRepository adminUserQueryRepository,
            AdminBoardQueryRepository adminBoardQueryRepository,
            UserRepository userRepository,
            BoardRepository boardRepository,
            BoardLikeRepository boardLikeRepository) {
        return new AdminService(adminUserQueryRepository, adminBoardQueryRepository, userRepository, boardRepository,
                boardLikeRepository);
    }

    private Board board(Integer id, String category, String title) {
        User user = User.create("admin", "1234", "admin@nate.com", "010", "ADMIN");
        user.setId(1);

        Board board = Board.create(user, title, category, "content");
        board.setId(id);
        board.setCreatedAt(LocalDateTime.of(2026, 3, 20, 12, 0));
        board.setViewCount(12);
        return board;
    }
}
