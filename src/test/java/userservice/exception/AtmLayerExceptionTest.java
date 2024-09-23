package userservice.exception;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AtmLayerExceptionTest {

    @Test
    void testExceptionWithThrowable() {
        Throwable throwable = new RuntimeException("Test error");
        AtmLayerException exception = AtmLayerException.builder().error(throwable).build();

        assertNotNull(exception);
        assertEquals("Test error", exception.getMessage());
        assertEquals(AppErrorCodeEnum.ATML_USER_SERVICE_500.getType().name(), exception.getType());
        assertEquals(500, exception.getStatusCode());
        assertEquals(AppErrorCodeEnum.ATML_USER_SERVICE_500.getErrorCode(), exception.getErrorCode());
        assertEquals(throwable, exception.getCause());
    }

    @Test
    void testAtmLayerException_allArgsBuilder() {
        AtmLayerException testException = new AtmLayerException("message", Response.Status.BAD_REQUEST, "type");
        assertEquals("type", testException.getType());
        assertEquals("message", testException.getMessage());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), testException.getStatusCode());
    }
}