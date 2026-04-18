package com.ajlekc.monitoringservice.service;

import com.ajlekc.monitoringservice.dto.PaginationWindow;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {

    private static final int DEFAULT_WINDOW_SIZE = 7;

    public PaginationWindow calculateWindow(int currentPage, int totalPages) {
        if (totalPages <= 0) {
            return new PaginationWindow(0, 0);
        }

        int startPage = Math.max(0, currentPage - DEFAULT_WINDOW_SIZE / 2);
        int endPage = Math.min(totalPages - 1, currentPage + DEFAULT_WINDOW_SIZE / 2);

        if (endPage - startPage + 1 < DEFAULT_WINDOW_SIZE) {
            if (startPage == 0) {
                endPage = Math.min(totalPages - 1, startPage + DEFAULT_WINDOW_SIZE - 1);
            } else if (endPage == totalPages - 1) {
                startPage = Math.max(0, endPage - DEFAULT_WINDOW_SIZE + 1);
            }
        }

        return new PaginationWindow(startPage, endPage);
    }
}