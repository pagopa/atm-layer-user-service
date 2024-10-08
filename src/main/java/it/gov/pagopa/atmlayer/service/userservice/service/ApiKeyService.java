package it.gov.pagopa.atmlayer.service.userservice.service;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanUpdateDTO;

public interface ApiKeyService {

    Uni<ApiKeyDTO> createApiKey(String apiKeyValue, String clientName);

    Uni<ApiKeyDTO> getApiKey(String clientName);

    Uni<Void> deleteApiKey(String apiKeyId);

    Uni<UsagePlanDTO> createUsagePlan(BankInsertionDTO bankInsertionDTO, String apiKeyId);

    Uni<UsagePlanDTO> getUsagePlan(String usagePlanId);

    Uni<Void> deleteUsagePlan(String usagePlanId);

    Uni<UsagePlanDTO> updateUsagePlan(String usagePlanId, UsagePlanUpdateDTO usagePlanUpdateDTO);
}
