package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.Getter;

@Getter
public enum AppErrorType {
    GENERIC,
    INVALID_ARGUMENT,
    CONSTRAINT_VIOLATION,
    NOT_EXISTING_USER_ID,
    NOT_EXISTING_USER_PROFILE,
    BLANK_FIELDS
}