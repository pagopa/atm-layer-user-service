package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;

import java.util.List;

public interface BankService {

    Uni<BankEntity> insertBank(BankInsertionDTO bankInsertionDTO);

    Uni<BankEntity> updateBank(BankInsertionDTO bankInsertionDTO);

    Uni<Void> disable(String acquirerId);

    Uni<PageInfo<BankEntity>> searchBanks(int pageIndex, int pageSize, String acquirerId, String denomination, String clientId);

    Uni<BankEntity> findBankById(String acquirerId);

    Uni<List<BankEntity>> getAll();


}
