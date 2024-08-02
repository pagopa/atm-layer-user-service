package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.configuration.AwsClientConf;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;


@ApplicationScoped
@Slf4j
public class CognitoServiceImpl implements CognitoService {
    @Inject
    AwsClientConf awsClientConf;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "cognito.user-pool.id")
    String userPoolId;

    @Override
    public Uni<ClientCredentialsDTO> getClientCredentials(String clientId) {
        return Uni.createFrom().item(() -> {
            DescribeUserPoolClientRequest request = DescribeUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build();
            UserPoolClientType client = null;
            try {
                DescribeUserPoolClientResponse response = awsClientConf.getCognitoClient().describeUserPoolClient(request);
                client = response.userPoolClient();
                log.info("Client value: {}", client);
            } catch (Exception e) {
                log.error("ERROR with getClientCredentials: {}", e.getMessage());
            }
            try {
                ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
                clientCredentialsDTO.setClientId(client != null ? client.clientId() : "");
                clientCredentialsDTO.setClientSecret(client != null ? client.clientSecret() : "");
                clientCredentialsDTO.setClientName(client != null ? client.clientName() : "");
                return clientCredentialsDTO;
            } catch (Exception e) {
                log.error("mapping exception");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Uni<ClientCredentialsDTO> generateClient(String clientName) {
        return Uni.createFrom().item(() -> {
                       CreateUserPoolClientRequest request = CreateUserPoolClientRequest.builder()
                    .userPoolId("eu-south-1_sEZF9PqAf")
                               .allowedOAuthFlowsUserPoolClient(true)
                    .supportedIdentityProviders("COGNITO")
                    .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
                               .allowedOAuthScopes("dev/tasks")
                    .clientName(clientName)
                    .generateSecret(true)
                    .build();
            UserPoolClientType client = null;
            try {
                CreateUserPoolClientResponse response = awsClientConf.getCognitoClient().createUserPoolClient(request);
                client = response.userPoolClient();
                log.info("Client value: {}", client);
            } catch (Exception e) {
                log.error("ERROR with getClientCredentials: {}", e.getMessage());
            }
            try {
                ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
                clientCredentialsDTO.setClientId(client != null ? client.clientId() : "");
                clientCredentialsDTO.setClientSecret(client != null ? client.clientSecret() : "");
                clientCredentialsDTO.setClientName(client != null ? client.clientName() : "");
                return clientCredentialsDTO;
            } catch (Exception e) {
                log.error("mapping exception");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Uni<Void> deleteClient(String clientId) {
        return Uni.createFrom().item(() -> {
            DeleteUserPoolClientRequest request = DeleteUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build();
            awsClientConf.getCognitoClient().deleteUserPoolClient(request);
            log.info("Client with ID {} deleted successfully", clientId);
            return null;

        }).onFailure().invoke(th -> log.error("Failed to delete usage plan with id: {}", clientId, th)).replaceWithVoid();
    }

}

