package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class AdminControllerTest {

    @Test
    void dash() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        AdminResponse.DashboardPageDTO pageDTO = new AdminResponse.DashboardPageDTO();

        when(adminService.getDashboardPage()).thenReturn(pageDTO);

        String view = controller.dashboard(model);

        assertEquals("pages/admin-dashboard", view);
        assertSame(pageDTO, model.getAttribute("page"));
        assertEquals(" is-active", pageDTO.getDashboardActiveClass());
        assertEquals("", pageDTO.getUsersActiveClass());
        assertEquals("", pageDTO.getBoardsActiveClass());
        verify(adminService).getDashboardPage();
    }

    @Test
    void users() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        AdminResponse.UserListPageDTO pageDTO = new AdminResponse.UserListPageDTO();

        when(adminService.getUsersPage(true, "ssar")).thenReturn(pageDTO);

        String view = controller.users(true, "ssar", model);

        assertEquals("pages/admin-users", view);
        assertSame(pageDTO, model.getAttribute("page"));
        assertEquals("", pageDTO.getDashboardActiveClass());
        assertEquals(" is-active", pageDTO.getUsersActiveClass());
        assertEquals("", pageDTO.getBoardsActiveClass());
        verify(adminService).getUsersPage(true, "ssar");
    }

    @Test
    void boards() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        AdminResponse.AdminBoardListDTO pageDTO = new AdminResponse.AdminBoardListDTO();

        when(adminService.getBoardsPage("tips", "busan", "latest", 1)).thenReturn(pageDTO);

        String view = controller.boards("tips", "busan", "latest", 1, model);

        assertEquals("pages/admin-boards", view);
        assertSame(pageDTO, model.getAttribute("page"));
        assertEquals("", pageDTO.getDashboardActiveClass());
        assertEquals("", pageDTO.getUsersActiveClass());
        assertEquals(" is-active", pageDTO.getBoardsActiveClass());
        verify(adminService).getBoardsPage("tips", "busan", "latest", 1);
    }

    @Test
    void del() {
        AdminService adminService = mock(AdminService.class);
        MockHttpSession session = session(3, "ADMIN");
        AdminController controller = new AdminController(adminService, session);
        SessionUser sessionUser = SessionUsers.require(session);

        String view = controller.deleteBoard(17);

        assertEquals("redirect:/admin/boards", view);
        verify(adminService).deleteBoard(sessionUser, 17);
    }

    @Test
    void del401() {
        AdminController controller = new AdminController(mock(AdminService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.deleteBoard(17));
    }

    private MockHttpSession session(Integer userId, String role) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "admin", "admin@nate.com", "010-1111-2222", role));
        return session;
    }
}
