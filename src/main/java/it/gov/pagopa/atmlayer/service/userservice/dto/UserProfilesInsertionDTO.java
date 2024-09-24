package it.gov.pagopa.atmlayer.service.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserProfilesInsertionDTO {
    @NotBlank
    @Schema(required = true, maxLength = 255, example = "email@domain.com")
    private String userId;
    @NotNull
    @Size(min = 1)
    @Schema(type = SchemaType.ARRAY, maxItems = 30)
    private List<@Range(min = 1) Integer> profileIds;
}
