package userservice.configuration;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.configuration.CognitoClientConf;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class CognitoClientConfTest {

    @Inject
    CognitoClientConf cognitoClientConf;

    @Test
    void testInit() {
        assertNotNull(cognitoClientConf.getCognitoClient());
    }

}
