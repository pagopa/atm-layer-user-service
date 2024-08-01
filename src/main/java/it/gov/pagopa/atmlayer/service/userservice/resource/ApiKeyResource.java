package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.ApiKeyServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@ApplicationScoped
@Path("/api-gateway")
public class ApiKeyResource {

    @Inject
    ApiKeyServiceImpl apiKeyService;

    @GET
    @Path("/api-key/retrieve/{clientName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ApiKeyDTO> getApiKey(@PathParam("clientName") String clientName) {
        return apiKeyService.getApiKey(clientName);
    }

    @DELETE
    @Path("/api-key/delete/{apiKeyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> deleteApiKey(@PathParam("apiKeyId") String apiKeyId) {
        return apiKeyService.deleteApiKey(apiKeyId);
    }

    @POST
    @Path("/api-key/generate")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ApiKeyDTO> generateApiKey(@HeaderParam("clientName") String clientName) {
        return apiKeyService.createApiKey(clientName);
    }

//    @POST
//    @Path("/create-usage-plan")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Uni<UsagePlanDTO> createUsagePlan(@RequestBody (required = true) BankInsertionDTO bankInsertionDTO) {
//        return apiKeyService.createUsagePlan(bankInsertionDTO);
//    }

    @GET
    @Path("/usage-plan/{usagePlanId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsagePlanDTO> getUsagePlan(@PathParam("usagePlanId") String usagePlanId) {
        return apiKeyService.getUsagePlan(usagePlanId);
    }

    @DELETE
    @Path("/usage-plan/{usagePlanId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> deleteUsagePlan(@PathParam("usagePlanId") String usagePlanId) {
        return apiKeyService.deleteUsagePlan(usagePlanId);
    }

    @PUT
    @Path("/usage-plan/{usagePlanId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsagePlanDTO> updateUsagePlan(@PathParam("usagePlanId") String usagePlanId, @RequestBody UsagePlanUpdateDTO usagePlanUpdateDTO) {
        return apiKeyService.updateUsagePlan(usagePlanId, usagePlanUpdateDTO);
    }

}
