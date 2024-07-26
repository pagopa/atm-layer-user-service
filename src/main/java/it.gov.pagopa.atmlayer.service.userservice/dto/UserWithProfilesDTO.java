package it.gov.pagopa.atmlayer.service.userservice.dto;

import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import lombok.*;

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
    private String userId;
    private String name;
    private String surname;
    private List<ProfileDTO> profiles;
    private Timestamp createdAt;
    private Timestamp lastUpdatedAt;
}
