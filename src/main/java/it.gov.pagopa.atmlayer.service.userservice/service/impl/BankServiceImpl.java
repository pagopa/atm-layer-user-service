package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.repository.BankRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@ApplicationScoped
@Slf4j
public class BankServiceImpl implements BankService {

    @Inject
    BankMapper bankMapper;

    @Inject
    BankRepository bankRepository;

    @Override
    @WithTransaction
    public Uni<BankEntity> insertBank(BankInsertionDTO bankInsertionDTO) {
        String acquirerId = bankInsertionDTO.getAcquirerId();
        log.info("Inserting bank with acquirerId : {}", acquirerId);

        return this.bankRepository.findAllById(bankInsertionDTO.getAcquirerId())
                .onItem()
                .transformToUni(Unchecked.function(foundBankList -> {

                    if (foundBankList.isEmpty()) {
                        String clientId = UUID.randomUUID().toString();
                        String clientSecret = UUID.randomUUID().toString();
                        BankEntity bankEntity = bankMapper.toEntityInsertion(bankInsertionDTO);
                        bankEntity.setClientId(clientId);
                        bankEntity.setClientSecret(clientSecret);
                        return bankRepository.persist(bankEntity);
                    }

                    BankEntity foundBank = foundBankList.get(0);
                    if (foundBank.getEnabled()) {
                        log.error("acquirerId {} already exists", acquirerId);
                        throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.BANK_WITH_THE_SAME_ID_ALREADY_EXISTS);
                    }
                    foundBank.setEnabled(true);
                    foundBank.setDenomination(bankInsertionDTO.getDenomination());
                    foundBank.setRateLimit(bankInsertionDTO.getRateLimit());
                    return bankRepository.persist(foundBank);
                }));
    }

    @Override
    @WithTransaction
    public Uni<BankEntity> updateBank(BankInsertionDTO bankInsertionDTO) {
        String acquirerId = bankInsertionDTO.getAcquirerId();
        log.info("Updating bank with acquirerId : {}", acquirerId);

        return this.findBankById(bankInsertionDTO.getAcquirerId())
                .onItem()
                .transformToUni(Unchecked.function(bankFound -> {
                    bankFound.setDenomination(bankInsertionDTO.getDenomination());
                    bankFound.setRateLimit(bankInsertionDTO.getRateLimit());
                    return bankRepository.persist(bankFound);
                }));
    }

    @Override
    public Uni<Void> disable(String acquirerId) {
        return this.setDisabledBankAttributes(acquirerId)
                .onItem()
                .transformToUni(disabledBank -> Uni.createFrom().voidItem());
    }

    @WithTransaction
    public Uni<BankEntity> setDisabledBankAttributes(String acquirerId) {
        return this.findBankById(acquirerId)
                .onItem()
                .transformToUni(bank -> {
                    bank.setEnabled(false);
                    return this.bankRepository.persist(bank);
                });
    }

    @Override
    @WithSession
    public Uni<BankEntity> findBankById(String acquirerId) {
        return this.bankRepository.findById(acquirerId)
                .onItem()
                .ifNull()
                .switchTo(() -> {
                    throw new AtmLayerException(Response.Status.NOT_FOUND, AppErrorCodeEnum.BANK_NOT_FOUND);
                })
                .onItem()
                .transformToUni(Unchecked.function(x -> Uni.createFrom().item(x)));
    }

    @Override
    @WithSession
    public Uni<PageInfo<BankEntity>> searchBanks(int pageIndex, int pageSize, String acquirerId, String denomination, int rateMin, int rateMax, String clientId) {
        if (rateMax < rateMin) {
            throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.RATEMIN_GREATER_THAN_RATEMAX);
        }
        Map<String, Object> filters = new HashMap<>();
        filters.put("acquirerId", acquirerId);
        filters.put("denomination", denomination);
        filters.put("rateMin", rateMin == 0 ? null : rateMin);
        filters.put("rateMax", rateMax == 0 ? null : rateMax);
        filters.put("clientId", clientId);
        filters.remove(null);
        filters.values().removeAll(Collections.singleton(null));
        filters.values().removeAll(Collections.singleton(""));
        return bankRepository.findByFilters(filters, pageIndex, pageSize);
    }

    @Override
    @WithSession
    public Uni<List<BankEntity>> getAll(){
        return this.bankRepository.findAll().list();
    }

}