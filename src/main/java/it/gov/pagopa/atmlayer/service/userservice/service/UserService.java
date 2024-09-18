package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface UserService {

    Uni<User> insertUser(UserInsertionDTO userInsertionDTO);

    Uni<User> insertUserWithProfiles(UserInsertionWithProfilesDTO userInsertionWithProfilesDTO);

    Uni<User> findUser(String userId);

    Uni<User> updateUser(@NotBlank String userId, @NotBlank String name, @NotBlank String surname);

    Uni<User> updateWithProfiles(UserInsertionWithProfilesDTO userInsertionWithProfilesDTO);

    Uni<Boolean> deleteUser(String userId);

    Uni<User> getById(String userId);

    Uni<List<User>> getAllUsers();

    Uni<PageInfo<User>> getUserFiltered(int pageIndex, int pageSize, String name, String surname, String userId, int profileId);

    Uni<Long> countUsers();

    Uni<Void> checkFirstAccess(String userId);
}
