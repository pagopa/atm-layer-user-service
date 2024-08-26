package it.gov.pagopa.atmlayer.service.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiKeyDTO {
    @Schema(format = "byte", maxLength = 100000)
    private String id;
    @Schema(format = "byte", maxLength = 100000)
    private String value;
    @Schema(format = "byte", maxLength = 100000)
    private String name;
}
