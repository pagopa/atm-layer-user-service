package it.gov.pagopa.atmlayer.service.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@Data
public class ProfileCreationDto {
    @NotEmpty
    @Schema(format = "byte", maxLength = 255)
    private String description;
    @NotNull
    @Range(min = 1)
    @Schema(minimum = "1", maximum = "30")
    private int profileId;
}

