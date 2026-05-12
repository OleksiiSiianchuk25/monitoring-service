package com.ajlekc.monitoringservice.controller;

import com.ajlekc.monitoringservice.dto.PaginationWindow;
import com.ajlekc.monitoringservice.job.DataFetchJob;
import com.ajlekc.monitoringservice.repository.JobRunRepository;
import com.ajlekc.monitoringservice.repository.UserRepository;
import com.ajlekc.monitoringservice.service.PaginationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRunRepository jobRunRepository;
    @Mock
    private DataFetchJob dataFetchJob;
    @Mock
    private PaginationService paginationService;

    @InjectMocks
    private WebController webController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    void dashboard_returnsMainView_withAttributes() throws Exception {
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Page.empty());

        when(jobRunRepository.findAllByOrderByStartedAtDesc(any()))
                .thenReturn(Page.empty());

        when(paginationService.calculateWindow(anyInt(), anyInt()))
                .thenReturn(new PaginationWindow(0, 0));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("users", "currentPage", "totalPages"));
    }

    @Test
    void fetch_triggersJob_andRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/fetch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}