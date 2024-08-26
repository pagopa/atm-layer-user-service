package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.model.*;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.ApiKeyServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@ApplicationScoped
@Path("/api-gateway")
public class ApiKeyResource {

    @Inject
    ApiKeyServiceImpl apiKeyService;

    @GET
    @Path("/api-key/retrieve/{apiKeyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getApiKey", summary = "Restituisce l'api-key se presente", description = "Esegue la GET su AWS e restituisce l'api-key trovata tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiKeyDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Token non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ApiKeyDTO> getApiKey(@PathParam("apiKeyId") @Schema(format = "byte", maxLength = 255) String apiKeyId) {
        return apiKeyService.getApiKey(apiKeyId);
    }

    @DELETE
    @Path("/api-key/delete/{apiKeyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteApiKey", summary = "Cancella l'api-key se presente", description = "Esegue la DELETE su AWS")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Token non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> deleteApiKey(@PathParam("apiKeyId") @Size(max=255) String apiKeyId) {
        return apiKeyService.deleteApiKey(apiKeyId);
    }

    @POST
    @Path("/api-key/generate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "generateApiKey", summary = "Restituisce l'api-key creata", description = "Esegue la CREATE api-key su AWS")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiKeyDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Token non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ApiKeyDTO> generateApiKey(@HeaderParam("clientName") @Schema(format = "byte", maxLength = 255) String clientName) {
        return apiKeyService.createApiKey(clientName);
    }

    @GET
    @Path("/usage-plan/{usagePlanId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getUsagePlan", summary = "Restituisce l'usage plan se presente", description = "Esegue la GET su AWS e restituisce l'usage plan trovato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsagePlanDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "usage plan non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UsagePlanDTO> getUsagePlan(@PathParam("usagePlanId") @Schema(format = "byte", maxLength = 255) String usagePlanId) {
        return apiKeyService.getUsagePlan(usagePlanId);
    }

    @DELETE
    @Path("/usage-plan/{usagePlanId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteUsagePlan", summary = "Cancella l'usage plan se presente", description = "Esegue la DELETE su AWS")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "usage plan non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> deleteUsagePlan(@PathParam("usagePlanId") @Schema(format = "byte", maxLength = 255) String usagePlanId) {
        return apiKeyService.deleteUsagePlan(usagePlanId);
    }

    @PUT
    @Path("/usage-plan/{usagePlanId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateUsagePlan", summary = "Restituisce l'usage plan aggiornato", description = "Esegue la PATCH su AWS e restituisce l'usage plan trovato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsagePlanDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "usage plan non trovato su AWS con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UsagePlanDTO> updateUsagePlan(@PathParam("usagePlanId") @Schema(format = "byte", maxLength = 255) String usagePlanId, @RequestBody UsagePlanUpdateDTO usagePlanUpdateDTO) {
        return apiKeyService.updateUsagePlan(usagePlanId, usagePlanUpdateDTO);
    }

}
