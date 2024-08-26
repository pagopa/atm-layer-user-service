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
import jakarta.validation.constraints.Size;
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

    @GET
    @Path("/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getProfile", summary = "Restituisce il profilo presente sul database", description = "Esegue la query sul database restituisce il profilo trovato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Profilo non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<ProfileDTO> retrieveProfile(@PathParam("profileId") @Schema(minimum = "1", maximum = "30") int profileId) {
        return this.profileService.retrieveProfile(profileId)
                .onItem()
                .transform(retrievedProfile -> profileMapper.toDto(retrievedProfile));
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

    @DELETE
    @Path("/{profileId}")
    @Operation(operationId = "deleteProfile", summary = "Cancella il profilo salvato sul database", description = "Cancella il profilo selezionato dal database")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Profile non presente sul database.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> deleteProfile(@PathParam("profileId") @Schema(minimum = "1", maximum = "30")int profileId) {
        return this.profileService.deleteProfile(profileId);
    }

}

