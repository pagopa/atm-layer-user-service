package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

public interface ApiKeyService {

    Uni<ApiKeyDTO> createApiKey(String clientName);

    Uni<ApiKeyDTO> getApiKey(String clientName);

    Uni<Void> deleteApiKey(String apiKeyId);

    Uni<UsagePlanDTO> createUsagePlan(String planName, String apiKeyId, int limit, QuotaPeriodType period, int burstLimit, double rateLimit);

    Uni<UsagePlanDTO> getUsagePlan(String usagePlanId);

    Uni<Void> deleteUsagePlan(String usagePlanId);

    Uni<UsagePlanDTO> updateUsagePlan(String usagePlanId, UsagePlanUpdateDTO usagePlanUpdateDTO);
}
