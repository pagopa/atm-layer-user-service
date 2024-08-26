package it.gov.pagopa.atmlayer.service.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserInsertionDTO {
    @NotBlank
    @Email(message = "must be an email address in the correct format")
    @Schema(required = true, maxLength = 255, example = "email@domain.com")
    private String userId;
    @Schema(format = "byte", maxLength = 255)
    private String name;
    @Schema(format = "byte", maxLength = 255)
    private String surname;
}
