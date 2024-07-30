package it.gov.pagopa.atmlayer.service.userservice.enums;

import lombok.Getter;

import static it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorType.*;

/**
 * Enumeration for application error codes and messages
 */
@Getter
public enum AppErrorCodeEnum {

    ATML_USER_SERVICE_500("ATMLU_500", "An unexpected error has occurred, see logs for more info", GENERIC),
    PAGE_SIZE_WRONG_VALUE("ATMLU_4000001", "Pagina e dimensione non devono essere nulli o vuoti, e la dimensione deve essere maggiore di zero", INVALID_ARGUMENT),
    USER_PROFILE_WITH_SAME_ID_ALREADY_EXIST("ATMLU_4000002", "Un profilo utente con lo stesso id esiste già", CONSTRAINT_VIOLATION),
    NO_USER_PROFILE_FOUND_FOR_ID("ATMLU_4000003", "Nessun utente trovato per l'id selezionato", NOT_EXISTING_USER_ID),
    NO_USER_PROFILE_FOUND_FOR_PROFILE("ATMLU_4000004", "Nessun profilo utente trovato", NOT_EXISTING_USER_PROFILE),
    USER_WITH_SAME_ID_ALREADY_EXIST("ATMLU_4000005", "Un utente con lo stesso id esiste già", CONSTRAINT_VIOLATION),
    USER_PROFILE_ALREADY_EXIST("ATMLU_4000006", "Profilo già associato all'utente", CONSTRAINT_VIOLATION),
    NO_USER_PROFILE_FOUND("ATMLU_4000007", "Nessun user profile trovato", CONSTRAINT_VIOLATION),
    NO_USER_FOUND_FOR_ID("ATMLU_4000008", "Nessun utente trovato per l'id selezionato", NOT_EXISTING_USER_ID),
    PROFILE_ALREADY_EXIST("ATMLU_4000009", "Esiste già un profilo con lo stesso id", CONSTRAINT_VIOLATION),
    PROFILE_NOT_FOUND("ATMLU_4000010", "Non esiste un profilo con l'id indicato", CONSTRAINT_VIOLATION),
    PROFILE_OR_USER_NOT_FOUND("ATMLU_4000011","Utente o profilo non trovato", CONSTRAINT_VIOLATION),
    NO_ASSOCIATION_FOUND("ATMLU_4000012","Nessuna associazione trovata", CONSTRAINT_VIOLATION),
    ALL_FIELDS_ARE_BLANK("ATMLU_4000013", "Tutti i campi sono vuoti", BLANK_FIELDS),
    BANK_WITH_THE_SAME_ID_ALREADY_EXISTS("ATMLU_4000014", "Una banca con lo stesso acquirerId esiste già", CONSTRAINT_VIOLATION),
    BANK_NOT_FOUND("ATMLU_4000015", "Non esiste tale acquirerId nel database", NON_EXISTING_ACQUIRER_ID),
    RATEMIN_GREATER_THAN_RATEMAX("ATMLU_4000016", "rateMax deve essere maggiore di rateMin", CONSTRAINT_VIOLATION),
    AWS_COMMUNICATION_ERROR("ATMLU_4000017", "Errore di comunicazione con AWS", COMMUNICATION_ERROR),
    DATABASE_TRANSACTION_ERROR("ATMLU_4000018", "Errore di comunicazione con il database", COMMUNICATION_ERROR);

    private final String errorCode;
    private final String errorMessage;
    private final AppErrorType type;

    AppErrorCodeEnum(String errorCode, String errorMessage, AppErrorType type) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.type = type;
    }
}
