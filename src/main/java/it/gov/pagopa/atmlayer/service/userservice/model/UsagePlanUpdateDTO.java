package it.gov.pagopa.atmlayer.service.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsagePlanUpdateDTO {
    @Schema(minimum = "1", maximum = "100000000")
    private Double rateLimit;
    @Schema(minimum = "1", maximum = "100000000")
    private Integer quotaLimit;

    private QuotaPeriodType quotaPeriod;
}
