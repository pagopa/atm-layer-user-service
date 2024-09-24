package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfilesPK;

import java.util.List;

public interface UserProfilesService {

    Uni<List<UserProfiles>> insertUserProfiles(UserProfilesInsertionDTO userProfilesInsertionDTO);

    Uni<UserProfiles> getById(String userId, int profileId);

    Uni<Void> deleteUserProfiles(UserProfilesPK userProfilesIDs);

    Uni<List<UserProfiles>> updateUserProfiles(UserProfilesInsertionDTO userProfilesInsertionDTO);

    Uni<Void> checkAtLeastTwoSpecificUserProfiles();
}
