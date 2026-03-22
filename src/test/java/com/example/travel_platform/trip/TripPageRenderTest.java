package com.example.travel_platform.trip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

@SpringBootTest
class TripPageRenderTest {

    private final WebApplicationContext context;

    private MockMvc mockMvc;

    TripPageRenderTest(WebApplicationContext context) {
        this.context = context;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createPage() throws Exception {
        MockHttpSession session = session(1);

        mockMvc.perform(get("/trip/create").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("여행 계획 만들기")));
    }

    @Test
    void detailPage() throws Exception {
        MockHttpSession session = session(1);

        mockMvc.perform(get("/trip/detail").param("id", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상세 일정")));
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
