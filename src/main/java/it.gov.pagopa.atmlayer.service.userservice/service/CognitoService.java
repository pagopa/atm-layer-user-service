package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;

public interface CognitoService {

    Uni<ClientCredentialsDTO> getClientCredentials(String clientId);

    Uni<ClientCredentialsDTO> generateClient(String clientName);

    Uni<Void> deleteClient(String clientId);
}
