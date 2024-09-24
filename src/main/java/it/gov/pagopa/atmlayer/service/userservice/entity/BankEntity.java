package it.gov.pagopa.atmlayer.service.userservice.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bank")
@Where(clause = "enabled = true")
public class BankEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "acquirer_id", nullable = false, updatable = false)
    private String acquirerId;

    @Column(name = "denomination")
    private String denomination;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "api_key_id")
    private String apiKeyId;

    @Column(name = "usage_plan_id")
    private String usagePlanId;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private Boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt;

}
