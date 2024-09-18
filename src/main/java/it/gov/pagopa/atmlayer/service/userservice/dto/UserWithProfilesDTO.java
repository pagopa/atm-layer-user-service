package it.gov.pagopa.atmlayer.service.userservice.dto;

import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserWithProfilesDTO {
    @Schema(required = true, maxLength = 255, example = "email@domain.com")
    private String userId;
    @Schema(format = "byte", maxLength = 255)
    private String name;
    @Schema(format = "byte", maxLength = 255)
    private String surname;
    @Schema(type = SchemaType.ARRAY, maxItems = 30)
    private List<ProfileDTO> profiles;
    @Schema(description = "Creation Timestamp", format = "timestamp", pattern = "DD/MM/YYYY", example = "{\"date\":\"2023-11-03T14:18:36.635+00:00\"}")
    private Timestamp createdAt;
    @Schema(description = "Last Update Timestamp", format = "timestamp", pattern = "DD/MM/YYYY", example = "{\"date\":\"2023-11-03T14:18:36.635+00:00\"}")
    private Timestamp lastUpdatedAt;
}
