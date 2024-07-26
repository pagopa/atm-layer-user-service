package model.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.model.ErrorResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ATMLayerErrorResponseTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testATMLayerErrorResponseSerialization() throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("E001")
                .type("Validation Error")
                .status(500)
                .message("Invalid input data")
                .build();

        String json = objectMapper.writeValueAsString(errorResponse);
        ErrorResponse deserializedErrorResponse = objectMapper.readValue(json, ErrorResponse.class);
        assertNotEquals(errorResponse, deserializedErrorResponse);
    }

    @Test
    void testJsonPropertyOrder() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("E001")
                .type("Validation Error")
                .status(500)
                .message("Invalid input data")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(errorResponse);
            assertTrue(json.contains("\"type\":\"Validation Error\""));
            assertTrue(json.contains("\"errorCode\":\"E001\""));
            assertTrue(json.contains("\"statusCode\":500"));
            assertTrue(json.contains("\"message\":\"Invalid input data\""));
        } catch (Exception e) {
            fail("JSON serialization failed: " + e.getMessage());
        }
    }

    @Test
    void testGetterAnnotations() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("E001")
                .type("Validation Error")
                .status(500)
                .message("Invalid input data")
                .build();

        assertEquals("E001", errorResponse.getErrorCode());
        assertEquals("Validation Error", errorResponse.getType());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Invalid input data", errorResponse.getMessage());
    }
}
