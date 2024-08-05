package userservice.service.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ApiKeyMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.ApiKeyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class ApiKeyServiceImplTest {

    @Mock
    ApiKeyMapper mapper;
    @Mock
    private ApiGatewayClient apiGatewayClient;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*@Test
    void testCreateApiKeySuccess() {
        String clientName = "test-client";

        CreateApiKeyResponse response = CreateApiKeyResponse.builder()
                .id("api-key-id")
                .value("api-key-value")
                .name(clientName + "-api-key")
                .build();

        when(apiGatewayClient.createApiKey(any(CreateApiKeyRequest.class))).thenReturn(response);

        Uni<ApiKeyDTO> resultUni = apiKeyService.createApiKey(clientName);

        ApiKeyDTO result = resultUni.await().indefinitely();

        assertEquals("api-key-id", result.getId());
        assertEquals("api-key-value", result.getValue());
        assertEquals(clientName + "-api-key", result.getName());
    }*/

    /*@Test
    void testGetUsagePlan_error() {
        // Arrange
        String usagePlanId = "invalidPlanId";
        when(apiGatewayClient.getUsagePlan(any(GetUsagePlanRequest.class)))
                .thenThrow(new RuntimeException("AWS SDK error"));

        // Act
        Uni<UsagePlanDTO> result = apiKeyService.getUsagePlan(usagePlanId);

        // Assert
        result.subscribe().with(
                dto -> {
                    fail("Expected an exception, but got: " + dto);
                },
                throwable -> {
                    assertTrue(throwable instanceof RuntimeException, "Expected RuntimeException");
                    assertEquals("AWS SDK error", throwable.getMessage(), "Exception message does not match");
                }
        );
    }*/

   /* @Test
    void testGetApiKey_success() {
        GetApiKeyResponse response = GetApiKeyResponse.builder()
                .id("keyId")
                .value("keyValue")
                .name("keyName")
                .build();
        when(apiGatewayClient.getApiKey(any(GetApiKeyRequest.class))).thenReturn(response);

        Uni<ApiKeyDTO> result = apiKeyService.getApiKey("keyId");

        result.subscribe().with(
                apiKeyDTO -> {
                    assertNotNull(apiKeyDTO, "ApiKeyDTO should not be null");
                    assertEquals("keyId", apiKeyDTO.getId(), "API Key ID does not match");
                    assertEquals("keyValue", apiKeyDTO.getValue(), "API Key value does not match");
                    assertEquals("keyName", apiKeyDTO.getName(), "API Key name does not match");
                },
                throwable -> fail("Expected no exception, but got: " + throwable.getMessage())
        );
    }*/


}
