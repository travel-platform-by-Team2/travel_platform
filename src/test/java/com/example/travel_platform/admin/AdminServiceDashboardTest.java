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
    void getDashboardPage_loadsMetricsChartsAndRecentLists() {
        AdminResponse.DashboardPageDTO page = adminService.getDashboardPage();

        assertEquals(3L, page.getTotalUserCount());
        assertEquals(2L, page.getActiveUserCount());
        assertEquals(1L, page.getInactiveUserCount());

        assertEquals(4, page.getMetrics().size());
        assertEquals(2, page.getUserStatusItems().size());
        assertEquals(5, page.getBoardCategoryItems().size());

        long categoryTotal = page.getBoardCategoryItems().stream()
                .map(item -> item.getCountLabel())
                .map(label -> label.replace("개", "").replace(",", "").trim())
                .mapToLong(label -> Long.parseLong(label))
                .sum();

        assertEquals(page.getTotalBoardCount(), categoryTotal);

        assertTrue(page.isHasRecentUsers());
        assertEquals(3, page.getRecentUsers().size());
        assertEquals("admin", page.getRecentUsers().get(0).getUsername());

        assertTrue(page.isHasRecentBoards());
        assertEquals(3, page.getRecentBoards().size());
        assertFalse(page.getRecentBoards().get(0).getTitle().isBlank());
        assertFalse(page.getRecentBoards().get(0).getUserName().isBlank());
    }

    @Test
    void getUsersPage_loadsUserListAndTabState() {
        AdminResponse.UserListPageDTO page = adminService.getUsersPage(null, null);

        assertEquals(3L, page.getTotalUserCount());
        assertEquals(1L, page.getInactiveUserCount());
        assertEquals("", page.getKeyword());
        assertTrue(page.isAllTab());
        assertFalse(page.isActiveTab());
        assertFalse(page.isInactiveTab());
        assertEquals(3, page.getUsers().size());
        assertEquals("admin", page.getUsers().get(0).getUsername());
    }

    @Test
    void getBoardsPage_setsSelectedCategoryForPagingLinks() {
        AdminResponse.AdminBoardListDTO page = adminService.getBoardsPage("tips", "", 0);

        assertEquals("tips", page.getAllCategory());
        assertEquals("tips", page.getSelectCategory());
        assertTrue(page.isTips());
        assertTrue(page.getTotalPages() >= 1);
    }
}
