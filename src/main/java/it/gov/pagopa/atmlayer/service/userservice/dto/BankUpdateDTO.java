package it.gov.pagopa.atmlayer.service.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

@Data
@NoArgsConstructor
public class BankUpdateDTO {
    @NotBlank
    private String acquirerId;

    private Integer limit;

    private QuotaPeriodType period;

    private Integer burstLimit;

    private Double rateLimit;
}
