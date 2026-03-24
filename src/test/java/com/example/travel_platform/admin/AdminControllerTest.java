package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
        AdminResponse.DashboardViewDTO viewDTO = AdminResponse.DashboardViewDTO.createDashboardView(
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        when(adminService.getDashboardView()).thenReturn(viewDTO);

        String view = controller.dashboard(model);

        assertEquals("pages/admin-dashboard", view);
        assertSame(viewDTO, model.getAttribute("model"));
        assertEquals(" is-active", viewDTO.getDashboardClass());
        assertEquals("", viewDTO.getUsersClass());
        assertEquals("", viewDTO.getBoardsClass());
        verify(adminService).getDashboardView();
    }

    @Test
    void users() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        AdminResponse.UserListPageDTO pageDTO = AdminResponse.UserListPageDTO.createUserListPage(
                3L,
                1L,
                "ssar",
                true,
                false,
                true,
                false,
                "postCount",
                "asc",
                "/admin/users?keyword=ssar&sortBy=postCount&orderBy=asc",
                "/admin/users?active=true&keyword=ssar&sortBy=postCount&orderBy=asc",
                "/admin/users?active=false&keyword=ssar&sortBy=postCount&orderBy=asc",
                0,
                1,
                1L,
                false,
                false,
                null,
                null,
                List.of(AdminResponse.UserPageItemDTO.createUserPageItem(0, 1, true)));
        AdminResponse.UserListViewDTO viewDTO = AdminResponse.UserListViewDTO.createUserListView(pageDTO, List.of());

        when(adminService.getUserListView(true, "ssar", "postCount", "asc", 0)).thenReturn(viewDTO);

        String view = controller.users(true, "ssar", "postCount", "asc", 0, model);

        assertEquals("pages/admin-users", view);
        assertSame(pageDTO, model.getAttribute("model"));
        assertSame(viewDTO.getModels(), model.getAttribute("models"));
        assertEquals("", pageDTO.getDashboardClass());
        assertEquals(" is-active", pageDTO.getUsersClass());
        assertEquals("", pageDTO.getBoardsClass());
        assertEquals("/admin/users?keyword=ssar&sortBy=postCount&orderBy=asc", pageDTO.getAllTabHref());
        assertEquals("/admin/users?active=true&keyword=ssar&sortBy=postCount&orderBy=asc", pageDTO.getActiveTabHref());
        assertEquals("/admin/users?active=false&keyword=ssar&sortBy=postCount&orderBy=asc", pageDTO.getInactiveTabHref());
        verify(adminService).getUserListView(true, "ssar", "postCount", "asc", 0);
    }

    @Test
    void userStatusKeepQuery() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap redirectAttributes =
                new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap();

        String view = controller.toggleUserStatus(3, false, true, " ssar ", "postCount", "asc", 2, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("true", String.valueOf(redirectAttributes.getAttribute("active")));
        assertEquals(" ssar ", redirectAttributes.getAttribute("keyword"));
        assertEquals("postCount", redirectAttributes.getAttribute("sortBy"));
        assertEquals("asc", redirectAttributes.getAttribute("orderBy"));
        assertEquals("2", String.valueOf(redirectAttributes.getAttribute("page")));
        verify(adminService).updateUserActiveStatus(3, false);
    }

    @Test
    void boards() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        AdminResponse.BoardListPageDTO pageDTO = AdminResponse.BoardListPageDTO.createBoardListPage(
                List.of(),
                1,
                1,
                0L,
                0L,
                null,
                null,
                "busan",
                "latest",
                "date",
                "desc",
                "latest",
                "tips",
                false,
                "tips",
                true,
                false,
                false,
                false,
                false);
        AdminResponse.BoardListViewDTO viewDTO = AdminResponse.BoardListViewDTO.createBoardListView(pageDTO, List.of());

        when(adminService.getBoardListView("tips", "busan", "latest", 1)).thenReturn(viewDTO);

        String view = controller.boards("tips", "busan", "latest", 1, model);

        assertEquals("pages/admin-boards", view);
        assertSame(pageDTO, model.getAttribute("model"));
        assertSame(viewDTO.getModels(), model.getAttribute("models"));
        assertEquals("", pageDTO.getDashboardClass());
        assertEquals("", pageDTO.getUsersClass());
        assertEquals(" is-active", pageDTO.getBoardsClass());
        verify(adminService).getBoardListView("tips", "busan", "latest", 1);
    }

    @Test
    void del() {
        AdminService adminService = mock(AdminService.class);
        MockHttpSession session = session(3, "ADMIN");
        AdminController controller = new AdminController(adminService, session);
        SessionUser sessionUser = SessionUsers.require(session);

        String view = controller.deleteBoard(17, null, null, null, null);

        assertEquals("redirect:/admin/boards", view);
        verify(adminService).deleteBoardByAdmin(sessionUser, 17);
    }

    @Test
    void delKeepQuery() {
        AdminService adminService = mock(AdminService.class);
        MockHttpSession session = session(3, "ADMIN");
        AdminController controller = new AdminController(adminService, session);
        SessionUser sessionUser = SessionUsers.require(session);

        String view = controller.deleteBoard(17, "tips", " busan ", "view", 1);

        assertEquals("redirect:/admin/boards?category=tips&keyword=busan&sort=view&page=1", view);
        verify(adminService).deleteBoardByAdmin(sessionUser, 17);
    }

    @Test
    void del401() {
        AdminController controller = new AdminController(mock(AdminService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.deleteBoard(17, null, null, null, null));
    }

    private MockHttpSession session(Integer userId, String role) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "admin", "admin@nate.com", "010-1111-2222", role));
        return session;
    }
}
