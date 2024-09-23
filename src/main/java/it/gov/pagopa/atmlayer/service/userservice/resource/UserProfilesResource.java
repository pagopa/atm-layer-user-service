package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfilesPK;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserProfilesMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ValidationErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.service.UserProfilesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@ApplicationScoped
@Path("/user_profiles")
@Tag(name = "User Profiles")
@Slf4j
public class UserProfilesResource {

    @Inject
    UserProfilesMapper userProfilesMapper;

    @Inject
    UserProfilesService userProfilesService;

    @POST
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createUserProfile", summary = "Restituisce le associazioni tra user e profili create e le salva nel database", description = "Salva sul database le associazioni tra profilo e user e le restituisce")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, maxItems = 30, implementation = UserProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<List<UserProfilesDTO>> insert(@RequestBody(required = true) @Valid UserProfilesInsertionDTO userProfilesInsertionDTO) {
        return this.userProfilesService.insertUserProfiles(userProfilesInsertionDTO)
                .onItem()
                .transform(insertedUserProfiles -> userProfilesMapper.toDtoList(insertedUserProfiles));
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateUserProfile", summary = "Aggiorna le associazioni tra user e profili sul database e le restituisce", description = "Salva sul database le associazioni tra profilo e user aggiornate e le restituisce")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, maxItems = 30, implementation = UserProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<List<UserProfilesDTO>> update(@RequestBody(required = true) @Valid UserProfilesInsertionDTO userProfilesInsertionDTO) {
        return this.userProfilesService.updateUserProfiles(userProfilesInsertionDTO)
                .onItem()
                .transform(updatedUserProfiles -> userProfilesMapper.toDtoList(updatedUserProfiles));
    }

}
