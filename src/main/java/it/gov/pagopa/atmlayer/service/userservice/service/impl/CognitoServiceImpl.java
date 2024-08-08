package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.configuration.CognitoClientConf;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;


@ApplicationScoped
@Slf4j
public class CognitoServiceImpl implements CognitoService {
    @Inject
    CognitoClientConf cognitoClientConf;

    @ConfigProperty(name = "cognito.user-pool.id")
    String userPoolId;

    @ConfigProperty(name = "cognito.scopes")
    String scopes;

    @Override
    public Uni<ClientCredentialsDTO> getClientCredentials(String clientId) {
        return Uni.createFrom().item(() -> {
            DescribeUserPoolClientRequest request = DescribeUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build();
            UserPoolClientType client = null;
            try {
                DescribeUserPoolClientResponse response = cognitoClientConf.getCognitoClient().describeUserPoolClient(request);
                client = response.userPoolClient();
                log.info("Client value: {}", client);
            } catch (Exception e) {
                log.error("ERROR with getClientCredentials: {}", e.getMessage());
                throw new AtmLayerException(("La richiesta di GetClientCredentials su AWS non è andata a buon fine"), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            if (client == null || client.sdkFields().isEmpty()){
                throw new AtmLayerException("Errore nella richiesta di DescribeUserPoolClient: risposta nulla o con campi vuoti", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
            clientCredentialsDTO.setClientId(client.clientId());
            clientCredentialsDTO.setClientSecret(client.clientSecret());
            clientCredentialsDTO.setClientName(client.clientName());
            return clientCredentialsDTO;
        });
    }

    @Override
    public Uni<ClientCredentialsDTO> generateClient(String clientName) {
        return Uni.createFrom().item(() -> {
                       CreateUserPoolClientRequest request = CreateUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                               .allowedOAuthFlowsUserPoolClient(true)
                    .supportedIdentityProviders("COGNITO")
                    .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
                               .allowedOAuthScopes(scopes)
                    .clientName(clientName)
                    .generateSecret(true)
                    .build();
            UserPoolClientType client = null;
            try {
                CreateUserPoolClientResponse response = cognitoClientConf.getCognitoClient().createUserPoolClient(request);
                client = response.userPoolClient();
                log.info("Client value: {}", client);
            } catch (Exception e) {
                log.error("La richiesta di CreateUserPoolClient su AWS non è andata a buon fine: {}", e.getMessage());
                throw new AtmLayerException(("La richiesta di CreateUserPoolClient su AWS non è andata a buon fine"), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            if (client == null || client.sdkFields().isEmpty()){
                throw new AtmLayerException("Errore nella richiesta di CreateUserPoolClient: risposta nulla o con campi vuoti", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
            clientCredentialsDTO.setClientId(client.clientId());
            clientCredentialsDTO.setClientSecret(client.clientSecret());
            clientCredentialsDTO.setClientName(client.clientName());
            return clientCredentialsDTO;
        });
    }

    @Override
    public Uni<ClientCredentialsDTO> updateClientName(String clientId, String clientName) {
        UpdateUserPoolClientRequest request = UpdateUserPoolClientRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .clientName(clientName)
                .build();
        UserPoolClientType client = null;
        try {
            UpdateUserPoolClientResponse response = cognitoClientConf.getCognitoClient().updateUserPoolClient(request);
            client = response.userPoolClient();
            log.info("Client value: {}", client);
        } catch (Exception e) {
            log.error("La richiesta di UpdateUserPoolClient su AWS non è andata a buon fine: {}", e.getMessage());
            throw new AtmLayerException(("La richiesta di UpdateUserPoolClient su AWS non è andata a buon fine"), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
        }
        if (client == null || client.sdkFields().isEmpty()){
            throw new AtmLayerException("Errore nella richiesta di UpdateUserPoolClient: risposta nulla o con campi vuoti", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
        }
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO();
        clientCredentialsDTO.setClientId(client.clientId());
        clientCredentialsDTO.setClientSecret(client.clientSecret());
        clientCredentialsDTO.setClientName(client.clientName());
        return Uni.createFrom().item(clientCredentialsDTO);
    }

    @Override
    public Uni<Void> deleteClient(String clientId) {
        return Uni.createFrom().item(() -> {
            DeleteUserPoolClientRequest request = DeleteUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build();
            cognitoClientConf.getCognitoClient().deleteUserPoolClient(request);
            log.info("Client with ID {} deleted successfully", clientId);
            return null;

        }).onFailure().invoke(th -> log.error("Failed to delete usage plan with id: {}", clientId, th)).replaceWithVoid();
    }

}

