package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.model.ValidationErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.CognitoServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@ApplicationScoped
@Path("/cognito")
public class CognitoResource {

    @Inject
    CognitoServiceImpl cognitoService;

    @GET
    @Path("/client-credentials/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getClient", summary = "Restituisce il Client se presente su AWS", description = "Esegue la GET su AWS e restituisce il client trovato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientCredentialsDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Client non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ClientCredentialsDTO> getClientCredentials(@PathParam("clientId") @Schema(format = "byte", maxLength = 255) String clientId) {
        return cognitoService.getClientCredentials(clientId);
    }

    @POST
    @Path("/client-credentials")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createClient", summary = "Restituisce il Client creato su AWS", description = "Esegue la POST su AWS e restituisce il client generato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientCredentialsDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ClientCredentialsDTO> generateClient(@HeaderParam("clientName") @Schema(format = "byte", maxLength = 255) @NotBlank String clientName) {
        return cognitoService.generateClient(clientName);
    }

    @DELETE
    @Path("/client-credentials/{clientId}")
    @Operation(operationId = "deleteClient", summary = "Cancella il Client presente su AWS", description = "Esegue la DELETE su AWS")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Client non presente su AWS.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> deleteClient(@PathParam("clientId") @Schema(format = "byte", maxLength = 255) String clientId) {
        return cognitoService.deleteClient(clientId);
    }

}


