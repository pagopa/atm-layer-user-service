package userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.repository.BankRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.BankServiceImpl;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class BankServiceImplTest {

    @Mock
    BankMapper bankMapper;
    @Mock
    BankRepository bankRepository;
    @Mock
    CognitoService cognitoService;
    @Mock
    ApiKeyService apiKeyService;
    @InjectMocks
    BankServiceImpl bankService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInsertBankSuccess() {
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        BankEntity bankEntity = new BankEntity();
        ClientCredentialsDTO clientCredentialsDTO;
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO("api-key-id", "api-key-value", "Test API Key");
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();

        bankInsertionDTO.setAcquirerId("123");
        bankInsertionDTO.setDenomination("Test Denomination");

        bankEntity.setAcquirerId("123");
        bankEntity.setDenomination("Test Denomination");

        clientCredentialsDTO = new ClientCredentialsDTO();
        clientCredentialsDTO.setClientId("client-id");
        clientCredentialsDTO.setClientSecret("client-secret");
        clientCredentialsDTO.setClientName("Test Client");

        usagePlanDTO.setId("usage-plan-id");

        when(bankRepository.findAllById(any(String.class)))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));
        when(cognitoService.generateClient(any(String.class)))
                .thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(apiKeyService.createApiKey(any(String.class)))
                .thenReturn(Uni.createFrom().item(apiKeyDTO));
        when(apiKeyService.createUsagePlan(any(BankInsertionDTO.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(usagePlanDTO));
        when(bankMapper.toEntityInsertion(any(BankInsertionDTO.class)))
                .thenReturn(bankEntity);
        when(bankRepository.persist(any(BankEntity.class)))
                .thenReturn(Uni.createFrom().item(bankEntity));
        when(bankMapper.toPresentationDTO(any(BankEntity.class), any(ApiKeyDTO.class), any(ClientCredentialsDTO.class), any(UsagePlanDTO.class)))
                .thenReturn(new BankPresentationDTO());

        Uni<BankPresentationDTO> result = bankService.insertBank(bankInsertionDTO);

        assertDoesNotThrow(() -> result.await().indefinitely());
        verify(bankRepository).findAllById(bankInsertionDTO.getAcquirerId());
        verify(cognitoService).generateClient(bankInsertionDTO.getDenomination());
        verify(apiKeyService).createApiKey(clientCredentialsDTO.getClientName());
        verify(apiKeyService).createUsagePlan(bankInsertionDTO, apiKeyDTO.getId());
        verify(bankRepository).persist(any(BankEntity.class));
    }

    @Test
    void testInsertBank_existingBank() {
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        BankEntity bankEntity = new BankEntity();
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();

        bankInsertionDTO.setAcquirerId("123");
        bankInsertionDTO.setDenomination("Test Denomination");

        bankEntity.setAcquirerId("123");
        bankEntity.setDenomination("Test Denomination");

        clientCredentialsDTO.setClientId("client-id");
        clientCredentialsDTO.setClientSecret("client-secret");
        clientCredentialsDTO.setClientName("Test Client");

        usagePlanDTO.setId("usage-plan-id");

        bankEntity.setEnabled(true);
        when(bankRepository.findAllById(any(String.class)))
                .thenReturn(Uni.createFrom().item(Collections.singletonList(bankEntity)));

        AtmLayerException thrown = assertThrows(AtmLayerException.class, () -> bankService.insertBank(bankInsertionDTO).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), thrown.getStatusCode(), "The response status should be 400 Bad Request");
        assertEquals(AppErrorCodeEnum.BANK_WITH_THE_SAME_ID_ALREADY_EXISTS.getErrorCode(), thrown.getErrorCode(), "The error code should be BANK_WITH_THE_SAME_ID_ALREADY_EXISTS");

        verify(bankRepository).findAllById(bankInsertionDTO.getAcquirerId());
        verify(cognitoService, never()).generateClient(any(String.class));
        verify(apiKeyService, never()).createApiKey(any(String.class));
        verify(apiKeyService, never()).createUsagePlan(any(BankInsertionDTO.class), any(String.class));
        verify(bankRepository, never()).persist(any(BankEntity.class));
    }

    @Test
    void testInsertBank_disabledBank() {
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setAcquirerId("123");
        bankInsertionDTO.setDenomination("Test Denomination");
        BankEntity bankEntity = new BankEntity();
        bankEntity.setAcquirerId("123");
        bankEntity.setDenomination("Test Denomination");
        bankEntity.setEnabled(false);
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        clientCredentialsDTO.setClientId("client-id");
        clientCredentialsDTO.setClientSecret("client-secret");
        clientCredentialsDTO.setClientName("Test Client");
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId("usage-plan-id");
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO("api-key-id", "api-key-value", "Test API Key");

        when(bankRepository.findAllById(any(String.class)))
                .thenReturn(Uni.createFrom().item(Collections.singletonList(bankEntity)));
        when(cognitoService.generateClient(any(String.class)))
                .thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(apiKeyService.createApiKey(any(String.class)))
                .thenReturn(Uni.createFrom().item(apiKeyDTO));
        when(apiKeyService.createUsagePlan(any(BankInsertionDTO.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(usagePlanDTO));
        when(bankMapper.toEntityInsertion(any(BankInsertionDTO.class)))
                .thenReturn(bankEntity);
        when(bankRepository.persist(any(BankEntity.class)))
                .thenReturn(Uni.createFrom().item(bankEntity));
        when(bankMapper.toPresentationDTO(any(BankEntity.class), any(ApiKeyDTO.class), any(ClientCredentialsDTO.class), any(UsagePlanDTO.class)))
                .thenReturn(new BankPresentationDTO());

        bankService.insertBank(bankInsertionDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
    }

    @Test
    void testInsertBank_rollback(){
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setAcquirerId("123");
        bankInsertionDTO.setDenomination("Test Denomination");
        BankEntity bankEntity = new BankEntity();
        bankEntity.setAcquirerId("123");
        bankEntity.setDenomination("Test Denomination");
        ClientCredentialsDTO clientCredentialsDTO;
        clientCredentialsDTO = new ClientCredentialsDTO();
        clientCredentialsDTO.setClientId("client-id");
        clientCredentialsDTO.setClientSecret("client-secret");
        clientCredentialsDTO.setClientName("Test Client");
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO("api-key-id", "api-key-value", "Test API Key");
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId("usage-plan-id");

        when(bankRepository.findAllById(any(String.class)))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));
        when(cognitoService.generateClient(any(String.class)))
                .thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(apiKeyService.createApiKey(any(String.class)))
                .thenReturn(Uni.createFrom().item(apiKeyDTO));
        when(apiKeyService.createUsagePlan(any(BankInsertionDTO.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(usagePlanDTO));
        when(bankMapper.toEntityInsertion(any(BankInsertionDTO.class)))
                .thenReturn(bankEntity);
        when(bankRepository.persist(any(BankEntity.class)))
                .thenThrow(new RuntimeException("something went wrong"));

        bankService.insertBank(bankInsertionDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(AtmLayerException.class, "something went wrong");
    }

    @Test
    void testUpdateBankSuccess_sameName() {
        BankUpdateDTO input = new BankUpdateDTO();
        input.setAcquirerId("test-acquirer-id");
        input.setDenomination("Denomination");
        input.setRateLimit(100.0);
        input.setBurstLimit(50);
        input.setLimit(1000);
        input.setPeriod(QuotaPeriodType.MONTH);

        BankEntity bankEntity = new BankEntity();
        bankEntity.setDenomination("Denomination");
        bankEntity.setClientId("client-id");
        bankEntity.setApiKeyId("api-key-id");
        bankEntity.setUsagePlanId("usage-plan-id");

        UsagePlanDTO usagePlanDto = new UsagePlanDTO();
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(bankRepository.findById(input.getAcquirerId())).thenReturn(Uni.createFrom().item(bankEntity));
        when(apiKeyService.updateUsagePlan(anyString(), any())).thenReturn(Uni.createFrom().item(usagePlanDto));
        when(apiKeyService.getApiKey(anyString())).thenReturn(Uni.createFrom().item(new ApiKeyDTO()));
        when(cognitoService.getClientCredentials(anyString())).thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(bankMapper.toPresentationDTO(any(), any(), any(), any())).thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.updateBank(input);
        BankPresentationDTO result = resultUni.await().indefinitely();

        assertNotNull(result, "The result should not be null");
        assertEquals(bankPresentationDTO, result, "The result should match the expected BankPresentationDTO");

        verify(bankRepository).findById(input.getAcquirerId());
        verify(apiKeyService).updateUsagePlan(anyString(), any());
        verify(apiKeyService).getApiKey(anyString());
        verify(bankMapper).toPresentationDTO(any(), any(), any(), any());
    }

    @Test
    void testUpdateBankSuccess_differentName() {
        BankUpdateDTO input = new BankUpdateDTO();
        input.setAcquirerId("test-acquirer-id");
        input.setDenomination("New Denomination");
        input.setRateLimit(100.0);
        input.setBurstLimit(50);
        input.setLimit(1000);
        input.setPeriod(QuotaPeriodType.MONTH);

        BankEntity bankEntity = new BankEntity();
        bankEntity.setDenomination("Old Denomination");
        bankEntity.setClientId("client-id");
        bankEntity.setApiKeyId("api-key-id");
        bankEntity.setUsagePlanId("usage-plan-id");

        UsagePlanDTO usagePlanDto = new UsagePlanDTO();
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(bankRepository.findById(input.getAcquirerId())).thenReturn(Uni.createFrom().item(bankEntity));
        when(bankRepository.persist(any(BankEntity.class))).thenReturn(Uni.createFrom().item(bankEntity));
        when(apiKeyService.updateUsagePlan(anyString(), any())).thenReturn(Uni.createFrom().item(usagePlanDto));
        when(cognitoService.updateClientName(anyString(), anyString())).thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(apiKeyService.getApiKey(anyString())).thenReturn(Uni.createFrom().item(new ApiKeyDTO()));
        when(cognitoService.getClientCredentials(anyString())).thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(bankMapper.toPresentationDTO(any(), any(), any(), any())).thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.updateBank(input);
        BankPresentationDTO result = resultUni.await().indefinitely();

        assertNotNull(result, "The result should not be null");
        assertEquals(bankPresentationDTO, result, "The result should match the expected BankPresentationDTO");

        verify(bankRepository).findById(input.getAcquirerId());
        verify(bankRepository).persist(any(BankEntity.class));
        verify(apiKeyService).updateUsagePlan(anyString(), any());
        verify(cognitoService).updateClientName(anyString(), anyString());
        verify(apiKeyService).getApiKey(anyString());
        verify(bankMapper).toPresentationDTO(any(), any(), any(), any());

        verifyNoMoreInteractions(bankRepository, apiKeyService, cognitoService, bankMapper);
    }

    @Test
    void testUpdateBank_rollback() {
        BankUpdateDTO input = new BankUpdateDTO();
        input.setAcquirerId("test-acquirer-id");
        input.setDenomination("New Denomination");
        input.setRateLimit(100.0);
        input.setBurstLimit(50);
        input.setLimit(1000);
        input.setPeriod(QuotaPeriodType.MONTH);

        BankEntity bankEntity = new BankEntity();
        bankEntity.setDenomination("Old Denomination");
        bankEntity.setClientId("client-id");
        bankEntity.setApiKeyId("api-key-id");
        bankEntity.setUsagePlanId("usage-plan-id");

        when(bankRepository.findById(input.getAcquirerId())).thenReturn(Uni.createFrom().item(bankEntity));
        when(bankRepository.persist(any(BankEntity.class))).thenReturn(Uni.createFrom().item(bankEntity));

        when(apiKeyService.updateUsagePlan(anyString(), any())).thenThrow(new RuntimeException("something went wrong"));

        when(cognitoService.updateClientName(anyString(), anyString())).thenReturn(Uni.createFrom().item(new ClientCredentialsDTO()));
        when(apiKeyService.getApiKey(anyString())).thenReturn(Uni.createFrom().item(new ApiKeyDTO()));
        when(cognitoService.getClientCredentials(anyString())).thenReturn(Uni.createFrom().item(new ClientCredentialsDTO()));
        when(bankMapper.toPresentationDTO(any(), any(), any(), any())).thenReturn(new BankPresentationDTO());

        bankService.updateBank(input)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(AtmLayerException.class, "something went wrong");
    }

    @Test
    void testUpdateBank_bankNotFound() {
        when(bankRepository.findById(any(String.class))).thenReturn(Uni.createFrom().nullItem());
        BankUpdateDTO bankUpdateDTO = new BankUpdateDTO();
        bankUpdateDTO.setAcquirerId("acquirerId");
        bankService.updateBank(bankUpdateDTO)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(AtmLayerException.class, AppErrorCodeEnum.BANK_NOT_FOUND.getErrorMessage());
    }

    @Test
    void testFindByAcquirerId_BankEntityNotFound() {
        String acquirerId = "non-existent-id";

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().nullItem());

        Uni<BankPresentationDTO> resultUni = bankService.findByAcquirerId(acquirerId);

        AtmLayerException thrownException = assertThrows(AtmLayerException.class, () -> resultUni.await().indefinitely());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), thrownException.getStatusCode(), "The response status should be 404 Not Found");
        assertEquals(AppErrorCodeEnum.BANK_NOT_FOUND.getErrorCode(), thrownException.getErrorCode(), "The error code should be BANK_NOT_FOUND");

        verify(bankRepository).findById(acquirerId);
        verifyNoMoreInteractions(apiKeyService, cognitoService, bankMapper);
    }

    @Test
    void testDisable() {
        String acquirerId = "test-acquirer-id";
        BankEntity bankEntity = new BankEntity();
        bankEntity.setApiKeyId("test-api-key-id");
        bankEntity.setClientId("test-client-id");
        bankEntity.setUsagePlanId("test-usage-plan-id");

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().item(bankEntity));
        when(apiKeyService.deleteUsagePlan("test-usage-plan-id")).thenReturn(Uni.createFrom().voidItem());
        when(apiKeyService.deleteApiKey("test-api-key-id")).thenReturn(Uni.createFrom().voidItem());
        when(cognitoService.deleteClient("test-client-id")).thenReturn(Uni.createFrom().voidItem());
        when(bankRepository.persist(bankEntity)).thenReturn(Uni.createFrom().item(bankEntity));

        Uni<Void> resultUni = bankService.disable(acquirerId);

        resultUni.subscribe().with(
                result -> {
                    // If we reach here, the test is successful
                },
                failure -> {
                    throw new AssertionError("Test failed with exception: " + failure.getMessage());
                }
        );
    }

    @Test
    void testDisableWithBankNotFound() {
        String acquirerId = "test-acquirer-id";
        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().nullItem());

        Uni<Void> result = bankService.disable(acquirerId);

        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(Throwable.class);

        Throwable failure = subscriber.getFailure();

        if (failure instanceof CompositeException) {
            Throwable innerFailure = failure.getCause();
            if (innerFailure instanceof AtmLayerException) {
                AtmLayerException atmLayerException = (AtmLayerException) innerFailure;
                assertEquals(404, atmLayerException.getResponse().getStatus());
                assertEquals("Non esiste tale ID nel database", atmLayerException.getMessage());
            } else {
                fail("Expected AtmLayerException but got " + innerFailure.getClass().getName());
            }
        } else if (failure instanceof AtmLayerException) {
            AtmLayerException atmLayerException = (AtmLayerException) failure;
            assertEquals(404, atmLayerException.getResponse().getStatus());
            assertEquals("Non esiste tale ID nel database", atmLayerException.getMessage());
        } else {
            fail("Expected CompositeException or AtmLayerException but got " + failure.getClass().getName());
        }
    }

    @Test
    void testGetStaticAWSInfo1() {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setApiKeyId("test-api-key-id");
        bankEntity.setClientId("test-client-id");
        bankEntity.setUsagePlanId("test-usage-plan-id");

        UsagePlanDTO usagePlanDto = new UsagePlanDTO();
        ApiKeyDTO apiKeyDto = new ApiKeyDTO();
        ClientCredentialsDTO clientDto = new ClientCredentialsDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(apiKeyService.getUsagePlan("test-usage-plan-id")).thenReturn(Uni.createFrom().item(usagePlanDto));
        when(apiKeyService.getApiKey("test-api-key-id")).thenReturn(Uni.createFrom().item(apiKeyDto));
        when(cognitoService.getClientCredentials("test-client-id")).thenReturn(Uni.createFrom().item(clientDto));
        when(bankMapper.toPresentationDTO(any(BankEntity.class), any(ApiKeyDTO.class), any(ClientCredentialsDTO.class), any(UsagePlanDTO.class)))
                .thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.getStaticAWSInfo(bankEntity);

        resultUni.subscribe().with(
                result -> {
                    assertNotNull(result);
                    assertEquals(bankPresentationDTO, result);
                },
                failure -> {
                    throw new AssertionError("Test failed with exception: " + failure.getMessage());
                }
        );
    }

    @Test
    void testGetStaticAWSInfo2() {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setApiKeyId("test-api-key-id");
        bankEntity.setClientId("test-client-id");

        UsagePlanDTO usagePlanDto = new UsagePlanDTO();

        ApiKeyDTO apiKeyDto = new ApiKeyDTO();
        ClientCredentialsDTO clientDto = new ClientCredentialsDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(apiKeyService.getApiKey("test-api-key-id")).thenReturn(Uni.createFrom().item(apiKeyDto));
        when(cognitoService.getClientCredentials("test-client-id")).thenReturn(Uni.createFrom().item(clientDto));
        when(bankMapper.toPresentationDTO(any(BankEntity.class), any(ApiKeyDTO.class), any(ClientCredentialsDTO.class), any(UsagePlanDTO.class)))
                .thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.getStaticAWSInfo(bankEntity, usagePlanDto);

        resultUni.subscribe().with(
                result -> {
                    assertNotNull(result);
                    assertEquals(bankPresentationDTO, result);
                },
                failure -> {
                    throw new AssertionError("Test failed with exception: " + failure.getMessage());
                }
        );
    }

    @Test
    void testGetStaticAWSInfo3() {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setApiKeyId("test-api-key-id");

        ClientCredentialsDTO clientDto = new ClientCredentialsDTO();
        UsagePlanDTO usagePlanDto = new UsagePlanDTO();

        ApiKeyDTO apiKeyDto = new ApiKeyDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(apiKeyService.getApiKey("test-api-key-id")).thenReturn(Uni.createFrom().item(apiKeyDto));
        when(bankMapper.toPresentationDTO(any(BankEntity.class), any(ApiKeyDTO.class), any(ClientCredentialsDTO.class), any(UsagePlanDTO.class)))
                .thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.getStaticAWSInfo(bankEntity, clientDto, usagePlanDto);

        resultUni.subscribe().with(
                result -> {
                    assertNotNull(result);
                    assertEquals(bankPresentationDTO, result);
                },
                failure -> {
                    throw new AssertionError("Test failed with exception: " + failure.getMessage());
                }
        );
    }

    @Test
    void testSearchBankSuccess() {
        List<BankEntity> bankList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankList.add(bankEntity);
        int pageIndex = 0;
        int pageSize = 10;
        String acquirerId = "acquirer123";
        String denomination = "Test Bank";
        String clientId = "client456";

        PageInfo<BankEntity> expectedPageInfo = new PageInfo<>(0, 10, 1, 1, bankList);

        when(bankRepository.findByFilters(anyMap(), eq(pageIndex), eq(pageSize)))
                .thenReturn(Uni.createFrom().item(expectedPageInfo));

        Uni<PageInfo<BankEntity>> result = bankService.searchBanks(pageIndex, pageSize, acquirerId, denomination, clientId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expectedPageInfo);
    }

    @Test
    void testSearchBanksEmptyFilters() {
        List<BankEntity> bankList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankList.add(bankEntity);

        int pageIndex = 0;
        int pageSize = 10;
        String acquirerId = null;
        String denomination = "";
        String clientId = null;

        Map<String, Object> filters = new HashMap<>();
        filters.put("acquirerId", acquirerId);
        filters.put("denomination", denomination);
        filters.put("clientId", clientId);
        filters.remove(null);
        filters.values().removeAll(Collections.singleton(null));
        filters.values().removeAll(Collections.singleton(""));

        PageInfo<BankEntity> expectedPageInfo = new PageInfo<>(0, 10, 1, 1, bankList);

        when(bankRepository.findByFilters(eq(filters), eq(pageIndex), eq(pageSize)))
                .thenReturn(Uni.createFrom().item(expectedPageInfo));

        Uni<PageInfo<BankEntity>> result = bankService.searchBanks(pageIndex, pageSize, acquirerId, denomination, clientId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expectedPageInfo);
    }

    @Test
    void testFindBankByIdFound() {
        String acquirerId = "test-acquirer-id";
        BankEntity bankEntity = new BankEntity();

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().item(bankEntity));

        Uni<BankEntity> result = bankService.findBankById(acquirerId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(bankEntity);
    }

    @Test
    void testFindBankByIdNotFound() {
        String acquirerId = "non-existent-id";

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().nullItem());

        AtmLayerException thrownException = assertThrows(AtmLayerException.class, () -> bankService.findBankById(acquirerId).await().indefinitely());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), thrownException.getStatusCode());
        assertEquals(AppErrorCodeEnum.BANK_NOT_FOUND.getErrorCode(), thrownException.getErrorCode());
    }

    @Test
    void testGetAll() {
        List<BankEntity> bankEntitiesList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankEntitiesList.add(bankEntity);

        PanacheQuery<BankEntity> panacheQuery = mock(PanacheQuery.class);

        when(bankRepository.findAll()).thenReturn(panacheQuery);
        when(panacheQuery.list()).thenReturn(Uni.createFrom().item(bankEntitiesList));

        Uni<List<BankEntity>> result = bankService.getAll();

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(bankEntitiesList);
    }

    @Test
    void testUpdateBank_UsagePlanUpdateSuccess() {
        BankUpdateDTO input = new BankUpdateDTO();
        input.setAcquirerId("test-acquirer-id");
        input.setDenomination("Denomination");
        input.setRateLimit(100.0);
        input.setBurstLimit(50);
        input.setLimit(1000);
        input.setPeriod(QuotaPeriodType.MONTH);

        BankEntity bankEntity = new BankEntity();
        bankEntity.setDenomination("Denomination");
        bankEntity.setClientId("client-id");
        bankEntity.setApiKeyId("api-key-id");
        bankEntity.setUsagePlanId("usage-plan-id");

        UsagePlanDTO updatedUsagePlan = new UsagePlanDTO();
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(bankRepository.findById(input.getAcquirerId())).thenReturn(Uni.createFrom().item(bankEntity));
        when(apiKeyService.updateUsagePlan(anyString(), any())).thenReturn(Uni.createFrom().item(updatedUsagePlan));
        when(apiKeyService.getApiKey(anyString())).thenReturn(Uni.createFrom().item(new ApiKeyDTO()));
        when(cognitoService.getClientCredentials(anyString())).thenReturn(Uni.createFrom().item(clientCredentialsDTO));
        when(bankMapper.toPresentationDTO(any(), any(), any(), any())).thenReturn(bankPresentationDTO);

        Uni<BankPresentationDTO> resultUni = bankService.updateBank(input);
        BankPresentationDTO result = resultUni.await().indefinitely();

        assertNotNull(result, "The result should not be null");
        assertEquals(bankPresentationDTO, result, "The result should match the expected BankPresentationDTO");

        verify(bankRepository).findById(input.getAcquirerId());
        verify(apiKeyService).updateUsagePlan(anyString(), any());
        verify(apiKeyService).getApiKey(anyString());
        verify(cognitoService).getClientCredentials(anyString());
        verify(bankMapper).toPresentationDTO(any(), any(), any(), any());
    }

    @Test
    void testRollbackUsagePlanCreation() {
        ApiKeyDTO apiKey = new ApiKeyDTO("usage-plan-id", "api-key-value", "Test API Key");

        when(apiKeyService.deleteUsagePlan(apiKey.getId()))
                .thenReturn(Uni.createFrom().voidItem());

        Uni<Void> result = bankService.rollbackUsagePlanCreation(apiKey);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();

        verify(apiKeyService).deleteUsagePlan(apiKey.getId());
        verifyNoMoreInteractions(apiKeyService);
    }
}
