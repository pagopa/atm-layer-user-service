package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class ProfileMapperImplTest {

    @Inject
    ProfileMapper mapper;

    @Test
    void toEntityTest () {
        ProfileCreationDto profileCreation = new ProfileCreationDto();
        Profile resultNull = mapper.toEntity(null);
        assertNull(resultNull, "Il risultato dovrebbe essere null quando l'input è null");

        profileCreation.setProfileId(1);
        profileCreation.setDescription("1");
        Profile result = mapper.toEntity(profileCreation);
        assertNotNull(result, "Il risultato non dovrebbe essere null");
    }

    @Test
    void toDtoTest () {
        Profile profile = new Profile();
        ProfileDTO resultNull = mapper.toDto(null);
        assertNull(resultNull, "Il risultato dovrebbe essere null quando l'input è null");

        profile.setDescription("Test Description");
        profile.setProfileId(1);

        Instant fixedInstant = Instant.parse("2024-10-01T00:00:00Z");
        profile.setCreatedAt(Timestamp.from(fixedInstant));
        profile.setLastUpdatedAt(Timestamp.from(fixedInstant));

        ProfileDTO result = mapper.toDto(profile);
        assertNotNull(result, "Il risultato non dovrebbe essere null");
    }
}
