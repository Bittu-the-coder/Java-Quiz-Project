package com.bittuthecoder.authservice.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchOrgRequest {

    @NotNull(message = "Target orgId is required")
    private UUID orgId;
}
