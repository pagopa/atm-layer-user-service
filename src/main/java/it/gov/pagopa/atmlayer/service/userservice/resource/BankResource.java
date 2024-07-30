package it.gov.pagopa.atmlayer.service.userservice.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;

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
    public Uni<BankPresentationDTO> insert(@RequestBody(required = true) @Valid BankInsertionDTO bankInsertionDTO) {
        return this.bankService.insertBank(bankInsertionDTO);
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<BankPresentationDTO> update(@RequestBody(required = true) @Valid BankInsertionDTO bankInsertionDTO) {
        return bankService.updateBank(bankInsertionDTO);
    }

    @POST
    @Path("/disable/{acquirerId}")
    public Uni<Void> disable(@PathParam("acquirerId") String acquirerId) {
        return this.bankService.disable(acquirerId);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<PageInfo<BankDTO>> search(@QueryParam("pageIndex") @DefaultValue("0") @Parameter(required = true, schema = @Schema(type = SchemaType.INTEGER, minimum = "0")) int pageIndex,
                                         @QueryParam("pageSize") @DefaultValue("10") @Parameter(required = true, schema = @Schema(type = SchemaType.INTEGER, minimum = "1")) int pageSize,
                                         @QueryParam("acquirerId") String acquirerId,
                                         @QueryParam("denomination") String denomination,
                                         @QueryParam("clientId") String clientId) {
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
    public Uni<BankPresentationDTO> getBank(@PathParam("acquirerId") String acquirerId) {
        return bankService.findByAcquirerId(acquirerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<BankDTO>> getAll() {
        return this.bankService.getAll()
                .onItem()
                .transform(Unchecked.function(list -> {
                    if (list.isEmpty()) {
                        log.info("No banks saved in database");
                    }
                    return bankMapper.toDTOList(list);
                }));
    }

}