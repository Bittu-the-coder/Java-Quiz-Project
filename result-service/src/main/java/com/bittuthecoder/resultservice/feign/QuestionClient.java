package com.bittuthecoder.resultservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "question-service")
public interface QuestionClient {

    @PostMapping("/api/questions/validate")
    Map<UUID, Boolean> validateAnswers(
            @RequestBody Map<UUID, UUID> answers
    );
}
