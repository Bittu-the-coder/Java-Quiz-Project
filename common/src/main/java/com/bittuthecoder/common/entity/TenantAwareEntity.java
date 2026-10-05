package com.bittuthecoder.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Base mapped superclass for any entity partitioned by organization (tenant).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantAwareEntity extends BaseAuditEntity {

    @Column(name = "org_id", nullable = false)
    private UUID orgId;
}
