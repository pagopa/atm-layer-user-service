package userservice.configuration;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.configuration.ApiGatewayClientConf;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ApiGatewayClientConfTest {

    @Inject
    ApiGatewayClientConf apiGatewayClientConf;

    @Test
    void testInit() {
        assertNotNull(apiGatewayClientConf.getApiGatewayClient());
    }

}
