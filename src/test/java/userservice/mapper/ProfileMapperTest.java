package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
class ProfileMapperTest {
    @Inject
    ProfileMapper mapper;
    @Test
    void toDtoListTest () {
        List<Profile> profileList = new ArrayList<>();
        mapper.toDTOList(profileList);
    }
}
