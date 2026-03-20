package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.User;

class AdminServiceTest {

    @Test
    void del401() {
        AdminService service = new AdminService(mock(AdminRepository.class), mock(BoardRepository.class));

        assertThrows(Exception401.class, () -> service.deleteBoard(null, 7));
    }

    @Test
    void del403() {
        AdminService service = new AdminService(mock(AdminRepository.class), mock(BoardRepository.class));
        SessionUser sessionUser = new SessionUser(1, "ssar", "ssar@nate.com", "010", "USER");

        assertThrows(Exception403.class, () -> service.deleteBoard(sessionUser, 7));
    }

    @Test
    void del404() {
        AdminRepository adminRepository = mock(AdminRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        AdminService service = new AdminService(adminRepository, boardRepository);
        SessionUser sessionUser = new SessionUser(1, "admin", "admin@nate.com", "010", "ADMIN");

        when(boardRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(Exception404.class, () -> service.deleteBoard(sessionUser, 7));
    }

    @Test
    void del() {
        AdminRepository adminRepository = mock(AdminRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        AdminService service = new AdminService(adminRepository, boardRepository);
        SessionUser sessionUser = new SessionUser(1, "admin", "admin@nate.com", "010", "ADMIN");
        Board board = board(7, "tips", "부산 팁");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));

        service.deleteBoard(sessionUser, 7);

        verify(boardRepository).deleteLikesByBoard(7);
        verify(boardRepository).delete(board);
    }

    @Test
    void boardsSearch() {
        AdminRepository adminRepository = mock(AdminRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        AdminService service = new AdminService(adminRepository, boardRepository);
        Board board = board(3, "tips", "부산 여행 팁");

        when(boardRepository.search(eq("tips"), any(String[].class), eq("view"), eq(10), eq(10)))
                .thenReturn(List.of(board));
        when(boardRepository.countSearch(eq("tips"), any(String[].class))).thenReturn(1L);
        when(boardRepository.count()).thenReturn(4L);

        AdminResponse.AdminBoardListDTO page = service.getBoardsPage("tips", " 부산  여행 ", "view", 1);

        assertEquals("tips", page.getAllCategory());
        assertEquals("부산  여행", page.getKeyword());
        assertEquals("view", page.getSort());
        assertEquals(1L, page.getTotalCount());
        assertEquals(4L, page.getAllCount());
        assertEquals(1, page.getBoards().size());
        assertEquals("부산 여행 팁", page.getBoards().get(0).getTitle());
        assertEquals(board.getCreatedAt().toLocalDate(), page.getBoards().get(0).getCreatedDate());
        assertEquals(0, page.getPrevPage());
        assertNull(page.getNextPage());
    }

    @Test
    void boardsDefault() {
        AdminRepository adminRepository = mock(AdminRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        AdminService service = new AdminService(adminRepository, boardRepository);

        when(boardRepository.findAllPaging("latest", 0, 10)).thenReturn(List.of());
        when(boardRepository.count()).thenReturn(0L);

        AdminResponse.AdminBoardListDTO page = service.getBoardsPage(null, "", "wrong", 0);

        assertEquals("all", page.getAllCategory());
        assertTrue(page.isAllCategoryTab());
        assertEquals("latest", page.getSort());
        assertEquals(1, page.getTotalPages());
        assertNull(page.getPrevPage());
        assertNull(page.getNextPage());
    }

    private Board board(Integer id, String category, String title) {
        User user = User.create("admin", "1234", "admin@nate.com", "010", "ADMIN");
        user.setId(1);

        Board board = Board.create(user, title, category, "내용");
        board.setId(id);
        board.setCreatedAt(LocalDateTime.of(2026, 3, 20, 12, 0));
        board.setViewCount(12);
        return board;
    }
}
