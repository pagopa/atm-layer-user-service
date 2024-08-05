package userservice.service.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.configuration.AwsClientConf;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.CognitoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
class CognitoServiceImplTest {

    @Mock
    AwsClientConf awsClientConf;
    @Mock
    private CognitoIdentityProviderClient cognitoClient;
    @InjectMocks
    CognitoServiceImpl cognitoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateClientNameSuccess() {
        String clientId = "test-client-id";
        String clientName = "updated-client-name";

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);

        UserPoolClientType userPoolClient = UserPoolClientType.builder()
                .clientId(clientId)
                .clientSecret("test-client-secret")
                .clientName(clientName)
                .build();

        UpdateUserPoolClientResponse updateResponse = UpdateUserPoolClientResponse.builder()
                .userPoolClient(userPoolClient)
                .build();

        when(cognitoClient.updateUserPoolClient(any(UpdateUserPoolClientRequest.class)))
                .thenReturn(updateResponse);

        Uni<ClientCredentialsDTO> resultUni = cognitoService.updateClientName(clientId, clientName);

        ClientCredentialsDTO result = resultUni.await().indefinitely();
        assertEquals(clientId, result.getClientId());
        assertEquals("test-client-secret", result.getClientSecret());
        assertEquals(clientName, result.getClientName());
    }

    @Test
    void testGenerateClientSuccess() {
        String clientName = "test-client";

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);

        UserPoolClientType userPoolClient = UserPoolClientType.builder()
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .clientName(clientName)
                .build();

        CreateUserPoolClientResponse createResponse = CreateUserPoolClientResponse.builder()
                .userPoolClient(userPoolClient)
                .build();

        when(cognitoClient.createUserPoolClient(any(CreateUserPoolClientRequest.class)))
                .thenReturn(createResponse);

        Uni<ClientCredentialsDTO> resultUni = cognitoService.generateClient(clientName);

        ClientCredentialsDTO result = resultUni.await().indefinitely();
        assertEquals("test-client-id", result.getClientId());
        assertEquals("test-client-secret", result.getClientSecret());
        assertEquals(clientName, result.getClientName());
    }

    @Test
    void testGenerateClientFailure() {
        String clientName = "test-client";

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);

        doThrow(new RuntimeException("Simulated failure")).when(cognitoClient)
                .createUserPoolClient(any(CreateUserPoolClientRequest.class));

        Uni<ClientCredentialsDTO> resultUni = cognitoService.generateClient(clientName);

        assertThrows(AtmLayerException.class, () -> resultUni.await().indefinitely());
    }

    @Test
    void testGetClientCredentials() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String clientName = "test-client-name";

        UserPoolClientType userPoolClient = UserPoolClientType.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientName(clientName)
                .build();

        DescribeUserPoolClientResponse response = DescribeUserPoolClientResponse.builder()
                .userPoolClient(userPoolClient)
                .build();

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);
        when(cognitoClient.describeUserPoolClient(any(DescribeUserPoolClientRequest.class)))
                .thenReturn(response);

        Uni<ClientCredentialsDTO> uniResult = cognitoService.getClientCredentials(clientId);

        ClientCredentialsDTO result = uniResult.await().indefinitely();

        assertEquals(clientId, result.getClientId());
        assertEquals(clientSecret, result.getClientSecret());
        assertEquals(clientName, result.getClientName());
    }

    @Test
    void testDeleteClientSuccess() {
        String clientId = "test-client-id";

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);

        Uni<Void> resultUni = cognitoService.deleteClient(clientId);

        assertDoesNotThrow(() -> resultUni.await().indefinitely());
    }

    @Test
    void testDeleteClientFailure() {
        String clientId = "test-client-id";

        when(awsClientConf.getCognitoClient()).thenReturn(cognitoClient);
        when(cognitoClient.deleteUserPoolClient(any(DeleteUserPoolClientRequest.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        Uni<Void> resultUni = cognitoService.deleteClient(clientId);
        assertThrows(RuntimeException.class, () -> resultUni.await().indefinitely());
    }
}
