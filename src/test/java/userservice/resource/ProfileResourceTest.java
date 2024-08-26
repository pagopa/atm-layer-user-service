package userservice.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.ProfileService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProfileResourceTest {

    @InjectMock
    ProfileService profileService;

    @InjectMock
    ProfileMapper profileMapper;

    @Test
    void testCreateProfile() {
        ProfileCreationDto profileCreationDto = new ProfileCreationDto();
        profileCreationDto.setProfileId(1);
        profileCreationDto.setDescription("1");

        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("1");

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("1");

        when(profileService.createProfile(any(ProfileCreationDto.class))).thenReturn(Uni.createFrom().item(profile));
        when(profileMapper.toDto(any(Profile.class))).thenReturn(profileDTO);

        ProfileDTO result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(profileCreationDto)
                .when().post("/api/v1/user-service/profile")
                .then()
                .statusCode(200)
                .extract().as(ProfileDTO.class);

        assertEquals(profileCreationDto.getProfileId(), result.getProfileId());
        assertEquals(profileCreationDto.getDescription(), result.getDescription());
    }

    @Test
    void TestGetProfileById() {
        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("1");

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("1");

        when(profileService.retrieveProfile(1)).thenReturn(Uni.createFrom().item(profile));
        when(profileMapper.toDto(any(Profile.class))).thenReturn(profileDTO);

        ProfileDTO result = given()
                .pathParam("profileId", 1)
                .when().get("/api/v1/user-service/profile/{profileId}")
                .then()
                .statusCode(200)
                .extract().as(ProfileDTO.class);

        assertEquals(profileDTO.getProfileId(), result.getProfileId());
        assertEquals(profileDTO.getDescription(), result.getDescription());
    }

    @Test
    void testUpdateProfile() {
        ProfileCreationDto profileCreationDto = new ProfileCreationDto();
        profileCreationDto.setProfileId(1);
        profileCreationDto.setDescription("2");

        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("2");

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("2");

        when(profileService.updateProfile(any(ProfileCreationDto.class))).thenReturn(Uni.createFrom().item(profile));
        when(profileMapper.toDto(any(Profile.class))).thenReturn(profileDTO);

        ProfileDTO result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(profileCreationDto)
                .when().put("/api/v1/user-service/profile")
                .then()
                .statusCode(200)
                .extract().as(ProfileDTO.class);

        assertEquals(profileCreationDto.getProfileId(), result.getProfileId());
        assertEquals(profileCreationDto.getDescription(), result.getDescription());
    }

    @Test
    void TestDeleteProfileById() {
        Profile profile = new Profile();
        profile.setProfileId(1);
        profile.setDescription("1");

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfileId(1);
        profileDTO.setDescription("1");

        when(profileService.deleteProfile(1)).thenReturn(Uni.createFrom().voidItem());

        given()
                .pathParam("profileId", 1)
                .when().delete("/api/v1/user-service/profile/{profileId}")
                .then()
                .statusCode(204);
    }
}
