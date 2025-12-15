package com.bittuthecoder.resultservice.service;

import com.bittuthecoder.resultservice.dto.ResultResponse;
import com.bittuthecoder.resultservice.dto.SubmitQuizRequest;

public interface ResultService {

    ResultResponse submitQuiz(
            SubmitQuizRequest request,
            String studentEmail
    );
}
