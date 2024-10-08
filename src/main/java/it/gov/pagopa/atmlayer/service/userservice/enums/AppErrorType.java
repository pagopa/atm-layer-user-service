package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.Getter;

@Getter
public enum AppErrorType {
    GENERIC,
    INVALID_ARGUMENT,
    CONSTRAINT_VIOLATION,
    NOT_EXISTING_USER_ID,
    NOT_EXISTING_USER_PROFILE,
    BLANK_FIELDS,
    NON_EXISTING_ACQUIRER_ID,
    COMMUNICATION_ERROR,
    AWS_ERROR,
    NO_USER_FOUND
}