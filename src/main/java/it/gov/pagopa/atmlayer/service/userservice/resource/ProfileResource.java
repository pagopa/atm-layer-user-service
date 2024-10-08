package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ProfileMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ValidationErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.service.ProfileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@ApplicationScoped
@Slf4j
@Path("/profile")
public class ProfileResource {

    @Inject
    ProfileService profileService;
    @Inject
    ProfileMapper profileMapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ProfileDTO>> getAll() {
        return this.profileService.getAll()
                .onItem()
                .transform(Unchecked.function(list -> {
                    if (list.isEmpty()) {
                        log.info("No Profiles saved in database");
                    }
                    return profileMapper.toDTOList(list);
                }));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createProfile", summary = "Restituisce il profilo creato e lo salva nel database", description = "Salva sul database il profilo e lo restituisce")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ProfileDTO> createProfile(@Valid ProfileCreationDto profile) {
        return this.profileService.createProfile(profile)
                .onItem()
                .transform(savedProfile -> profileMapper.toDto(savedProfile));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateProfile", summary = "Restituisce il profilo aggiornato persistito sul database", description = "Aggiorna il profilo aggiornato persistito sul database")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Profilo non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ProfileDTO> updateProfile(@Valid ProfileCreationDto profile) {
        return this.profileService.updateProfile(profile)
                .onItem()
                .transform(updatedProfile -> profileMapper.toDto(updatedProfile));
    }

}

