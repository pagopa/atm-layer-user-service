package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UsagePlanPatchOperations {

    THROTTLE("/throttle"),
    QUOTA("/quota"),
    RATE_LIMIT("/throttle/rateLimit"),
    BURST_LIMIT("/throttle/burstLimit"),
    QUOTA_LIMIT("/quota/limit"),
    QUOTA_PERIOD("/quota/period"),
    NAME("/name"),
    DESCRIPTION("/description");

    private final String path;

}
