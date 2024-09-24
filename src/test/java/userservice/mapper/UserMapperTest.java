package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testToFrontEndDTOListPaged() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("Admin");
        profile.setCreatedAt(now);
        profile.setLastUpdatedAt(now);

        UserProfiles userProfile = new UserProfiles();
        userProfile.setProfile(profile);

        User user = new User();
        user.setUserId("user-1");
        user.setName("John");
        user.setSurname("Doe");
        user.setUserProfiles(Arrays.asList(userProfile));
        user.setCreatedAt(now);
        user.setLastUpdatedAt(now);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("Admin");
        profileDTO.setCreatedAt(now);
        profileDTO.setLastUpdatedAt(now);

        UserWithProfilesDTO expectedDTO = UserWithProfilesDTO.builder()
                .userId("user-1")
                .name("John")
                .surname("Doe")
                .profiles(Arrays.asList(profileDTO))
                .createdAt(now)
                .lastUpdatedAt(now)
                .build();

        PageInfo<User> input = new PageInfo<>(1, 10, 1, 1, Arrays.asList(user));

        PageInfo<UserWithProfilesDTO> expectedPageInfo = new PageInfo<>(1, 10, 1, 1, Arrays.asList(expectedDTO));

        PageInfo<UserWithProfilesDTO> actualPageInfo = mapper.toFrontEndDTOListPaged(input);

        assertEquals(expectedPageInfo.getPage(), actualPageInfo.getPage());
        assertEquals(expectedPageInfo.getLimit(), actualPageInfo.getLimit());
        assertEquals(expectedPageInfo.getItemsFound(), actualPageInfo.getItemsFound());
        assertEquals(expectedPageInfo.getTotalPages(), actualPageInfo.getTotalPages());
        assertEquals(expectedPageInfo.getResults().size(), actualPageInfo.getResults().size());
        for (int i = 0; i < expectedPageInfo.getResults().size(); i++) {
            assertEquals(expectedPageInfo.getResults().get(i), actualPageInfo.getResults().get(i));
        }
    }

    @Test
    void testToDTOList() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("Admin");
        profile.setCreatedAt(now);
        profile.setLastUpdatedAt(now);

        UserProfiles userProfile = new UserProfiles();
        userProfile.setProfile(profile);
        userProfile.setCreatedAt(now);
        userProfile.setLastUpdatedAt(now);

        User user1 = new User();
        user1.setUserId("user-1");
        user1.setName("John");
        user1.setSurname("Doe");
        user1.setUserProfiles(Arrays.asList(userProfile));
        user1.setCreatedAt(now);
        user1.setLastUpdatedAt(now);

        User user2 = new User();
        user2.setUserId("user-2");
        user2.setName("Jane");
        user2.setSurname("Doe");
        user2.setUserProfiles(Arrays.asList(userProfile));
        user2.setCreatedAt(now);
        user2.setLastUpdatedAt(now);

        List<User> userEntities = Arrays.asList(user1, user2);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("Admin");
        profileDTO.setCreatedAt(now);
        profileDTO.setLastUpdatedAt(now);

        List<UserWithProfilesDTO> expectedDTOs = Arrays.asList(
                UserWithProfilesDTO.builder()
                        .userId("user-1")
                        .name("John")
                        .surname("Doe")
                        .profiles(Arrays.asList(profileDTO))
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build(),
                UserWithProfilesDTO.builder()
                        .userId("user-2")
                        .name("Jane")
                        .surname("Doe")
                        .profiles(Arrays.asList(profileDTO))
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build()
        );
        List<UserWithProfilesDTO> actualDTOs = mapper.toDTOList(userEntities);

        assertEquals(expectedDTOs.size(), actualDTOs.size());
        for (int i = 0; i < expectedDTOs.size(); i++) {
            assertEquals(expectedDTOs.get(i), actualDTOs.get(i));
        }
    }


}
