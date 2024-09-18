package userservice.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.CognitoServiceImpl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
class CognitoResourceTest {

    @InjectMock
    CognitoServiceImpl cognitoService;

    @Test
    void testGenerateClientSuccess() {
        String clientName = "testClient";
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO("clientId123", "clientSecret123", "testClient-credentials");

        when(cognitoService.generateClient(clientName)).thenReturn(Uni.createFrom().item(clientCredentialsDTO));

        ClientCredentialsDTO result = given()
                .header("clientName", clientName)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("api/v1/user-service/cognito/client-credentials")
                .then()
                .statusCode(200)
                .extract()
                .as(ClientCredentialsDTO.class);

        assertEquals(clientCredentialsDTO, result);

        verify(cognitoService, times(1)).generateClient(clientName);
    }

    @Test
    void testGenerateClientFailure() {
        String clientName = "testClient";

        when(cognitoService.generateClient(clientName))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("La richiesta di generateClient su AWS non è andata a buon fine")));

        given()
                .header("clientName", clientName)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("api/v1/user-service/cognito/client-credentials")
                .then()
                .statusCode(500)
                .body(containsString("La richiesta di generateClient su AWS non è andata a buon fine"));

        verify(cognitoService, times(1)).generateClient(clientName);
    }

    @Test
    void testGetClientCredentials() {
        String clientId = "valid-client-id";
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();

        when(cognitoService.getClientCredentials(clientId))
                .thenReturn(Uni.createFrom().item(clientCredentialsDTO));

        ClientCredentialsDTO result = given()
                .when()
                .get("api/v1/user-service/cognito/client-credentials/" + clientId)
                .then()
                .statusCode(200)
                .extract()
                .as(ClientCredentialsDTO.class);

        assertEquals(clientCredentialsDTO, result);

        verify(cognitoService, times(1)).getClientCredentials(clientId);
    }

    @Test
    void testGetClientCredentialsNotFound() {
        String clientId = "nonexistent-client-id";

        when(cognitoService.getClientCredentials(clientId))
                .thenReturn(Uni.createFrom().failure(new AtmLayerException("ClientCredentials non trovate", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)));

        given()
                .when()
                .get("api/v1/user-service/cognito/client-credentials/" + clientId)
                .then()
                .statusCode(500)
                .body("message", equalTo("ClientCredentials non trovate"));

        verify(cognitoService, times(1)).getClientCredentials(clientId);
    }

    @Test
    void testDeleteClientSuccess() {
        String clientId = "12345";

        when(cognitoService.deleteClient(clientId)).thenReturn(Uni.createFrom().item(() -> null));

        given()
                .when()
                .delete("api/v1/user-service/cognito/client-credentials/" + clientId)
                .then()
                .statusCode(204); // No Content

        verify(cognitoService, times(1)).deleteClient(clientId);
    }

    @Test
    void testDeleteClientFailure() {
        String clientId = "nonexistent";

        when(cognitoService.deleteClient(clientId))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Client not found")));

        given()
                .when()
                .delete("api/v1/user-service/cognito/client-credentials/" + clientId)
                .then()
                .statusCode(500) // Internal Server Error
                .body(containsString("Client not found"));

        verify(cognitoService, times(1)).deleteClient(clientId);
    }

}
