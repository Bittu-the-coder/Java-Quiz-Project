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
public class OrgResponse {

    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private String role;
}
