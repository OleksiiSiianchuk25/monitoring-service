package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.dto.PaginationWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationServiceTest {

    private final PaginationService paginationService = new PaginationService();

    @Test
    void shouldReturnZeroWindowWhenTotalPagesIsZero() {
        PaginationWindow window = paginationService.calculateWindow(0, 0);

        assertEquals(0, window.startPage());
        assertEquals(0, window.endPage());
    }

    @Test
    void shouldReturnFullWindowWhenTotalPagesLessThanWindowSize() {
        PaginationWindow window = paginationService.calculateWindow(2, 4);

        assertEquals(0, window.startPage());
        assertEquals(3, window.endPage());
    }

    @Test
    void shouldShiftWindowWhenAtTheBeginning() {
        PaginationWindow window = paginationService.calculateWindow(1, 20);

        assertEquals(0, window.startPage());
        assertEquals(6, window.endPage());
    }

    @Test
    void shouldShiftWindowWhenAtTheEnd() {
        PaginationWindow window = paginationService.calculateWindow(19, 20);

        assertEquals(13, window.startPage());
        assertEquals(19, window.endPage());
    }

    @Test
    void shouldKeepCurrentPageInMiddleWhenInTheMiddleOfDataset() {
        PaginationWindow window = paginationService.calculateWindow(10, 20);

        assertEquals(7, window.startPage());
        assertEquals(13, window.endPage());
    }
}