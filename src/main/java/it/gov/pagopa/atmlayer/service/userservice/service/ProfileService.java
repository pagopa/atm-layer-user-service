package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;

import java.util.List;

public interface ProfileService {
    Uni<Profile> createProfile(ProfileCreationDto profile);

    Uni<Profile> retrieveProfile(int profileId);

    Uni<Profile> updateProfile(ProfileCreationDto profile);

    Uni<Void> deleteProfile(int profileId);

    Uni<List<Profile>> getAll();
}
