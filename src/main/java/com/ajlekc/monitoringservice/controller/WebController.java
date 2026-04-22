package com.ajlekc.monitoringservice.controller;

import com.ajlekc.monitoringservice.dto.PaginationWindow;
import com.ajlekc.monitoringservice.job.DataFetchJob;
import com.ajlekc.monitoringservice.model.User;
import com.ajlekc.monitoringservice.repository.UserRepository;
import com.ajlekc.monitoringservice.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private final UserRepository userRepository;
    private final DataFetchJob dataFetchJob;
    private final PaginationService paginationService;

    @GetMapping
    public String showDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));

        PaginationWindow window = paginationService.calculateWindow(page, userPage.getTotalPages());

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("startPage", window.startPage());
        model.addAttribute("endPage", window.endPage());

        return "main";
    }

    @PostMapping("/fetch")
    public String triggerFetch(RedirectAttributes redirectAttributes) {
        dataFetchJob.execute();
        redirectAttributes.addFlashAttribute("message", "The new request was sent successfully!");
        return "redirect:/";
    }
}