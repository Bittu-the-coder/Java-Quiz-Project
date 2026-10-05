package com.bittuthecoder.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Unified API Error response structure across all Assessify services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @Builder.Default
    private Instant timestamp = Instant.now();

    private int status;

    private String error;

    private String message;

    private String path;

    private Map<String, String> validationErrors;
}
