package it.gov.pagopa.atmlayer.service.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientCredentialsDTO {
    @Schema(format = "byte", maxLength = 100000)
    private String clientId;
    @Schema(format = "byte", maxLength = 100000)
    private String clientSecret;
    @Schema(format = "byte", maxLength = 100000)
    private String clientName;
}

