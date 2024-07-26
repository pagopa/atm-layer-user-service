package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.Getter;

import static it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorType.*;

/**
 * Enumeration for application error codes and messages
 */
@Getter
public enum AppErrorCodeEnum {

    ATML_USER_SERVICE_500("ATML_USER_SERVICE_500", "An unexpected error has occurred, see logs for more info", GENERIC),
    PAGE_SIZE_WRONG_VALUE("ATMLM_4000041", "Pagina e dimensione non devono essere nulli o vuoti, e la dimensione deve essere maggiore di zero", INVALID_ARGUMENT),
    USER_PROFILE_WITH_SAME_ID_ALREADY_EXIST("ATMLM_4000043", "Un profilo utente con lo stesso id esiste già", CONSTRAINT_VIOLATION),
    NO_USER_PROFILE_FOUND_FOR_ID("ATMLM_4000044", "Nessun utente trovato per l'id selezionato", NOT_EXISTING_USER_ID),
    NO_USER_PROFILE_FOUND_FOR_PROFILE("ATMLM_4000045", "Nessun profilo utente trovato", NOT_EXISTING_USER_PROFILE),
    USER_WITH_SAME_ID_ALREADY_EXIST("ATMLM_4000053", "Un utente con lo stesso id esiste già", CONSTRAINT_VIOLATION),
    USER_PROFILE_ALREADY_EXIST("ATMLM_4000054", "Profilo già associato all'utente", CONSTRAINT_VIOLATION),
    NO_USER_PROFILE_FOUND("ATMLM_4000055", "Nessun user profile trovato", CONSTRAINT_VIOLATION),
    NO_USER_FOUND_FOR_ID("ATMLM_4000056", "Nessun utente trovato per l'id selezionato", NOT_EXISTING_USER_ID),
    PROFILE_ALREADY_EXIST("ATMLM_4000057", "Esiste già un profilo con lo stesso id", CONSTRAINT_VIOLATION),
    PROFILE_NOT_FOUND("ATMLM_4000058", "Non esiste un profilo con l'id indicato", CONSTRAINT_VIOLATION),
    PROFILE_OR_USER_NOT_FOUND("ATMLM_4000059","Utente o profilo non trovato", CONSTRAINT_VIOLATION),
    NO_ASSOCIATION_FOUND("ATMLM_4000060","Nessuna associazione trovata", CONSTRAINT_VIOLATION),
    ALL_FIELDS_ARE_BLANK("ATMLM_4000061", "Tutti i campi sono vuoti", BLANK_FIELDS);

    private final String errorCode;
    private final String errorMessage;
    private final AppErrorType type;

    AppErrorCodeEnum(String errorCode, String errorMessage, AppErrorType type) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.type = type;
    }
}
