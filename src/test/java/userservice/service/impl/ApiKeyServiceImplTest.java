package userservice.service.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.configuration.ApiGatewayClientConf;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ApiKeyMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.ApiKeyServiceImpl;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static software.amazon.awssdk.services.apigateway.model.QuotaPeriodType.MONTH;

@QuarkusTest
class ApiKeyServiceImplTest {

    @Mock
    ApiKeyMapper mapper;
    @Mock
    private ApiGatewayClientConf apiGatewayClientConf;
    @Mock
    private ApiGatewayClient apiGatewayClient;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    @Spy
    private Logger log = LoggerFactory.getLogger(ApiKeyServiceImpl.class);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        log = mock(Logger.class);
    }

    @Test
    void testCreateApiKeySuccess() {
        String clientName = "testClient";
        String apiKeyValue = "apiKeyValue";
        CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                .value(apiKeyValue)
                .name(clientName + "-api-key")
                .enabled(true)
                .build();

        CreateApiKeyResponse response = CreateApiKeyResponse.builder()
                .id("123")
                .value("api-key-value")
                .name(clientName + "-api-key")
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createApiKey(request)).thenReturn(response);

        Uni<ApiKeyDTO> apiKeyDTOUni = apiKeyService.createApiKey(apiKeyValue, clientName);

        apiKeyDTOUni.subscribe().with(apiKeyDTO -> {
            assertEquals("123", apiKeyDTO.getId());
            assertEquals("apiKeyValue", apiKeyDTO.getValue());
            assertEquals(clientName + "-api-key", apiKeyDTO.getName());
        }, throwable -> fail("Expected no exception, but got: " + throwable));
    }

    @Test
    void testCreateApiKeyFailure() {
        String clientName = "testClient";
        String apiKeyValue = "apiKeyValue";
        CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                .value(apiKeyValue)
                .name(clientName + "-api-key")
                .enabled(true)
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createApiKey(request)).thenReturn(CreateApiKeyResponse.builder().build());

        Uni<ApiKeyDTO> apiKeyDTOUni = apiKeyService.createApiKey(apiKeyValue, clientName);
        apiKeyDTOUni.subscribe().with(
                apiKeyDTO -> fail("Expected an exception, but got: " + apiKeyDTO),
                throwable -> {
                    assertTrue(throwable instanceof AtmLayerException);
                    assertEquals("La richiesta di CreateApiKey su AWS non è andata a buon fine", throwable.getMessage());
                }
        );
    }

    @Test
    void testGetApiKeySuccess() {
        String apiKeyId = "123";
        GetApiKeyRequest request = GetApiKeyRequest.builder()
                .apiKey(apiKeyId)
                .includeValue(true)
                .build();

        GetApiKeyResponse response = GetApiKeyResponse.builder()
                .id(apiKeyId)
                .value("api-key-value")
                .name("test-api-key")
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.getApiKey(request)).thenReturn(response);

        Uni<ApiKeyDTO> apiKeyDTOUni = apiKeyService.getApiKey(apiKeyId);

        apiKeyDTOUni.subscribe().with(apiKeyDTO -> {
            assertEquals(apiKeyId, apiKeyDTO.getId());
            assertEquals("api-key-value", apiKeyDTO.getValue());
            assertEquals("test-api-key", apiKeyDTO.getName());
        }, throwable -> fail("Expected no exception, but got: " + throwable));
    }

    @Test
    void testGetApiKeyNotFound() {
        String apiKeyId = "123";
        GetApiKeyRequest request = GetApiKeyRequest.builder()
                .apiKey(apiKeyId)
                .includeValue(true)
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.getApiKey(request)).thenReturn(GetApiKeyResponse.builder().build());

        Uni<ApiKeyDTO> apiKeyDTOUni = apiKeyService.getApiKey(apiKeyId);
        apiKeyDTOUni.subscribe().with(
                apiKeyDTO -> fail("Expected an exception, but got: " + apiKeyDTO),
                throwable -> {
                    assertTrue(throwable instanceof AtmLayerException);
                    assertEquals("ApiKey non trovata", throwable.getMessage());
                }
        );
    }

    @Test
    void testDeleteApiKeySuccess() {
        String apiKeyId = "123";

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);

        Uni<Void> deleteResultUni = apiKeyService.deleteApiKey(apiKeyId);

        assertDoesNotThrow(() -> deleteResultUni.await().indefinitely());
    }

    @Test
    void testDeleteApiKeyFailure() {
        String apiKeyId = "123";
        DeleteApiKeyRequest request = DeleteApiKeyRequest.builder()
                .apiKey(apiKeyId)
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        doThrow(new RuntimeException("Delete failed")).when(apiGatewayClient).deleteApiKey(request);

        Uni<Void> deleteResultUni = apiKeyService.deleteApiKey(apiKeyId);

        deleteResultUni.subscribe().with(
                result -> fail("Expected an exception, but got: " + result),
                throwable -> {
                    assertTrue(throwable instanceof RuntimeException);
                    assertEquals("Delete failed", throwable.getMessage());
                }
        );
        verify(apiGatewayClient).deleteApiKey(request);
    }

    @Test
    void testGetUsagePlanSuccess() {
        String usagePlanId = "plan123";
        GetUsagePlanRequest usagePlanRequest = GetUsagePlanRequest.builder()
                .usagePlanId(usagePlanId)
                .build();

        ThrottleSettings throttleSettings = ThrottleSettings.builder()
                .burstLimit(200)
                .rateLimit(10.5)
                .build();

        QuotaSettings quotaSettings = QuotaSettings.builder()
                .limit(1000)
                .period(MONTH)
                .build();

        GetUsagePlanResponse usagePlanResponse = GetUsagePlanResponse.builder()
                .id(usagePlanId)
                .name("Test Usage Plan")
                .description("Description of the usage plan")
                .throttle(throttleSettings)
                .quota(quotaSettings)
                .build();

        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlanId);
        usagePlanDTO.setName("Test Usage Plan");
        usagePlanDTO.setLimit(1000);
        usagePlanDTO.setPeriod(MONTH);
        usagePlanDTO.setBurstLimit(200);
        usagePlanDTO.setRateLimit(10.5);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.getUsagePlan(usagePlanRequest)).thenReturn(usagePlanResponse);
        when(mapper.usagePlanGetToDto(usagePlanResponse)).thenReturn(usagePlanDTO);

        Uni<UsagePlanDTO> usagePlanDTOSubscription = apiKeyService.getUsagePlan(usagePlanId);

        usagePlanDTOSubscription.subscribe().with(dto -> {
            assertEquals(usagePlanId, dto.getId());
            assertEquals("Test Usage Plan", dto.getName());
            assertEquals(1000, dto.getLimit());
            assertEquals(MONTH, dto.getPeriod());
            assertEquals(200, dto.getBurstLimit());
            assertEquals(10.5, dto.getRateLimit());
        }, throwable -> fail("Expected no exception, but got: " + throwable));

        verify(apiGatewayClient).getUsagePlan(usagePlanRequest);
        verify(mapper).usagePlanGetToDto(usagePlanResponse);
    }

    @Test
    void testGetUsagePlanFailure() {
        String usagePlanId = "plan123";
        GetUsagePlanRequest usagePlanRequest = GetUsagePlanRequest.builder()
                .usagePlanId(usagePlanId)
                .build();

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.getUsagePlan(usagePlanRequest))
                .thenThrow(new RuntimeException("Failed to fetch usage plan"));

        Uni<UsagePlanDTO> usagePlanDTOSubscription = apiKeyService.getUsagePlan(usagePlanId);

        usagePlanDTOSubscription.subscribe().with(
                dto -> fail("Expected an exception, but got: " + dto),
                throwable -> {
                    assertTrue(throwable instanceof RuntimeException);
                    assertEquals("Failed to fetch usage plan", throwable.getMessage());
                }
        );
        verify(apiGatewayClient).getUsagePlan(usagePlanRequest);
        verify(mapper, never()).usagePlanGetToDto(any());
    }

    @Test
    void testCreateUsagePlanSuccess() {
        String apiKeyId = "apiKey123";
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setDenomination("Test Bank");
        bankInsertionDTO.setLimit(1000);
        bankInsertionDTO.setPeriod(QuotaPeriodType.MONTH);
        bankInsertionDTO.setBurstLimit(200);
        bankInsertionDTO.setRateLimit(10.5);

        String usagePlanId = "plan123";

        CreateUsagePlanResponse usagePlanResponse = CreateUsagePlanResponse.builder()
                .id(usagePlanId)
                .build();

        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlanId);
        usagePlanDTO.setName("Test Bank-plan");
        usagePlanDTO.setLimit(1000);
        usagePlanDTO.setPeriod(QuotaPeriodType.MONTH);
        usagePlanDTO.setBurstLimit(200);
        usagePlanDTO.setRateLimit(10.5);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createUsagePlan(any(CreateUsagePlanRequest.class))).thenReturn(usagePlanResponse);
        when(mapper.usagePlanCreateToDto(any(CreateUsagePlanResponse.class))).thenReturn(usagePlanDTO);

        Uni<UsagePlanDTO> usagePlanDTOSubscription = apiKeyService.createUsagePlan(bankInsertionDTO, apiKeyId);

        usagePlanDTOSubscription.subscribe().with(dto -> {
            assertNotNull(dto);
            assertEquals(usagePlanId, dto.getId());
            assertEquals("Test Bank-plan", dto.getName());
            assertEquals(1000, dto.getLimit());
            assertEquals(QuotaPeriodType.MONTH, dto.getPeriod());
            assertEquals(200, dto.getBurstLimit());
            assertEquals(10.5, dto.getRateLimit());
        }, throwable -> fail("Expected no exception, but got: " + throwable));

        verify(apiGatewayClient).createUsagePlan(any(CreateUsagePlanRequest.class));
        verify(apiGatewayClient).createUsagePlanKey(any(CreateUsagePlanKeyRequest.class));
        verify(mapper).usagePlanCreateToDto(any(CreateUsagePlanResponse.class));
    }

    @Test
    void testUpdateUsagePlanSuccess() {
        String usagePlanId = "plan123";
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(15.0);
        updateDTO.setBurstLimit(300);

        UpdateUsagePlanResponse updateUsagePlanResponse = UpdateUsagePlanResponse.builder()
                .build();

        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlanId);
        usagePlanDTO.setRateLimit(15.0);
        usagePlanDTO.setBurstLimit(300);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.updateUsagePlan(any(UpdateUsagePlanRequest.class))).thenReturn(updateUsagePlanResponse);
        when(mapper.usagePlanUpdateToDto(any(UpdateUsagePlanResponse.class))).thenReturn(usagePlanDTO);

        Uni<UsagePlanDTO> resultUni = apiKeyService.updateUsagePlan(usagePlanId, updateDTO);

        resultUni.subscribe().with(result -> {
            assertNotNull(result);
            assertEquals(usagePlanId, result.getId());
            assertEquals(15.0, result.getRateLimit());
            assertEquals(300, result.getBurstLimit());
        }, throwable -> fail("Expected no exception, but got: " + throwable));

        verify(apiGatewayClient).updateUsagePlan(any(UpdateUsagePlanRequest.class));
        verify(mapper).usagePlanUpdateToDto(any(UpdateUsagePlanResponse.class));
    }

    @Test
    void testBuildPatchOperationWithAllValidFields() {
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setQuotaLimit(100);
        updateDTO.setQuotaPeriod(MONTH);
        updateDTO.setRateLimit(5.0);
        updateDTO.setBurstLimit(10);

        List<PatchOperation> patchOperations = apiKeyService.buildPatchOperation(updateDTO);

        assertEquals(4, patchOperations.size());
        assertTrue(patchOperations.contains(PatchOperation.builder().op(Op.REPLACE).path("/quota/limit").value("100").build()));
        assertTrue(patchOperations.contains(PatchOperation.builder().op(Op.REPLACE).path("/quota/period").value("MONTH").build()));
        assertTrue(patchOperations.contains(PatchOperation.builder().op(Op.REPLACE).path("/throttle/rateLimit").value("5.0").build()));
        assertTrue(patchOperations.contains(PatchOperation.builder().op(Op.REPLACE).path("/throttle/burstLimit").value("10").build()));
    }

    @Test
    void testBuildPatchOperationWithInvalidRateLimitAndBurstLimit() {
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(5.0);

        Exception exception = assertThrows(AtmLayerException.class, () -> apiKeyService.buildPatchOperation(updateDTO));

        assertEquals("Non è possibile specificare solo uno tra rate limit e burst limit", exception.getMessage());
    }

    @Test
    void testBuildPatchOperationWithInvalidQuotaLimitAndPeriod() {
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setQuotaPeriod(MONTH);

        Exception exception = assertThrows(AtmLayerException.class, () -> apiKeyService.buildPatchOperation(updateDTO));

        assertEquals("Non è possibile specificare solo uno tra quota limit e quota period", exception.getMessage());
    }

    @Test
    void testCreateUsagePlanWithFailureOnKeyCreation() {
        String apiKeyId = "apiKey123";
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setDenomination("Test Bank");
        bankInsertionDTO.setLimit(1000);
        bankInsertionDTO.setPeriod(QuotaPeriodType.MONTH);
        bankInsertionDTO.setBurstLimit(200);
        bankInsertionDTO.setRateLimit(10.5);

        String usagePlanId = "plan123";

        CreateUsagePlanResponse usagePlanResponse = CreateUsagePlanResponse.builder()
                .id(usagePlanId)
                .build();

        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlanId);
        usagePlanDTO.setName("Test Bank-plan");
        usagePlanDTO.setLimit(1000);
        usagePlanDTO.setPeriod(QuotaPeriodType.MONTH);
        usagePlanDTO.setBurstLimit(200);
        usagePlanDTO.setRateLimit(10.5);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createUsagePlan(any(CreateUsagePlanRequest.class))).thenReturn(usagePlanResponse);
        when(apiGatewayClient.createUsagePlanKey(any(CreateUsagePlanKeyRequest.class))).thenThrow(new RuntimeException("Simulated exception"));
        when(mapper.usagePlanCreateToDto(any(CreateUsagePlanResponse.class))).thenReturn(usagePlanDTO);

        Uni<UsagePlanDTO> usagePlanDTOSubscription = apiKeyService.createUsagePlan(bankInsertionDTO, apiKeyId);

        usagePlanDTOSubscription.subscribe().with(
                dto -> fail("Expected an exception, but got: " + dto),
                throwable -> {
                    assertInstanceOf(AtmLayerException.class, throwable);
                    assertEquals("La richiesta di CreateUsagePlanKey su AWS non è andata a buon fine", throwable.getMessage());
                    verify(apiGatewayClient).updateUsagePlan(any(UpdateUsagePlanRequest.class));
                    verify(apiGatewayClient).deleteUsagePlan(any(DeleteUsagePlanRequest.class));
                }
        );
    }

    @Test
    void testCreateUsagePlanWithFailure_no_limits_in_input() {
        String apiKeyId = "apiKey123";
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setDenomination("Test Bank");

        String usagePlanId = "plan123";

        CreateUsagePlanResponse usagePlanResponse = CreateUsagePlanResponse.builder()
                .id(usagePlanId)
                .build();

        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlanId);
        usagePlanDTO.setName("Test Bank-plan");
        usagePlanDTO.setLimit(1000);
        usagePlanDTO.setPeriod(QuotaPeriodType.MONTH);
        usagePlanDTO.setBurstLimit(200);
        usagePlanDTO.setRateLimit(10.5);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createUsagePlan(any(CreateUsagePlanRequest.class))).thenReturn(usagePlanResponse);
        when(apiGatewayClient.createUsagePlanKey(any(CreateUsagePlanKeyRequest.class))).thenThrow(new RuntimeException("Simulated exception"));
        when(mapper.usagePlanCreateToDto(any(CreateUsagePlanResponse.class))).thenReturn(usagePlanDTO);

        Uni<UsagePlanDTO> usagePlanDTOSubscription = apiKeyService.createUsagePlan(bankInsertionDTO, apiKeyId);

        usagePlanDTOSubscription.subscribe().with(
                dto -> fail("Expected an exception, but got: " + dto),
                throwable -> {
                    assertInstanceOf(AtmLayerException.class, throwable);
                    assertEquals("La richiesta di CreateUsagePlanKey su AWS non è andata a buon fine", throwable.getMessage());
                    verify(apiGatewayClient).updateUsagePlan(any(UpdateUsagePlanRequest.class));
                    verify(apiGatewayClient).deleteUsagePlan(any(DeleteUsagePlanRequest.class));
                }
        );
    }

    @Test
    void testDeleteUsagePlanSuccess() {
        String usagePlanId = "plan123";

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);

        Uni<Void> result = apiKeyService.deleteUsagePlan(usagePlanId);

        assertDoesNotThrow(() -> result.await().indefinitely());

        ArgumentCaptor<DeleteUsagePlanRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteUsagePlanRequest.class);
        verify(apiGatewayClient).deleteUsagePlan(deleteCaptor.capture());

        DeleteUsagePlanRequest capturedRequest = deleteCaptor.getValue();
        assertEquals(usagePlanId, capturedRequest.usagePlanId());

        verify(apiGatewayClient).updateUsagePlan(any(UpdateUsagePlanRequest.class));
    }

    @Test
    void testDeleteUsagePlanFailure() {
        String usagePlanId = "plan123";

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);

        doThrow(new RuntimeException("Simulated delete failure")).when(apiGatewayClient).deleteUsagePlan(any(DeleteUsagePlanRequest.class));

        Uni<Void> result = apiKeyService.deleteUsagePlan(usagePlanId);

        result.subscribe().with(
                item -> fail("Expected an exception, but the operation succeeded"),
                throwable -> {
                    assertInstanceOf(RuntimeException.class, throwable);
                    assertEquals("Simulated delete failure", throwable.getMessage());
                }
        );

    }

    @Test
    void testCreateApiKeyWithException() {
        // Arrange
        CreateApiKeyRequest mockRequest = mock(CreateApiKeyRequest.class);
        CreateApiKeyResponse mockResponse = mock(CreateApiKeyResponse.class);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.createApiKey(any(CreateApiKeyRequest.class))).thenReturn(mockResponse);
        when(mockResponse.sdkFields()).thenReturn(Collections.emptyList());

        // Act & Assert
        AtmLayerException exception = assertThrows(AtmLayerException.class, () -> {
            apiKeyService.createApiKey("apiKeyValue", "test-client").await().indefinitely();
        });

        assertEquals("La richiesta di CreateApiKey su AWS non è andata a buon fine", exception.getMessage());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getStatusCode());
        assertEquals(AppErrorCodeEnum.AWS_OPERATION_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testGetApiKeyWithException() {
        // Arrange
        GetApiKeyRequest mockRequest = mock(GetApiKeyRequest.class);
        GetApiKeyResponse mockResponse = mock(GetApiKeyResponse.class);

        when(apiGatewayClientConf.getApiGatewayClient()).thenReturn(apiGatewayClient);
        when(apiGatewayClient.getApiKey(any(GetApiKeyRequest.class))).thenReturn(mockResponse);
        when(mockResponse.sdkFields()).thenReturn(Collections.emptyList());

        // Act & Assert
        AtmLayerException exception = assertThrows(AtmLayerException.class, () -> {
            apiKeyService.getApiKey("test-api-key-id").await().indefinitely();
        });

        assertEquals("ApiKey non trovata", exception.getMessage());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getStatusCode());
        assertEquals(AppErrorCodeEnum.AWS_OPERATION_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testBuildPatchOperation_withRateLimitAndBurstLimit() {
        // Arrange
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(100.0);
        updateDTO.setBurstLimit(200);

        // Act
        List<PatchOperation> patchOperations = apiKeyService.buildPatchOperation(updateDTO);

        // Assert
        assertEquals(3, patchOperations.size());
        assertTrue(patchOperations.stream().anyMatch(po -> po.path().equals("/throttle/rateLimit") && po.value().equals("100.0")));
        assertTrue(patchOperations.stream().anyMatch(po -> po.path().equals("/throttle/burstLimit") && po.value().equals("200")));
    }

    @Test
    void testBuildPatchOperation_withoutRateLimit() {
        // Arrange
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(null);
        updateDTO.setBurstLimit(null);

        // Act
        List<PatchOperation> patchOperations = apiKeyService.buildPatchOperation(updateDTO);

        // Assert
        assertEquals(2, patchOperations.size());
        assertFalse(patchOperations.stream().anyMatch(po -> po.op().equals("REMOVE") && po.path().equals("/throttle")));
    }

    @Test
    void testBuildPatchOperation_withRateLimitOnly() {
        // Arrange
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(100.0);
        updateDTO.setBurstLimit(null);

        // Act & Assert
        AtmLayerException thrown = assertThrows(
                AtmLayerException.class,
                () -> apiKeyService.buildPatchOperation(updateDTO),
                "Expected buildPatchOperation() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Non è possibile specificare solo uno tra rate limit e burst limit"));
        assertEquals(AppErrorCodeEnum.INVALID_PAYLOAD.getErrorCode(), thrown.getErrorCode());
    }

    @Test
    void testBuildPatchOperation_withBurstLimitOnly() {
        // Arrange
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setRateLimit(null);
        updateDTO.setBurstLimit(200);

        // Act & Assert
        AtmLayerException thrown = assertThrows(
                AtmLayerException.class,
                () -> apiKeyService.buildPatchOperation(updateDTO),
                "Expected buildPatchOperation() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Non è possibile specificare solo uno tra rate limit e burst limit"));
        assertEquals(AppErrorCodeEnum.INVALID_PAYLOAD.getErrorCode(), thrown.getErrorCode());
    }

    @Test
    void testBuildPatchOperation_withQuotaLimitOnly() {
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setQuotaLimit(100);

        assertThrows(AtmLayerException.class,
                () -> apiKeyService.buildPatchOperation(updateDTO),
                "Non è possibile specificare solo uno tra quota limit e quota period"
        );
    }

    @Test
    void testBuildPatchOperation_withPeriodOnly() {
        UsagePlanUpdateDTO updateDTO = new UsagePlanUpdateDTO();
        updateDTO.setQuotaPeriod(MONTH);

        assertThrows(AtmLayerException.class,
                () -> apiKeyService.buildPatchOperation(updateDTO),
                "Non è possibile specificare solo uno tra quota limit e quota period"
        );
    }

}
