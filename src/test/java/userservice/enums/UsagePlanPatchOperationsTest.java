package userservice.enums;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.enums.UsagePlanPatchOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class UsagePlanPatchOperationsTest {

    @Test
    void testEnumValues() {
        assertEquals("/throttle", UsagePlanPatchOperations.THROTTLE.getPath());
        assertEquals("/quota", UsagePlanPatchOperations.QUOTA.getPath());
        assertEquals("/throttle/rateLimit", UsagePlanPatchOperations.RATE_LIMIT.getPath());
        assertEquals("/throttle/burstLimit", UsagePlanPatchOperations.BURST_LIMIT.getPath());
        assertEquals("/quota/limit", UsagePlanPatchOperations.QUOTA_LIMIT.getPath());
        assertEquals("/quota/period", UsagePlanPatchOperations.QUOTA_PERIOD.getPath());
        assertEquals("/name", UsagePlanPatchOperations.NAME.getPath());
        assertEquals("/description", UsagePlanPatchOperations.DESCRIPTION.getPath());
    }

    @Test
    void testEnumLength() {
        assertEquals(8, UsagePlanPatchOperations.values().length);
    }

    @Test
    void testEnumOrder() {
        UsagePlanPatchOperations[] expectedOrder = {
                UsagePlanPatchOperations.THROTTLE,
                UsagePlanPatchOperations.QUOTA,
                UsagePlanPatchOperations.RATE_LIMIT,
                UsagePlanPatchOperations.BURST_LIMIT,
                UsagePlanPatchOperations.QUOTA_LIMIT,
                UsagePlanPatchOperations.QUOTA_PERIOD,
                UsagePlanPatchOperations.NAME,
                UsagePlanPatchOperations.DESCRIPTION
        };
        assertArrayEquals(expectedOrder, UsagePlanPatchOperations.values());
    }

}
