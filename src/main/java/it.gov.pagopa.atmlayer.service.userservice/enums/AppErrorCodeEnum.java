package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.Getter;

import static it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorType.GENERIC;

/**
 * Enumeration for application error codes and messages
 */
@Getter
public enum AppErrorCodeEnum {

    ATML_USER_SERVICE_500("ATML_USER_SERVICE_500", "An unexpected error has occurred, see logs for more info", GENERIC);

    private final String errorCode;
    private final String errorMessage;
    private final AppErrorType type;

    AppErrorCodeEnum(String errorCode, String errorMessage, AppErrorType type) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.type = type;
    }
}
