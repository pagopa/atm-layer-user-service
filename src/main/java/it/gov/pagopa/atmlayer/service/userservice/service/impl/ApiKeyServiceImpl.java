package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.configuration.ApiGatewayClientConf;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ApiKeyMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.atmlayer.service.userservice.enums.UsagePlanPatchOperations.*;

@ApplicationScoped
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {
    @Inject
    ApiGatewayClientConf apiGatewayClientConf;
    @Inject
    ApiKeyMapper mapper;
    @ConfigProperty(name = "api-gateway.id")
    String apiGatewayId;
    @ConfigProperty(name = "app.environment")
    String apiGatewayStage;


    @Override
    public Uni<ApiKeyDTO> createApiKey(String apiKeyValue, String clientName) {
        return Uni.createFrom().item(() -> {
            CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                    .value(apiKeyValue)
                    .name(clientName + "-api-key")
                    .enabled(true)
                    .build();
            CreateApiKeyResponse response = apiGatewayClientConf.getApiGatewayClient().createApiKey(request);
            if (response.sdkFields().isEmpty()) {
                throw new AtmLayerException("La richiesta di CreateApiKey su AWS non è andata a buon fine", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            return new ApiKeyDTO(response.id(), response.value(), response.name());
        });
    }

    @Override
    public Uni<ApiKeyDTO> getApiKey(String apiKeyId) {
        return Uni.createFrom().item(() -> {
            GetApiKeyRequest request = GetApiKeyRequest.builder()
                    .apiKey(apiKeyId)
                    .includeValue(true)
                    .build();
            GetApiKeyResponse response = apiGatewayClientConf.getApiGatewayClient().getApiKey(request);
            if (response.sdkFields().isEmpty()) {
                throw new AtmLayerException("ApiKey non trovata", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            return new ApiKeyDTO(response.id(), response.value(), response.name());
        });
    }

    @Override
    public Uni<Void> deleteApiKey(String apiKeyId) {
        return Uni.createFrom().item(() -> {
            DeleteApiKeyRequest request = DeleteApiKeyRequest.builder()
                    .apiKey(apiKeyId)
                    .build();
            apiGatewayClientConf.getApiGatewayClient().deleteApiKey(request);
            return null;
        }).onFailure().invoke(th -> log.error("Failed to delete usage plan with id: {}", apiKeyId, th)).replaceWithVoid();
    }

    @Override
    public Uni<UsagePlanDTO> createUsagePlan(BankInsertionDTO bankInsertionDTO, String apiKeyId) {
        return Uni.createFrom().item(() -> {
            CreateUsagePlanRequest.Builder usagePlanRequestBuilder = CreateUsagePlanRequest.builder()
                    .name(Optional.ofNullable(bankInsertionDTO.getDenomination()).orElse("") + "-plan")
                    .description("Usage plan for " + Optional.ofNullable(bankInsertionDTO.getDenomination()).orElse(""));

            if (bankInsertionDTO.getLimit() != null && bankInsertionDTO.getPeriod() != null) {
                usagePlanRequestBuilder.quota(q -> q.limit(bankInsertionDTO.getLimit()).period(bankInsertionDTO.getPeriod()));
            }

            if (bankInsertionDTO.getRateLimit() != null) {
                usagePlanRequestBuilder.throttle(t -> t.burstLimit(1).rateLimit(bankInsertionDTO.getRateLimit()));
            }

            usagePlanRequestBuilder.apiStages(ApiStage.builder().apiId(apiGatewayId).stage(apiGatewayStage).build());

            CreateUsagePlanRequest usagePlanRequest = usagePlanRequestBuilder.build();

            CreateUsagePlanResponse usagePlanResponse = apiGatewayClientConf.getApiGatewayClient().createUsagePlan(usagePlanRequest);

            UsagePlanDTO usagePlan = mapper.usagePlanCreateToDto(usagePlanResponse);

            CreateUsagePlanKeyRequest usagePlanKeyRequest = CreateUsagePlanKeyRequest.builder()
                    .usagePlanId(usagePlanResponse.id())
                    .keyId(apiKeyId)
                    .keyType("API_KEY")
                    .build();
            try {
                apiGatewayClientConf.getApiGatewayClient().createUsagePlanKey(usagePlanKeyRequest);
            } catch (Exception e) {
                List<PatchOperation> patchOperations = new ArrayList<>();
                patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path("/apiStages").value(apiGatewayId + ":" + apiGatewayStage).build());
                UpdateUsagePlanRequest updateUsagePlanRequest = UpdateUsagePlanRequest.builder()
                        .usagePlanId(usagePlan.getId())
                        .patchOperations(patchOperations)
                        .build();
                apiGatewayClientConf.getApiGatewayClient().updateUsagePlan(updateUsagePlanRequest);

                DeleteUsagePlanRequest deleteUsagePlanRequest = DeleteUsagePlanRequest.builder()
                        .usagePlanId(usagePlan.getId())
                        .build();

                apiGatewayClientConf.getApiGatewayClient().deleteUsagePlan(deleteUsagePlanRequest);

                throw new AtmLayerException("La richiesta di CreateUsagePlanKey su AWS non è andata a buon fine", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            log.info("Usage plan: {}", usagePlan);
            return usagePlan;
        });
    }

    @Override
    public Uni<UsagePlanDTO> getUsagePlan(String usagePlanId) {
        return Uni.createFrom().item(() -> {
            GetUsagePlanRequest usagePlanRequest = GetUsagePlanRequest.builder()
                    .usagePlanId(usagePlanId)
                    .build();

            GetUsagePlanResponse usagePlanResponse = apiGatewayClientConf.getApiGatewayClient().getUsagePlan(usagePlanRequest);
            return mapper.usagePlanGetToDto(usagePlanResponse);
        });
    }

    @Override
    public Uni<UsagePlanDTO> updateUsagePlan(String usagePlanId, UsagePlanUpdateDTO usagePlanUpdateDTO) {
        return Uni.createFrom().item(() -> {
            UpdateUsagePlanRequest updateUsagePlanRequest = UpdateUsagePlanRequest.builder()
                    .usagePlanId(usagePlanId)
                    .patchOperations(buildPatchOperation(usagePlanUpdateDTO))
                    .build();
            UpdateUsagePlanResponse updatedPlan = apiGatewayClientConf.getApiGatewayClient().updateUsagePlan(updateUsagePlanRequest);
            return mapper.usagePlanUpdateToDto(updatedPlan);

        }).onFailure().transform(error -> new AtmLayerException("La richiesta di UpdateUsagePlan su AWS non è andata a buon fine", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR));
    }

    public List<PatchOperation> buildPatchOperation(UsagePlanUpdateDTO updateDTO) {
        if ((updateDTO.getQuotaLimit() == null && updateDTO.getQuotaPeriod() != null) || (updateDTO.getQuotaLimit() != null && updateDTO.getQuotaPeriod() == null)) {
            throw new AtmLayerException("Non è possibile specificare solo uno tra quota limit e quota period", Response.Status.BAD_REQUEST, AppErrorCodeEnum.INVALID_PAYLOAD);
        }
        log.info("-------- preparing patchOperations");
        List<PatchOperation> patchOperations = new ArrayList<>();
        Optional.ofNullable(updateDTO.getQuotaLimit()).ifPresentOrElse(
                quotaLimit -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(QUOTA_LIMIT.getPath()).value(String.valueOf(quotaLimit)).build()),
                () -> patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path(QUOTA.getPath()).build()));
        Optional.ofNullable(updateDTO.getQuotaPeriod()).ifPresent(quotaPeriod -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(QUOTA_PERIOD.getPath()).value(quotaPeriod.toString()).build()));
        Optional.ofNullable(updateDTO.getRateLimit()).ifPresentOrElse(
                rateLimit -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(RATE_LIMIT.getPath()).value(String.valueOf(rateLimit)).build()),
                () -> patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path(THROTTLE.getPath()).build()));
        log.info("-------- prepared patchOperations: {}", patchOperations);
        return patchOperations;
    }

    @Override
    public Uni<Void> deleteUsagePlan(String usagePlanId) {
        return Uni.createFrom().item(() -> {

            List<PatchOperation> patchOperations = new ArrayList<>();
            patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path("/apiStages").value(apiGatewayId + ":" + apiGatewayStage).build());
            UpdateUsagePlanRequest updateUsagePlanRequest = UpdateUsagePlanRequest.builder()
                    .usagePlanId(usagePlanId)
                    .patchOperations(patchOperations)
                    .build();
            apiGatewayClientConf.getApiGatewayClient().updateUsagePlan(updateUsagePlanRequest);

            DeleteUsagePlanRequest usagePlanRequest = DeleteUsagePlanRequest.builder()
                    .usagePlanId(usagePlanId)
                    .build();

            apiGatewayClientConf.getApiGatewayClient().deleteUsagePlan(usagePlanRequest);

            return null;
        }).onFailure().invoke(th -> log.error("Failed to delete usage plan with id: {}", usagePlanId, th)).replaceWithVoid();
    }
}
