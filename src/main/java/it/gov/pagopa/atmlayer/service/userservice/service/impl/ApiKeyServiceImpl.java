package it.gov.pagopa.atmlayer.service.userservice.service.impl;

import io.smallrye.mutiny.Uni;
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
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.atmlayer.service.userservice.enums.UsagePlanPatchOperations.*;

@ApplicationScoped
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {
    private final ApiGatewayClient apiGatewayClient;
    @ConfigProperty(name = "api-gateway.id")
    String apiGatewayId;
    @ConfigProperty(name = "app.environment")
    String apiGatewayStage;
    @Inject
    ApiKeyMapper mapper;

    public ApiKeyServiceImpl() {
        this.apiGatewayClient = ApiGatewayClient.builder()
                .httpClient(ApacheHttpClient.builder().build())
                .region(Region.EU_SOUTH_1)
                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .build();
    }

    @Override
    public Uni<ApiKeyDTO> createApiKey(String clientName) {
        return Uni.createFrom().item(() -> {
            CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                    .name(clientName + "-api-key")
                    .enabled(true)
                    .build();
            CreateApiKeyResponse response = apiGatewayClient.createApiKey(request);
            if (response.sdkFields().isEmpty()) {
                throw new AtmLayerException("La richiesta di CreateApiKey su AWS non è andata a buon fine", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR);
            }
            return new ApiKeyDTO(response.id(), response.value(), response.name()); // La chiave API viene restituita come ID
        });
    }

    @Override
    public Uni<ApiKeyDTO> getApiKey(String apiKeyId) {
        return Uni.createFrom().item(() -> {
            GetApiKeyRequest request = GetApiKeyRequest.builder()
                    .apiKey(apiKeyId)
                    .includeValue(true)
                    .build();
            GetApiKeyResponse response = apiGatewayClient.getApiKey(request);
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
            apiGatewayClient.deleteApiKey(request);
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

            if (bankInsertionDTO.getBurstLimit() != null && bankInsertionDTO.getRateLimit() != null) {
                usagePlanRequestBuilder.throttle(t -> t.burstLimit(bankInsertionDTO.getBurstLimit()).rateLimit(bankInsertionDTO.getRateLimit()));
            }

            usagePlanRequestBuilder.apiStages(ApiStage.builder().apiId(apiGatewayId).stage(apiGatewayStage).build());

            CreateUsagePlanRequest usagePlanRequest = usagePlanRequestBuilder.build();

            CreateUsagePlanResponse usagePlanResponse = apiGatewayClient.createUsagePlan(usagePlanRequest);

            UsagePlanDTO usagePlan = mapper.usagePlanCreateToDto(usagePlanResponse);

            CreateUsagePlanKeyRequest usagePlanKeyRequest = CreateUsagePlanKeyRequest.builder()
                    .usagePlanId(usagePlanResponse.id())
                    .keyId(apiKeyId)
                    .keyType("API_KEY")
                    .build();
            apiGatewayClient.createUsagePlanKey(usagePlanKeyRequest);

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

            GetUsagePlanResponse usagePlanResponse = apiGatewayClient.getUsagePlan(usagePlanRequest);
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
            UpdateUsagePlanResponse updatedPlan = apiGatewayClient.updateUsagePlan(updateUsagePlanRequest);
            return mapper.usagePlanUpdateToDto(updatedPlan);

        }).onFailure().transform(error -> new AtmLayerException("La richiesta di UpdateUsagePlan su AWS non è andata a buon fine", Response.Status.INTERNAL_SERVER_ERROR, AppErrorCodeEnum.AWS_OPERATION_ERROR));
    }

    public List<PatchOperation> buildPatchOperation(UsagePlanUpdateDTO updateDTO) {
        if ((updateDTO.getBurstLimit() == null && updateDTO.getRateLimit() != null) || (updateDTO.getBurstLimit() != null && updateDTO.getRateLimit() == null)) {
            throw new AtmLayerException("Non è possibile specificare solo uno tra rate limit e burst limit", Response.Status.BAD_REQUEST, AppErrorCodeEnum.INVALID_PAYLOAD);
        }
        if ((updateDTO.getQuotaLimit() == null && updateDTO.getQuotaPeriod() != null) || (updateDTO.getQuotaLimit() != null && updateDTO.getQuotaPeriod() == null)) {
            throw new AtmLayerException("Non è possibile specificare solo uno tra quota limit e quota period", Response.Status.BAD_REQUEST, AppErrorCodeEnum.INVALID_PAYLOAD);
        }
        log.info("-------- preparing patchOperations");
        // Build patch operations to update the usage plan
        List<PatchOperation> patchOperations = new ArrayList<>();
        Optional.ofNullable(updateDTO.getQuotaLimit()).ifPresentOrElse(
                quotaLimit -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(QUOTA_LIMIT.getPath()).value(String.valueOf(quotaLimit)).build()),
                () -> patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path(QUOTA.getPath()).build()));
        Optional.ofNullable(updateDTO.getQuotaPeriod()).ifPresent(quotaPeriod -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(QUOTA_PERIOD.getPath()).value(quotaPeriod.toString()).build()));
        Optional.ofNullable(updateDTO.getRateLimit()).ifPresentOrElse(
                rateLimit -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(RATE_LIMIT.getPath()).value(String.valueOf(rateLimit)).build()),
                () -> patchOperations.add(PatchOperation.builder().op(Op.REMOVE).path(THROTTLE.getPath()).build()));
        Optional.ofNullable(updateDTO.getBurstLimit()).ifPresent(burstLimit -> patchOperations.add(PatchOperation.builder().op(Op.REPLACE).path(BURST_LIMIT.getPath()).value(String.valueOf(burstLimit)).build()));
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
            apiGatewayClient.updateUsagePlan(updateUsagePlanRequest);

            DeleteUsagePlanRequest usagePlanRequest = DeleteUsagePlanRequest.builder()
                    .usagePlanId(usagePlanId)
                    .build();

            DeleteUsagePlanResponse usagePlanResponse = apiGatewayClient.deleteUsagePlan(usagePlanRequest);

            log.info("Usage plan: {}", usagePlanResponse);
            return null;
        }).onFailure().invoke(th -> log.error("Failed to delete usage plan with id: {}", usagePlanId, th)).replaceWithVoid();
    }
}
