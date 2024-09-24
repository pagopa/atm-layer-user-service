package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ProfileMapperTest {
    @Inject
    ProfileMapper mapper;

    @Test
    void testToDtoList() {
        List<Profile> profileList = new ArrayList<>();
        List<ProfileDTO> dtoList = mapper.toDTOList(profileList);
        assertTrue(dtoList.isEmpty(), "La lista DTO dovrebbe essere vuota");
    }
}
