package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.mapper.ApiKeyMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigateway.model.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ApiKeyMapperTest {

    private ApiKeyMapperImpl apiKeyMapper;

    @BeforeEach
    void setUp() {
        apiKeyMapper = new ApiKeyMapperImpl();
    }

    @Test
    void testUsagePlanCreateToDto() {
        CreateUsagePlanResponse usagePlan = CreateUsagePlanResponse.builder()
                .id("test-id")
                .name("test-name")
                .quota(QuotaSettings.builder()
                        .limit(100)
                        .period(QuotaPeriodType.DAY)
                        .build())
                .throttle(ThrottleSettings.builder()
                        .burstLimit(50)
                        .rateLimit(10.0)
                        .build())
                .build();

        UsagePlanDTO expectedDto = new UsagePlanDTO(
                "test-id",
                "test-name",
                100,
                QuotaPeriodType.DAY,
                50,
                10.0
        );
        UsagePlanDTO actualDto = apiKeyMapper.usagePlanCreateToDto(usagePlan);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void testUsagePlanGetToDto() {
        GetUsagePlanResponse usagePlan = GetUsagePlanResponse.builder()
                .id("test-id")
                .name("test-name")
                .quota(QuotaSettings.builder()
                        .limit(100)
                        .period(QuotaPeriodType.DAY)
                        .build())
                .throttle(ThrottleSettings.builder()
                        .burstLimit(50)
                        .rateLimit(10.0)
                        .build())
                .build();

        UsagePlanDTO expectedDto = new UsagePlanDTO(
                "test-id",
                "test-name",
                100,
                QuotaPeriodType.DAY,
                50,
                10.0
        );
        UsagePlanDTO actualDto = apiKeyMapper.usagePlanGetToDto(usagePlan);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void testUsagePlanUpdateToDto() {
        UpdateUsagePlanResponse usagePlan = UpdateUsagePlanResponse.builder()
                .id("test-id")
                .name("test-name")
                .quota(QuotaSettings.builder()
                        .limit(200)
                        .period(QuotaPeriodType.MONTH)
                        .build())
                .throttle(ThrottleSettings.builder()
                        .burstLimit(100)
                        .rateLimit(20.0)
                        .build())
                .build();

        UsagePlanDTO expectedDto = new UsagePlanDTO(
                "test-id",
                "test-name",
                200,
                QuotaPeriodType.MONTH,
                100,
                20.0
        );
        UsagePlanDTO actualDto = apiKeyMapper.usagePlanUpdateToDto(usagePlan);
        assertEquals(expectedDto, actualDto);
    }

    private static class ApiKeyMapperImpl extends ApiKeyMapper {
    }

}
