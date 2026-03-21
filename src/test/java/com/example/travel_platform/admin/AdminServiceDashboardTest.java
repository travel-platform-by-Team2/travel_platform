package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminServiceDashboardTest {

    @Autowired
    private AdminService adminService;

    @Test
    void dash() {
        AdminResponse.DashboardViewDTO view = adminService.getDashboardView();

        assertEquals(3L, view.getTotalUserCount());
        assertEquals(2L, view.getActiveUserCount());
        assertEquals(1L, view.getInactiveUserCount());

        assertEquals(4, view.getMetrics().size());
        assertEquals(2, view.getUserStatusItems().size());
        assertEquals(5, view.getBoardCategoryItems().size());

        long categoryTotal = view.getBoardCategoryItems().stream()
                .map(item -> item.getCountLabel())
                .map(label -> label.replace("개", "").replace(",", "").trim())
                .mapToLong(Long::parseLong)
                .sum();

        assertEquals(view.getTotalBoardCount(), categoryTotal);

        assertTrue(view.isHasRecentUsers());
        assertEquals(3, view.getRecentUsers().size());
        assertEquals("admin", view.getRecentUsers().get(0).getUsername());

        assertTrue(view.isHasRecentBoards());
        assertEquals(3, view.getRecentBoards().size());
        assertFalse(view.getRecentBoards().get(0).getTitle().isBlank());
        assertFalse(view.getRecentBoards().get(0).getUserName().isBlank());
    }

    @Test
    void users() {
        AdminResponse.UserListViewDTO view = adminService.getUserListView(null, null, null, null);

        assertEquals(3L, view.getModel().getTotalUserCount());
        assertEquals(1L, view.getModel().getInactiveUserCount());
        assertEquals("", view.getModel().getKeyword());
        assertTrue(view.getModel().isAllTab());
        assertFalse(view.getModel().isActiveTab());
        assertFalse(view.getModel().isInactiveTab());
        assertEquals("createdAt", view.getModel().getSortBy());
        assertEquals("desc", view.getModel().getOrderBy());
        assertEquals(3, view.getModels().size());
        assertEquals("ssar", view.getModels().get(0).getUsername());
    }

    @Test
    void boardsCat() {
        AdminResponse.BoardListViewDTO view = adminService.getBoardListView("tips", "", null, 0);

        assertEquals("tips", view.getModel().getAllCategory());
        assertEquals("tips", view.getModel().getSelectCategory());
        assertFalse(view.getModel().isAllCategoryTab());
        assertTrue(view.getModel().isTips());
        assertTrue(view.getModel().getTotalPages() >= 1);
    }

    @Test
    void boardsAll() {
        AdminResponse.BoardListViewDTO view = adminService.getBoardListView(null, "", null, 0);

        assertEquals("all", view.getModel().getAllCategory());
        assertTrue(view.getModel().isAllCategoryTab());
        assertFalse(view.getModel().isTips());
    }
}
