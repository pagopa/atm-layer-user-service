package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.ValidationErrorResponse;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

@ApplicationScoped
@Path("/banks")
@Tag(name = "Bank")
@Slf4j
public class BankResource {

    @Inject
    BankService bankService;

    @Inject
    BankMapper bankMapper;

    @POST
    @Path("/insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createBank", summary = "Restituisce la banca creata", description = "Salva la banca sul database e crea lo user di cognito, l'api-key e l'eventuale usage plan")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankPresentationDTO.class)))
    @APIResponse(responseCode = "400", description = "il body della request non è valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<BankPresentationDTO> insert(@RequestBody(required = true) @Valid BankInsertionDTO bankInsertionDTO) {
        return this.bankService.insertBank(bankInsertionDTO);
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateBank", summary = "Restituisce la banca aggiornata", description = "Aggiorna sul database i valori di cognito, api-key e usage plan")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankPresentationDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Banca non presente sul database", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<BankPresentationDTO> update(@RequestBody(required = true) @Valid BankUpdateDTO bankUpdateDTO) {
        return bankService.updateBank(bankUpdateDTO);
    }

    @POST
    @Path("/disable/{acquirerId}")
    @Operation(operationId = "disableBank", summary = "Disabilita la banca selezionata", description = "Disabilita sul database e rimuove i valori di cognito, api-key e usage plan da AWS")
    @APIResponse(responseCode = "204", description = "Operazione eseguita con successo. Il processo è terminato.")
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Banca non presente sul database", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "AWS non raggiungibile.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Void> disable(@PathParam("acquirerId") @Schema(format = "byte", maxLength = 255) String acquirerId) {
        return this.bankService.disable(acquirerId);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getBankFiltered", summary = "Restituisce la lista di banche filtrate", description = "Cerca sul database le banche corrispondenti ai filtri inseriti")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(schema = @Schema(implementation = PageInfo.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Nessuna banca corrisponde ai filtri di ricerca", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<PageInfo<BankDTO>> search(@QueryParam("pageIndex") @DefaultValue("0") @Parameter(required = true, schema = @Schema(minimum = "0", maximum = "10000")) int pageIndex,
                                         @QueryParam("pageSize") @DefaultValue("10") @Parameter(required = true, schema = @Schema(minimum = "1", maximum = "100")) int pageSize,
                                         @QueryParam("acquirerId") @Schema(format = "byte", maxLength = 255) String acquirerId,
                                         @QueryParam("denomination") @Schema(format = "byte", maxLength = 255) String denomination,
                                         @QueryParam("clientId") @Schema(format = "byte", maxLength = 255) String clientId) {
        return this.bankService.searchBanks(pageIndex, pageSize, acquirerId, denomination, clientId)
                .onItem()
                .transform(Unchecked.function(pagedList -> {
                    if (pagedList.getResults().isEmpty()) {
                        log.info("No Bank Entity meets the applied filters");
                    }
                    return bankMapper.toDtoPaged(pagedList);
                }));
    }

    @GET
    @Path("/{acquirerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getBank", summary = "Restituisce la banca mediante acquirerId", description = "Cerca sul database la banca con l'acquirerId specificato")
    @APIResponse(responseCode = "200", description = "Operazione eseguita con successo. Il processo è terminato.", content = @Content(schema = @Schema(implementation = BankPresentationDTO.class)))
    @APIResponse(responseCode = "400", description = "Uno o più valori di input non valorizzati correttamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Nessuna banca corrisponde ai filtri di ricerca", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "500", description = "Errore interno.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<BankPresentationDTO> getBank(@PathParam("acquirerId") @Schema(format = "byte", maxLength = 255) String acquirerId) {
        return bankService.findByAcquirerId(acquirerId);
    }

}