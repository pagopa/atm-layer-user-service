package it.gov.pagopa.atmlayer.service.userservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

@Data
@NoArgsConstructor
public class BankInsertionDTO {

    @NotBlank
    @Schema(format = "byte", maxLength = 255)
    private String acquirerId;
    @NotBlank
    @Schema(format = "byte", maxLength = 255)
    private String denomination;
    @Schema(minimum = "1", maximum = "100000000")
    @Min(1)
    private Integer limit;

    private QuotaPeriodType period;

    @Schema(minimum = "1", maximum = "100000000")
    @Min(1)
    private Integer burstLimit;

    @Schema(minimum = "1", maximum = "100000000")
    @Min(1)
    private Double rateLimit;

}
