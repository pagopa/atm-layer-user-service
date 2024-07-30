package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
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
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.repository.BankRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
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

    @Inject
    CognitoService cognitoService;

    @Inject
    ApiKeyService apiKeyService;

    @Override
    @WithTransaction
    public Uni<BankPresentationDTO> insertBank(BankInsertionDTO bankInsertionDTO) {
        String acquirerId = bankInsertionDTO.getAcquirerId();
        log.info("Inserting bank with acquirerId : {}", acquirerId);
        return this.bankRepository.findAllById(bankInsertionDTO.getAcquirerId())
                .onItem()
                .transformToUni(Unchecked.function(findResult -> {
                    if (!findResult.isEmpty() && Boolean.TRUE.equals(findResult.get(0).getEnabled())) {
                        log.error("acquirerId {} already exists", acquirerId);
                        throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.BANK_WITH_THE_SAME_ID_ALREADY_EXISTS);
                    }
                    return cognitoService.generateClient(bankInsertionDTO.getDenomination()).onItem().transformToUni(cognitoCredentials -> {
                        ClientCredentialsDTO createdClient = cognitoCredentials;
                        log.info("client credentials created : {}", createdClient);
                        return apiKeyService.createApiKey(createdClient.getClientName()).onItem().transformToUni(apiKey -> {
                            ApiKeyDTO apikeyCreated = apiKey;
                            log.info("apikey created : {}", apikeyCreated);
                            return apiKeyService.createUsagePlan(bankInsertionDTO, apikeyCreated.getId()).onItem().transformToUni(associatedUsagePlan -> {
                                log.info("associatedUsagePlan created : {}", associatedUsagePlan);
                                BankEntity bankEntity;
                                if (!findResult.isEmpty()) {
                                    bankEntity = findResult.get(0);
                                    bankEntity.setEnabled(true);
                                } else {
                                    bankEntity = bankMapper.toEntityInsertion(bankInsertionDTO);
                                }
                                bankEntity.setClientId(createdClient.getClientId());
                                bankEntity.setApiKeyId(apikeyCreated.getId());
                                bankEntity.setUsagePlanId(associatedUsagePlan.getId());
                                log.info("bankEntity : {}", bankEntity);
                                return bankRepository.persist(bankEntity)
                                        .onItem()
                                        .transformToUni(bank -> Uni.createFrom().item(bankMapper.toPresentationDTO(bankEntity, apikeyCreated, createdClient, associatedUsagePlan)));
                            });
                        });
                    });
                }));
    }

    @Override
    @WithTransaction
    public Uni<BankPresentationDTO> updateBank(BankInsertionDTO input) {
        String acquirerId = input.getAcquirerId();
        log.info("Updating bank with acquirerId : {}", acquirerId);
        return bankRepository.findById(input.getAcquirerId())
                .onItem()
                .transformToUni(Unchecked.function(bankToUpdate -> {
                    if (bankToUpdate == null) {
                        throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.BANK_NOT_FOUND);
                    }
                    return apiKeyService.updateUsagePlan(bankToUpdate.getUsagePlanId(), new UsagePlanUpdateDTO(input.getRateLimit(), input.getBurstLimit(), input.getLimit(), input.getPeriod()))
                            .onItem()
                            .transformToUni(usagePlan -> getStaticAWSInfo(bankToUpdate, usagePlan));
                }));
    }

    public Uni<BankPresentationDTO> getStaticAWSInfo(BankEntity bankEntity, UsagePlanDTO usagePlanDto) {
        return apiKeyService.getApiKey(bankEntity.getDenomination())
                .onItem()
                .transformToUni(apiKeyDto -> cognitoService.getClientCredentials(bankEntity.getClientId())
                        .onItem()
                        .transformToUni(clientDto ->
                                Uni.createFrom().item(bankMapper.toPresentationDTO(bankEntity, apiKeyDto, clientDto, usagePlanDto))));
    }

    @Override
    @WithSession
    public Uni<BankPresentationDTO> findByAcquirerId(String acquirerId) {
        return bankRepository.findById(acquirerId)
                .onItem()
                .transformToUni(bankEntity -> {
                    if (bankEntity == null) {
                        throw new AtmLayerException(Response.Status.NOT_FOUND, AppErrorCodeEnum.BANK_NOT_FOUND);
                    }
                    return gatherAdditionalInfo(bankEntity);
                });
    }

    private Uni<BankPresentationDTO> gatherAdditionalInfo(BankEntity bankEntity) {
        return apiKeyService.getUsagePlan(bankEntity.getUsagePlanId())
                .onItem()
                .transformToUni(usagePlanDto ->
                        apiKeyService.getApiKey(bankEntity.getDenomination())
                                .onItem()
                                .transformToUni(apiKeyDto ->
                                        cognitoService.getClientCredentials(bankEntity.getClientId())
                                                .onItem()
                                                .transform(clientDto ->
                                                        bankMapper.toPresentationDTO(bankEntity, apiKeyDto, clientDto, usagePlanDto)
                                                )
                                )
                );
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
    public Uni<PageInfo<BankEntity>> searchBanks(int pageIndex, int pageSize, String acquirerId, String denomination, String clientId) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("acquirerId", acquirerId);
        filters.put("denomination", denomination);
        filters.put("clientId", clientId);
        filters.remove(null);
        filters.values().removeAll(Collections.singleton(null));
        filters.values().removeAll(Collections.singleton(""));
        return bankRepository.findByFilters(filters, pageIndex, pageSize);
    }

    @Override
    @WithSession
    public Uni<List<BankEntity>> getAll() {
        return this.bankRepository.findAll().list();
    }

    @Override
    public Uni<Void> disable(String acquirerId) {
        return this.findBankById(acquirerId)
                .onItem().transformToUni(bankEntity -> {
                    Uni<Void> deleteUsagePlanUni;

                    if (bankEntity.getUsagePlanId() != null) {
                        deleteUsagePlanUni = apiKeyService.deleteUsagePlan(bankEntity.getUsagePlanId());
                    } else {
                        deleteUsagePlanUni = Uni.createFrom().voidItem();
                    }

                    return deleteUsagePlanUni
                            .chain(usagePlan -> apiKeyService.deleteApiKey(bankEntity.getApiKeyId()))
                            .chain(apiKey -> cognitoService.deleteClient(bankEntity.getClientId()))
                            .chain(client -> {
                                bankEntity.setEnabled(false);
                                bankEntity.setClientId(null);
                                bankEntity.setUsagePlanId(null);
                                bankEntity.setApiKeyId(null);
                                return bankRepository.persist(bankEntity)
                                        .onFailure().invoke(Unchecked.consumer(th -> {
                                            throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.DATABASE_TRANSACTION_ERROR);
                                        }));
                            })
                            .replaceWithVoid();
                })
                .onFailure().invoke(Unchecked.consumer(th -> {
                    throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.ATML_USER_SERVICE_500);
                }))
                .replaceWithVoid();
    }



}