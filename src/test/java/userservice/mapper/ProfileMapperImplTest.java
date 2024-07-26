package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

@QuarkusTest
class ProfileMapperImplTest {

    @Inject
    ProfileMapper mapper;

    @Test
    void toEntityTest () {
        ProfileCreationDto profileCreation = new ProfileCreationDto();
        mapper.toEntity(null);

        profileCreation.setProfileId(1);
        profileCreation.setDescription("1");
        mapper.toEntity(profileCreation);
    }

    @Test
    void toDtoTest () {
        Profile profile = new Profile();
        mapper.toDto(null);

        profile.setDescription("Test Description");
        profile.setProfileId(1);

        Instant fixedInstant = Instant.parse("2024-10-01T00:00:00Z");
        profile.setCreatedAt(Timestamp.from(fixedInstant));
        profile.setLastUpdatedAt(Timestamp.from(fixedInstant));

        mapper.toDto(profile);
    }
}
