package it.gov.pagopa.atmlayer.service.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsagePlanDTO {

    @Schema(format = "byte", maxLength = 100000)
    private String id;
    @Schema(format = "byte", maxLength = 100000)
    private String name;
    @Schema(minimum = "1", maximum = "100000000")
    private Integer limit;

    private QuotaPeriodType period;
    @Schema(minimum = "1", maximum = "100000000")
    private Integer burstLimit;
    @Schema(minimum = "1", maximum = "100000000")
    private Double rateLimit;

}
