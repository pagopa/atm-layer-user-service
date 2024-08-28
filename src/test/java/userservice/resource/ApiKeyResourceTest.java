package userservice.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
class ApiKeyResourceTest {

    @InjectMock
    ApiKeyService apiKeyService;

    @Test
    void testGetApiKey() {
        String apiKeyId = "12345";
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO();

        when(apiKeyService.getApiKey(apiKeyId)).thenReturn(Uni.createFrom().item(apiKeyDTO));

        ApiKeyDTO result = given()
                .when()
                .get("api/v1/user-service/api-gateway/api-key/retrieve/" + apiKeyId)
                .then()
                .statusCode(200)
                .extract()
                .as(ApiKeyDTO.class);

        assertEquals(apiKeyDTO, result);

        verify(apiKeyService, times(1)).getApiKey(apiKeyId);
    }

    @Test
    void testGetApiKeyNotFound() {
        String apiKeyId = "nonexistent";

        when(apiKeyService.getApiKey(apiKeyId)).thenReturn(Uni.createFrom().failure(new AtmLayerException("ApiKey non trovata", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)));

        given()
                .when()
                .get("api/v1/user-service/api-gateway/api-key/retrieve/" + apiKeyId)
                .then()
                .statusCode(500)
                .body("message", equalTo("ApiKey non trovata"));

        verify(apiKeyService, times(1)).getApiKey(apiKeyId);
    }

    @Test
    void testDeleteApiKey() {
        String apiKeyId = "12345";

        when(apiKeyService.deleteApiKey(apiKeyId)).thenReturn(Uni.createFrom().item(() -> null));

        given()
                .when()
                .delete("api/v1/user-service/api-gateway/api-key/delete/" + apiKeyId)
                .then()
                .statusCode(204);

        verify(apiKeyService, times(1)).deleteApiKey(apiKeyId);
    }

    @Test
    void testDeleteApiKeyFailure() {
        String apiKeyId = "nonexistent";

        when(apiKeyService.deleteApiKey(apiKeyId))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("API Key not found")));

        given()
                .when()
                .delete("api/v1/user-service/api-gateway/api-key/delete/" + apiKeyId)
                .then()
                .statusCode(500)
                .body(containsString("API Key not found"));

        verify(apiKeyService, times(1)).deleteApiKey(apiKeyId);
    }

    @Test
    void testGenerateApiKeySuccess() {
        String clientName = "testClient";
        String apiKeyValue = "apiKeyValue";
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO("id123", "value123", "testClient-api-key");

        when(apiKeyService.createApiKey(apiKeyValue, clientName)).thenReturn(Uni.createFrom().item(apiKeyDTO));

        given()
                .header("clientName", clientName)
                .header("apiKeyValue", apiKeyValue)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("api/v1/user-service/api-gateway/api-key/generate")
                .then()
                .statusCode(200) // Codice di stato OK
                .body("id", equalTo("id123"))
                .body("value", equalTo("value123"))
                .body("name", equalTo("testClient-api-key"));

        verify(apiKeyService, times(1)).createApiKey(apiKeyValue, clientName);
    }

    @Test
    void testGenerateApiKeyFailure() {
        String clientName = "testClient";
        String apiKeyValue = "apiKeyValue";

        when(apiKeyService.createApiKey(apiKeyValue, clientName))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("La richiesta di CreateApiKey su AWS non è andata a buon fine")));

        given()
                .header("clientName", clientName)
                .header("apiKeyValue", apiKeyValue)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("api/v1/user-service/api-gateway/api-key/generate")
                .then()
                .statusCode(500)
                .body(containsString("La richiesta di CreateApiKey su AWS non è andata a buon fine"));

        verify(apiKeyService, times(1)).createApiKey(apiKeyValue, clientName);
    }

}
