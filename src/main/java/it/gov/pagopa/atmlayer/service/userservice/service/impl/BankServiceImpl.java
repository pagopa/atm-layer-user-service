package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.*;
import it.gov.pagopa.atmlayer.service.userservice.repository.BankRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        return this.bankRepository.findAllById(acquirerId)
                .onItem()
                .transformToUni(findResult -> {
                    if (!findResult.isEmpty() && Boolean.TRUE.equals(findResult.get(0).getEnabled())) {
                        log.error("acquirerId {} already exists", acquirerId);
                        throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.BANK_WITH_THE_SAME_ID_ALREADY_EXISTS);
                    }

                    BankEntity bankEntity;
                    if (!findResult.isEmpty()) {
                        bankEntity = findResult.get(0);
                        bankEntity.setEnabled(true);
                        bankEntity.setDenomination(bankInsertionDTO.getDenomination());
                    } else {
                        bankEntity = bankMapper.toEntityInsertion(bankInsertionDTO);
                    }

                    return cognitoService.generateClient(bankInsertionDTO.getDenomination())
                            .onItem()
                            .transformToUni(cognitoCredentials -> {
                                ClientCredentialsDTO createdClient = cognitoCredentials;
                                log.info("client credentials created : {}", createdClient);

                                return apiKeyService.createApiKey(createdClient.getClientId(), createdClient.getClientName())
                                        .onItem()
                                        .transformToUni(apiKey -> {
                                            ApiKeyDTO apikeyCreated = apiKey;
                                            log.info("apikey created : {}", apiKey);

                                            return apiKeyService.createUsagePlan(bankInsertionDTO, apikeyCreated.getId())
                                                    .onItem()
                                                    .transformToUni(associatedUsagePlan -> {
                                                        log.info("associatedUsagePlan created : {}", associatedUsagePlan);

                                                        bankEntity.setClientId(createdClient.getClientId());
                                                        bankEntity.setApiKeyId(apikeyCreated.getId());
                                                        bankEntity.setUsagePlanId(associatedUsagePlan.getId());
                                                        log.info("bankEntity : {}", bankEntity);

                                                        return bankRepository.persist(bankEntity)
                                                                .onItem()
                                                                .transform(bank -> bankMapper.toPresentationDTO(bankEntity, apikeyCreated, createdClient, associatedUsagePlan))
                                                                .onFailure().recoverWithUni(throwable -> rollbackUsagePlanCreation(apikeyCreated)
                                                                        .onItem().transformToUni(v -> Uni.createFrom().failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR))));

                                                    })
                                                    .onFailure().recoverWithUni(throwable -> rollbackApiKeyCreation(apikeyCreated)
                                                            .onItem().transformToUni(v -> Uni.createFrom().failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR))));
                                        })
                                        .onFailure().recoverWithUni(throwable -> rollbackAppClientCreation(createdClient)
                                                .onItem().transformToUni(v -> Uni.createFrom().failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR))));
                            })
                            .onFailure().recoverWithUni(throwable -> Uni.createFrom().failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)));
                });
    }

    @WithTransaction
    public Uni<Void> rollbackAppClientCreation(ClientCredentialsDTO clientCredentials) {
        return cognitoService.deleteClient(clientCredentials.getClientId())
                .onItem().invoke(() -> log.info("Rollback: Cognito Client deleted."))
                .replaceWith(Uni.createFrom().voidItem());
    }

    @WithTransaction
    public Uni<Void> rollbackApiKeyCreation(ApiKeyDTO apiKeyDTO) {
        return apiKeyService.deleteApiKey(apiKeyDTO.getId())
                .onItem().invoke(() -> log.info("Rollback: API Key deleted."))
                .replaceWith(Uni.createFrom().voidItem());
    }

    @WithTransaction
    public Uni<Void> rollbackUsagePlanCreation(ApiKeyDTO apiKey) {
        return apiKeyService.deleteUsagePlan(apiKey.getId())
                .onItem().invoke(() -> log.info("Rollback: Usage Plan deleted."))
                .replaceWith(Uni.createFrom().voidItem());
    }



    @Override
    @WithTransaction
    public Uni<BankPresentationDTO> updateBank(BankUpdateDTO input) {
        String acquirerId = input.getAcquirerId();
        log.info("Updating bank with acquirerId : {}", acquirerId);
        return bankRepository.findById(input.getAcquirerId())
                .onItem()
                .transformToUni(Unchecked.function(bankToUpdate -> {
                    if (bankToUpdate == null) {
                        throw new AtmLayerException(Response.Status.BAD_REQUEST, AppErrorCodeEnum.BANK_NOT_FOUND);
                    }
                    if (!Objects.equals(bankToUpdate.getDenomination(), input.getDenomination())){
                        String oldName = bankToUpdate.getDenomination();
                        bankToUpdate.setDenomination(input.getDenomination());
                        return bankRepository.persist(bankToUpdate)
                                .onItem()
                                .transformToUni(bankWithUpdatedName -> cognitoService.updateClientName(bankWithUpdatedName.getClientId(), bankWithUpdatedName.getDenomination())
                                        .onItem()
                                        .transformToUni(updatedClientCredentials ->
                                                apiKeyService.updateUsagePlan(bankToUpdate.getUsagePlanId(), new UsagePlanUpdateDTO(input.getRateLimit(), input.getLimit(), input.getPeriod()))
                                                        .onItem()
                                                        .transformToUni(updatedUsagePlan -> getStaticAWSInfo(bankWithUpdatedName, updatedClientCredentials, updatedUsagePlan))
                                                        .onFailure()
                                                        .recoverWithUni(throwable -> cognitoService.updateClientName(bankToUpdate.getClientId(), oldName)
                                                                .onItem()
                                                                .transformToUni(rollBackClient -> Uni.createFrom()
                                                                        .failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)))))
                                        .onFailure().recoverWithUni(throwable -> {
                                            bankToUpdate.setDenomination(oldName);
                                            return bankRepository.persist(bankToUpdate)
                                                    .onItem().transformToUni(rollBackEntity -> Uni.createFrom()
                                                            .failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)));
                                        }))
                                .onFailure().recoverWithUni(throwable -> Uni.createFrom()
                                        .failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.ATML_USER_SERVICE_500)));
                    }
                    return apiKeyService.updateUsagePlan(bankToUpdate.getUsagePlanId(), new UsagePlanUpdateDTO(input.getRateLimit(), input.getLimit(), input.getPeriod()))
                            .onItem()
                            .transformToUni(usagePlan -> getStaticAWSInfo(bankToUpdate, usagePlan))
                            .onFailure()
                            .recoverWithUni(throwable -> Uni.createFrom().failure(new AtmLayerException(throwable.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR)));
                }));
    }

    public Uni<BankPresentationDTO> getStaticAWSInfo(BankEntity bankEntity) {
        return apiKeyService.getUsagePlan(bankEntity.getUsagePlanId())
                .onItem()
                .transformToUni(usagePlanDto ->
                        apiKeyService.getApiKey(bankEntity.getApiKeyId())
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

    public Uni<BankPresentationDTO> getStaticAWSInfo(BankEntity bankEntity, UsagePlanDTO usagePlanDto) {
        return apiKeyService.getApiKey(bankEntity.getApiKeyId())
                .onItem()
                .transformToUni(apiKeyDto -> cognitoService.getClientCredentials(bankEntity.getClientId())
                        .onItem()
                        .transformToUni(clientDto ->
                                Uni.createFrom().item(bankMapper.toPresentationDTO(bankEntity, apiKeyDto, clientDto, usagePlanDto))));
    }

    public Uni<BankPresentationDTO> getStaticAWSInfo(BankEntity bankEntity, ClientCredentialsDTO clientDto, UsagePlanDTO usagePlanDto) {
        return apiKeyService.getApiKey(bankEntity.getApiKeyId())
                .onItem()
                .transformToUni(apiKeyDto ->
                        Uni.createFrom().item(bankMapper.toPresentationDTO(bankEntity, apiKeyDto, clientDto, usagePlanDto)));
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
                    return getStaticAWSInfo(bankEntity);
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
    @WithTransaction
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