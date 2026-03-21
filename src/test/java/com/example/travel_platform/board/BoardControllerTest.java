package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class BoardControllerTest {

    @Test
    void list() {
        BoardService boardService = mock(BoardService.class);
        BoardController controller = new BoardController(boardService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        BoardResponse.ListViewDTO responseDTO = BoardResponse.ListViewDTO.builder()
                .model(BoardResponse.ListPageDTO.builder().build())
                .models(List.of())
                .build();

        when(boardService.getBoardList("tips", "busan", "latest", 1)).thenReturn(responseDTO);

        String view = controller.list("tips", "busan", "latest", 1, model);

        assertEquals("pages/board-list", view);
        assertSame(responseDTO.getModel(), model.getAttribute("model"));
        assertSame(responseDTO.getModels(), model.getAttribute("models"));
        verify(boardService).getBoardList("tips", "busan", "latest", 1);
    }

    @Test
    void createErr() {
        BoardService boardService = mock(BoardService.class);
        BoardController controller = new BoardController(boardService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        BoardRequest.CreateDTO reqDTO = new BoardRequest.CreateDTO();
        reqDTO.setTitle("제목");
        reqDTO.setContent("내용");
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDTO, "reqDTO");
        bindingResult.rejectValue("category", "NotBlank", "카테고리를 선택해주세요.");

        String view = controller.create(reqDTO, bindingResult, model);

        BoardResponse.FormDTO page = (BoardResponse.FormDTO) model.getAttribute("model");
        assertEquals("pages/board-create", view);
        assertEquals("제목", page.getTitle());
        assertEquals("카테고리를 선택해주세요.", page.getCategoryError());
        verifyNoInteractions(boardService);
    }

    @Test
    void detailGuest() {
        BoardService boardService = mock(BoardService.class);
        BoardController controller = new BoardController(boardService, new MockHttpSession());
        Model model = new ExtendedModelMap();
        BoardResponse.DetailDTO detailDTO = BoardResponse.DetailDTO.builder().id(3).title("detail").build();

        when(boardService.getBoardDetail(null, 3)).thenReturn(detailDTO);

        String view = controller.detail(3, model);

        assertEquals("pages/board-detail", view);
        assertSame(detailDTO, model.getAttribute("model"));
        verify(boardService).getBoardDetail(null, 3);
    }

    @Test
    void updErr() {
        BoardService boardService = mock(BoardService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(7, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        BoardController controller = new BoardController(boardService, session);
        Model model = new ExtendedModelMap();
        BoardRequest.UpdateDTO reqDTO = new BoardRequest.UpdateDTO();
        reqDTO.setCategory("tips");
        reqDTO.setContent("내용");
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDTO, "reqDTO");
        bindingResult.rejectValue("title", "NotBlank", "제목을 입력해주세요.");
        when(boardService.getBoardForm(7, 11)).thenReturn(BoardResponse.FormDTO.builder().id(11).build());

        String view = controller.update(11, reqDTO, bindingResult, model);

        BoardResponse.FormDTO page = (BoardResponse.FormDTO) model.getAttribute("model");
        assertEquals("pages/board-edit", view);
        assertEquals(11, page.getId());
        assertEquals("tips", page.getCategory());
        assertEquals("제목을 입력해주세요.", page.getTitleError());
        verify(boardService).getBoardForm(7, 11);
        verifyNoMoreInteractions(boardService);
    }
}
