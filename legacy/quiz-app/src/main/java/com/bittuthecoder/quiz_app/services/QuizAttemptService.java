package com.bittuthecoder.quiz_app.services;


import com.bittuthecoder.quiz_app.dtos.QuizResultResponse;
import com.bittuthecoder.quiz_app.dtos.StartQuizRequest;
import com.bittuthecoder.quiz_app.dtos.SubmitQuizRequest;

import java.util.UUID;

public interface QuizAttemptService {

    UUID startQuiz(StartQuizRequest request);

    QuizResultResponse submitQuiz(SubmitQuizRequest request);
}
