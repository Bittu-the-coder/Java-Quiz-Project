package com.bittuthecoder.resultservice.controller;

import com.bittuthecoder.resultservice.dto.ResultResponse;
import com.bittuthecoder.resultservice.dto.SubmitQuizRequest;
import com.bittuthecoder.resultservice.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    // STUDENT ONLY (Gateway enforced)
    @PostMapping("/submit")
    public ResultResponse submitQuiz(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody SubmitQuizRequest request
    ) {
        System.out.println("EMAIL = " + email);
        System.out.println("ROLE = " + role);
        System.out.println("REQUEST = " + request);

        if (email == null || role == null) {
            throw new RuntimeException("Headers missing from Gateway");
        }

        if (!role.equals("STUDENT")) {
            throw new RuntimeException("Only students can submit quizzes");
        }

        return resultService.submitQuiz(request, email);
    }

}
