package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserInsertionWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.UserWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.mapper.UserMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ValidationErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.repository.UserRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.UserService;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@ApplicationScoped
@Path("/users")
@Tag(name = "User")
@Slf4j
public class UserResource {

    @Inject
    UserMapper userMapper;

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @POST
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createUser", summary = "Restituisce lo user creato e lo salva nel database", description = "Salva sul database lo user e lo restituisce")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWithProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> insert(@RequestBody(required = true) @Valid UserInsertionDTO userInsertionDTO) {
        return this.userService.insertUser(userInsertionDTO)
                .onItem()
                .transformToUni(insertedUser -> Uni.createFrom().item(this.userMapper.toProfilesDTO(insertedUser)));
    }

    @POST
    @Path("/first-access/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "firstAccessUser", summary = "Restituisce lo user creato e lo salva nel database", description = "Salva sul database lo user e lo restituisce, se è il primo utente viene automaticamente aggiunto il ruolo di gestione utenti")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWithProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> firstAccess(@PathParam("userId") @Schema(format = "byte", maxLength = 255) String userId) {
        return this.userService.checkFirstAccess(userId)
                .onItem()
                .transformToUni(insertedProfiles -> userService.getById(userId))
                .onItem()
                .transformToUni(user -> Uni.createFrom().item(this.userMapper.toProfilesDTO(user)));
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateUser", summary = "Restituisce lo user aggiornato e lo salva nel database", description = "Salva sul database lo user aggiornato e lo restituisce, questa operazione comprende solo i campi dell'entità User")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWithProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> update(@RequestBody(required = true) @Valid UserInsertionDTO input) {
        return this.userService.updateUser(input.getUserId(), input.getName(), input.getSurname())
                .onItem()
                .transformToUni(updatedUser -> Uni.createFrom().item(userMapper.toProfilesDTO(updatedUser)));
    }

    @PUT
    @Path("/update-with-profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateUserWithProfiles", summary = "Restituisce lo user aggiornato e lo salva nel database", description = "Salva sul database lo user aggiornato e lo restituisce, questa operazione comprende sia i campi dell'entità User che Profile")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWithProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> updateWithProfiles(@RequestBody(required = true) @Valid UserInsertionWithProfilesDTO userInsertionWithProfilesDTO) {
        return this.userService.updateWithProfiles(userInsertionWithProfilesDTO)
                .onItem()
                .transformToUni(updatedUser -> userRepository.findByIdCustom(userInsertionWithProfilesDTO.getUserId()))
                .onItem()
                .transformToUni(insertedUser -> Uni.createFrom().item(this.userMapper.toProfilesDTO(insertedUser)));
    }


    @POST
    @Path("/insert-with-profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createUserWithProfiles", summary = "Restituisce lo user creato e lo salva nel database", description = "Salva sul database lo user creato e lo restituisce, questa operazione comprende sia i campi dell'entità User che Profile")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserWithProfilesDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> insertWithProfiles(@RequestBody(required = true) @Valid UserInsertionWithProfilesDTO userInsertionWithProfilesDTO) {
        return this.userService.insertUserWithProfiles(userInsertionWithProfilesDTO)
                .onItem()
                .transformToUni(insertedProfiles -> userRepository.findByIdCustom(userInsertionWithProfilesDTO.getUserId()))
                .onItem()
                .transformToUni(insertedUser -> Uni.createFrom().item(this.userMapper.toProfilesDTO(insertedUser)));
    }


    @DELETE
    @Path("/delete/userId/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteUser", summary = "Cancella lo user salvato sul database", description = "Cancella lo user selezionato dal database")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non presente sul database.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> delete(@PathParam("userId") @Schema(format = "byte", maxLength = 255) String userId) {
        return this.userService.deleteUser(userId)
                .onItem()
                .ignore()
                .andSwitchTo(Uni.createFrom().voidItem());
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getUser", summary = "Restituisce lo user presente sul database", description = "Esegue la query sul database restituisce lo user trovato tramite i valori di input")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "User non trovato sul database con i valori di input inseriti", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<UserWithProfilesDTO> getByIdWithProfiles(@PathParam("userId") @Schema(format = "byte", maxLength = 255) String userId) {
        return this.userService.getById(userId)
                .onItem()
                .transform(foundUser -> userMapper.toProfilesDTO(foundUser));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter")
    @Operation(operationId = "getUserFiltered", summary = "Restituisce la lista di user filtrati", description = "Cerca sul database gli user corrispondenti ai filtri inseriti")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(schema = @Schema(implementation = PageInfo.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Nessuno user corrisponde ai filtri di ricerca", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<PageInfo<UserWithProfilesDTO>> getUserFiltered(@QueryParam("pageIndex") @DefaultValue("0") @Parameter(required = true, schema = @Schema(minimum = "0", maximum = "10000")) int pageIndex,
                                                              @QueryParam("pageSize") @DefaultValue("10") @Parameter(required = true, schema = @Schema(minimum = "1", maximum = "100")) int pageSize,
                                                              @QueryParam("name") @Schema(format = "byte", maxLength = 255) String name,
                                                              @QueryParam("surname") @Schema(format = "byte", maxLength = 255) String surname,
                                                              @QueryParam("userId") @Schema(format = "byte", maxLength = 255) String userId,
                                                              @QueryParam("profileId") @Schema(minimum = "1", maximum = "10") int profileId) {
        return userService.getUserFiltered(pageIndex, pageSize, name, surname, userId, profileId)
                .onItem()
                .transformToUni(Unchecked.function(pagedList -> {
                    if (pagedList.getResults().isEmpty()) {
                        log.info("No user found for the applied filters");
                    }
                    return Uni.createFrom().item(userMapper.toFrontEndDTOListPaged(pagedList));
                }));
    }

}
