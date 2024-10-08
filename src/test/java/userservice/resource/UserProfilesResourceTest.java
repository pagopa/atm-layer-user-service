package userservice.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfilesPK;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserProfilesMapper;
import it.gov.pagopa.atmlayer.service.userservice.service.UserProfilesService;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserProfilesResourceTest {

    @InjectMock
    UserProfilesMapper userProfilesMapper;

    @InjectMock
    UserProfilesService userProfilesService;

    @Test
    void testInsert() {
        UserProfiles userProfiles = new UserProfiles();
        userProfiles.setUserProfilesPK(new UserProfilesPK("1", 1));
        userProfiles.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userProfiles.setLastUpdatedAt(new Timestamp(System.currentTimeMillis()));

        List<UserProfiles> userProfilesList = new ArrayList<>();
        userProfilesList.add(userProfiles);

        UserProfilesDTO userProfilesDTO = UserProfilesDTO.builder()
                .userId("1")
                .profileId(1)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        List<UserProfilesDTO> userProfilesDTOList = new ArrayList<>();
        userProfilesDTOList.add(userProfilesDTO);

        String myJson = """
                {
                    "userId": "1",
                    "profileIds": [1, 2, 3]
                }
                """;

        when(userProfilesService.insertUserProfiles(any(UserProfilesInsertionDTO.class)))
                .thenReturn(Uni.createFrom().item(userProfilesList));
        when(userProfilesMapper.toDtoList(userProfilesList))
                .thenReturn(userProfilesDTOList);

        List<UserProfilesDTO> result = given()
                .contentType(ContentType.JSON)
                .body(myJson)
                .when()
                .post("/api/v1/user-service/user_profiles/insert")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(userProfilesDTOList, result);
    }

    @Test
    void testUpdate() {
        UserProfiles userProfiles = new UserProfiles();
        userProfiles.setUserProfilesPK(new UserProfilesPK("1", 1));
        userProfiles.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userProfiles.setLastUpdatedAt(new Timestamp(System.currentTimeMillis()));

        List<UserProfiles> userProfilesList = new ArrayList<>();
        userProfilesList.add(userProfiles);

        UserProfilesDTO userProfilesDTO = UserProfilesDTO.builder()
                .userId("1")
                .profileId(1)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .lastUpdatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        List<UserProfilesDTO> userProfilesDTOList = new ArrayList<>();
        userProfilesDTOList.add(userProfilesDTO);

        String myJson = """
                {
                    "userId": "1",
                    "profileIds": [1, 2, 3]
                }
                """;

        when(userProfilesService.updateUserProfiles(any(UserProfilesInsertionDTO.class)))
                .thenReturn(Uni.createFrom().item(userProfilesList));
        when(userProfilesMapper.toDtoList(userProfilesList))
                .thenReturn(userProfilesDTOList);

        List<UserProfilesDTO> result = given()
                .contentType(ContentType.JSON)
                .body(myJson)
                .when()
                .put("/api/v1/user-service/user_profiles/update")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(userProfilesDTOList, result);
    }

}
