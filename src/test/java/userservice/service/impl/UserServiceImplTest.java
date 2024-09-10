package userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfilesPK;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.repository.UserRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.UserProfilesService;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    UserProfilesService userProfilesService;

    @InjectMocks
    UserServiceImpl userServiceImpl;

    private User user;
    private UserProfiles userProfiles;
    private List<UserProfiles> userProfilesList;
    private UserInsertionDTO userInsertionDTO;
    private UserInsertionWithProfilesDTO userInsertionWithProfilesDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        userProfiles = new UserProfiles();
        userProfiles.setUserProfilesPK(new UserProfilesPK("prova@test.com", 1));
        userProfiles.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userProfiles.setLastUpdatedAt(new Timestamp(System.currentTimeMillis()));

        userProfilesList = new ArrayList<>();
        userProfilesList.add(userProfiles);

        user.setUserId("prova@test.com");
        user.setName("prova");
        user.setSurname("test");
        user.setUserProfiles(userProfilesList);

        userInsertionDTO = new UserInsertionDTO();
        userInsertionDTO.setUserId("prova@test.com");
        userInsertionDTO.setName("prova");
        userInsertionDTO.setSurname("test");

        userInsertionWithProfilesDTO = new UserInsertionWithProfilesDTO();
        userInsertionWithProfilesDTO.setUserId("prova@test.com");
        userInsertionWithProfilesDTO.setName("prova");
        userInsertionWithProfilesDTO.setSurname("test");
        userInsertionWithProfilesDTO.setProfileIds(List.of(1));
    }

    @Test
    void testGetUserFiltered() {
        List<User> usersList = new ArrayList<>();
        User user = new User();
        usersList.add(user);
        int pageIndex = 0;
        int pageSize = 10;
        String name = "John";
        String surname = "Doe";
        String userId = "123";
        int profileId = 1;

        PageInfo<User> expectedResult = new PageInfo<>(0, 10, 1, 1, usersList);

        when(userRepository.findByFilters(anyMap(), eq(pageIndex), eq(pageSize))).thenReturn(Uni.createFrom().item(expectedResult));

        Uni<PageInfo<User>> result = userServiceImpl.getUserFiltered(pageIndex, pageSize, name, surname, userId, profileId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expectedResult);
    }

    @Test
    void testGetUserFilteredWithNullOrEmptyFilters() {
        int pageIndex = 0;
        int pageSize = 10;
        String name = "John";
        String surname = "Doe";
        String userId = "123";
        int profileId = 1;

        assertDoesNotThrow(() -> userServiceImpl.getUserFiltered(pageIndex, pageSize, name, surname, null, profileId).await().indefinitely());
        assertDoesNotThrow(() -> userServiceImpl.getUserFiltered(pageIndex, pageSize, null, surname, userId, profileId).await().indefinitely());
        assertDoesNotThrow(() -> userServiceImpl.getUserFiltered(pageIndex, pageSize, null, null, null, profileId).await().indefinitely());
    }

    @Test
    void testInsertUserOK() {
        User user = new User();
        user.setUserId("prova@test.com");
        String userId = user.getUserId();
        UserInsertionDTO userInsertionDTO = new UserInsertionDTO();
        userInsertionDTO.setUserId("prova@test.com");
        userInsertionDTO.setName("prova");
        userInsertionDTO.setSurname("test");

        when(userMapper.toEntityInsertion(any(UserInsertionDTO.class))).thenReturn(user);
        when(userRepository.findById(user.getUserId())).thenReturn(Uni.createFrom().nullItem());
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.insertUser(userInsertionDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(user);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).persist(user);
    }

    @Test
    void findUserTest() {
        userServiceImpl.findUser(user.getUserId())
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        verify(userRepository, times(1)).findByIdCustom(user.getUserId());
    }

    @Test
    void insertUserWithProfilesTestWithInitialUserNotFound() {
        when(userMapper.toEntityInsertionWithProfiles(any(UserInsertionWithProfilesDTO.class))).thenReturn(user);
        when(userRepository.findById(user.getUserId())).thenReturn(Uni.createFrom().nullItem());
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        User result = userServiceImpl.insertUserWithProfiles(userInsertionWithProfilesDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        Assertions.assertNotNull(result);
        assertEquals(user, result);

        verify(userMapper, times(1)).toEntityInsertionWithProfiles(any(UserInsertionWithProfilesDTO.class));
        verify(userRepository, times(1)).findById(user.getUserId());
        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    void insertUserWithProfilesTestWithInitialUserFound() {
        when(userMapper.toEntityInsertionWithProfiles(any(UserInsertionWithProfilesDTO.class))).thenReturn(user);
        when(userRepository.findById(user.getUserId())).thenReturn(Uni.createFrom().item(user));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.insertUserWithProfiles(userInsertionWithProfilesDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(AtmLayerException.class, "Esiste già un utente associato all'indirizzo email indicato");
    }

    @Test
    void testInsertUserExceptionCase() {

        UserInsertionDTO userInsertionDTO = new UserInsertionDTO();
        userInsertionDTO.setUserId("prova@test.com");
        userInsertionDTO.setName("prova");
        userInsertionDTO.setSurname("test");
        User user = new User();
        user.setUserId(userInsertionDTO.getUserId());
        user.setName(userInsertionDTO.getName());
        user.setSurname(userInsertionDTO.getSurname());

        when(userMapper.toEntityInsertion(any(UserInsertionDTO.class))).thenReturn(user);
        when(userRepository.findById("prova@test.com")).thenReturn(Uni.createFrom().item(user));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.insertUser(userInsertionDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .assertFailedWith(AtmLayerException.class, "Esiste già un utente associato all'indirizzo email indicato");

        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUserId("Paolo@Rossi.com");

        when(userServiceImpl.getById(any(String.class))).thenReturn(Uni.createFrom().item(user));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.updateUser("Paolo@Rossi.com", "Paolo", "Rossi").subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(user);

        verify(userRepository).persist(user);
    }

    @Test
    void testUpdateUserSuccessPartialNameOnly() {
        User user = new User();
        user.setUserId("Paolo@Rossi.com");

        when(userServiceImpl.getById(any(String.class))).thenReturn(Uni.createFrom().item(user));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.updateUser("Paolo@Rossi.com", "Paolo", "Rossi").subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(user);

        verify(userRepository).persist(user);
    }

    @Test
    void testUpdateUserSuccessPartialSurnameOnly() {
        User user = new User();
        user.setUserId("Paolo@Rossi.com");

        when(userServiceImpl.getById(any(String.class))).thenReturn(Uni.createFrom().item(user));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.updateUser("Paolo@Rossi.com", "Paolo", "Rossi").subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(user);

    }

    @Test
    void testUpdateWithProfilesFailOnUserProfilesUpdate() {
        when(userProfilesService.updateUserProfiles(any())).thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
        when(userMapper.toEntityInsertionWithProfiles(any(UserInsertionWithProfilesDTO.class))).thenReturn(user);

        Uni<User> result = userServiceImpl.updateWithProfiles(userInsertionWithProfilesDTO);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(RuntimeException.class, "Error");

        verify(userProfilesService).updateUserProfiles(any());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void testGetById() {
        String userId = "existentId";
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByIdCustom(userId)).thenReturn(Uni.createFrom().item(user));

        userServiceImpl.getById(userId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(user);

        verify(userRepository).findByIdCustom(userId);
    }

    @Test
    void testFindByIdExceptionCase() {
        String userId = "nonExistentId";

        when(userRepository.findById(userId)).thenReturn(Uni.createFrom().nullItem());

        userServiceImpl.getById(userId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .assertFailedWith(AtmLayerException.class, "Nessun utente trovato per l'id selezionato");
    }

    @Test
    void testGetAllUsers() {
        List<User> userList = new ArrayList<>();
        User user = new User();
        userList.add(user);

        PanacheQuery<User> panacheQuery = mock(PanacheQuery.class);

        when(userRepository.findAllCustom()).thenReturn(panacheQuery);
        when(panacheQuery.list()).thenReturn(Uni.createFrom().item(userList));

        Uni<List<User>> result = userServiceImpl.getAllUsers();

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(userList);
    }

    @Test
    void testDeleteOK() {
        String userId = "testUserId";
        User user = new User();

        when(userServiceImpl.getById(userId)).thenReturn(Uni.createFrom().item(user));
        when(userRepository.deleteById(userId)).thenReturn(Uni.createFrom().item(true));

        userServiceImpl.deleteUser(userId)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(true);
    }

    @Test
    void testCheckFirstAccessWhenNoUsers() {

        long userCount = 0;

        when(userRepository.count()).thenReturn(Uni.createFrom().item(userCount));

        UniAssertSubscriber<Void> subscriber = userServiceImpl.checkFirstAccess("test@test.com")
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertFailedWith(AtmLayerException.class);

        verify(userRepository, times(1)).count();
    }




    @Test
    void testCheckFirstAccessWhenUsersExist() {
        String userId = "testUserId";
        long userCount = 5;

        when(userRepository.count()).thenReturn(Uni.createFrom().item(userCount));

        userServiceImpl.checkFirstAccess(userId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(null);

        verify(userRepository, times(1)).count();
        verify(userProfilesService, never()).insertUserProfiles(any(UserProfilesInsertionDTO.class));
    }

}
