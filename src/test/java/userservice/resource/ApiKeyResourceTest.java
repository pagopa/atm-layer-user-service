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
