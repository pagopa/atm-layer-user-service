package it.gov.pagopa.atmlayer.service.userservice.mapper;

import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import org.mapstruct.Mapper;
import software.amazon.awssdk.services.apigateway.model.CreateUsagePlanResponse;
import software.amazon.awssdk.services.apigateway.model.GetUsagePlanResponse;
import software.amazon.awssdk.services.apigateway.model.UpdateUsagePlanResponse;

@Mapper(componentModel = "cdi")
public abstract class ApiKeyMapper {


    public UsagePlanDTO usagePlanCreateToDto(CreateUsagePlanResponse usagePlan) {
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlan.id() != null ? usagePlan.id() : null);
        usagePlanDTO.setName(usagePlan.name() != null ? usagePlan.name() : null);
        if (usagePlan.quota() != null) {
            usagePlanDTO.setLimit(usagePlan.quota().limit());
            usagePlanDTO.setPeriod(usagePlan.quota().period());
        }
        if (usagePlan.throttle() != null) {
            usagePlanDTO.setBurstLimit(usagePlan.throttle().burstLimit());
            usagePlanDTO.setRateLimit(usagePlan.throttle().rateLimit());
        }
        return usagePlanDTO;
    }

    public UsagePlanDTO usagePlanGetToDto(GetUsagePlanResponse usagePlan) {
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlan.id() != null ? usagePlan.id() : null);
        usagePlanDTO.setName(usagePlan.name() != null ? usagePlan.name() : null);
        if (usagePlan.quota() != null) {
            usagePlanDTO.setLimit(usagePlan.quota().limit());
            usagePlanDTO.setPeriod(usagePlan.quota().period());
        }
        if (usagePlan.throttle() != null) {
            usagePlanDTO.setBurstLimit(usagePlan.throttle().burstLimit());
            usagePlanDTO.setRateLimit(usagePlan.throttle().rateLimit());
        }
        return usagePlanDTO;
    }

    public UsagePlanDTO usagePlanUpdateToDto(UpdateUsagePlanResponse usagePlan) {
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO();
        usagePlanDTO.setId(usagePlan.id() != null ? usagePlan.id() : null);
        usagePlanDTO.setName(usagePlan.name() != null ? usagePlan.name() : null);
        if (usagePlan.quota() != null) {
            usagePlanDTO.setLimit(usagePlan.quota().limit());
            usagePlanDTO.setPeriod(usagePlan.quota().period());
        }
        if (usagePlan.throttle() != null) {
            usagePlanDTO.setBurstLimit(usagePlan.throttle().burstLimit());
            usagePlanDTO.setRateLimit(usagePlan.throttle().rateLimit());
        }
        return usagePlanDTO;
    }

}
