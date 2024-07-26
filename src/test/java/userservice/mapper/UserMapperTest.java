package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserMapper;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class UserMapperTest {

    @Inject
    UserMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toEntityInsertionTest() {
        UserInsertionDTO userInsertionDTO = new UserInsertionDTO();
        userInsertionDTO.setUserId("prova@test.com");
        userInsertionDTO.setName("prova");
        userInsertionDTO.setSurname("test");

        User user = mapper.toEntityInsertion(userInsertionDTO);
        assertNotNull(user);
    }

    @Test
    void toEntityInsertionWithProfilesTest() {
        UserInsertionWithProfilesDTO userInsertionWithProfilesDTO = new UserInsertionWithProfilesDTO();
        userInsertionWithProfilesDTO.setUserId("prova@test.com");
        userInsertionWithProfilesDTO.setName("prova");
        userInsertionWithProfilesDTO.setSurname("test");
        List<Integer> profileIds = new ArrayList<>();
        profileIds.add(1);
        userInsertionWithProfilesDTO.setProfileIds(profileIds);

        User user = mapper.toEntityInsertionWithProfiles(userInsertionWithProfilesDTO);
        assertNotNull(user);
    }

    @Test
    void toProfilesDTOTestNotNullWithListNotNull() {
        User user = new User();
        List<UserProfiles> list = new ArrayList<>();

        Instant fixedInstant = Instant.parse("2024-10-01T00:00:00Z");
        user.setUserId("prova@test.com");
        user.setName("prova");
        user.setSurname("test");
        user.setUserProfiles(list);
        user.setCreatedAt(Timestamp.from(fixedInstant));
        user.setLastUpdatedAt(Timestamp.from(fixedInstant));

        UserWithProfilesDTO dto = mapper.toProfilesDTO(user);
        assertNotNull(dto);
    }

    @Test
    void toProfilesDTOTestNullWithListNull() {
        User user = new User();

        Instant fixedInstant = Instant.parse("2024-10-01T00:00:00Z");
        user.setUserId("prova@test.com");
        user.setName("prova");
        user.setSurname("test");
        user.setUserProfiles(null);
        user.setCreatedAt(Timestamp.from(fixedInstant));
        user.setLastUpdatedAt(Timestamp.from(fixedInstant));

        UserWithProfilesDTO dto = mapper.toProfilesDTO(user);
        assertNotNull(dto);
    }

    @Test
    void toProfilesDTOUserNull() {
        mapper.toProfilesDTO(null);
    }
}
