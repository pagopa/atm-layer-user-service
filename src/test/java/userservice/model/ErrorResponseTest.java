package userservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ErrorResponseTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testErrorResponseSerialization() throws Exception {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("error")
                .statusCode(500)
                .message("An unexpected error has occurred. Please contact support.")
                .errorCode("ATMLU_500")
                .build();

        String json = objectMapper.writeValueAsString(errorResponse);
        ErrorResponse deserializedErrorResponse = objectMapper.readValue(json, ErrorResponse.class);
        assertNotEquals(errorResponse, deserializedErrorResponse);
    }

    @Test
    void testJsonPropertyOrder() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("error")
                .statusCode(500)
                .message("An unexpected error has occurred. Please contact support.")
                .errorCode("ATMLU_500")
                .build();

        try {
            String json = objectMapper.writeValueAsString(errorResponse);
            String expectedJson = "{\"type\":\"error\",\"statusCode\":500,\"message\":\"An unexpected error has occurred. Please contact support.\",\"errorCode\":\"ATMLU_500\"}";
            assertEquals(expectedJson, json);
        } catch (Exception e) {
            fail("JSON serialization failed: " + e.getMessage());
        }
    }
}
