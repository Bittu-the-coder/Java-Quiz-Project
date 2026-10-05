package com.bittuthecoder.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchOrgResponse {

    private String token;
    private UUID orgId;
    private String orgName;
    private String orgSlug;
    private String role;
}
