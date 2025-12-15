package com.bittuthecoder.question_service.service.impl;

import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.QuestionRepository;
import com.bittuthecoder.question_service.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public Question createQuestion(Question question) {
        question.getOptions().forEach(opt -> opt.setQuestion(question));
        return questionRepository.save(question);
    }


    @Override
    public List<Question> getQuestionsByQuiz(UUID quizId) {
        return questionRepository.findByQuizId(quizId);
    }
}
