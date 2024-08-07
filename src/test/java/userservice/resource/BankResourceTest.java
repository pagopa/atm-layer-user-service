package userservice.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankUpdateDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.service.BankService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class BankResourceTest {

    @InjectMock
    BankService bankService;

    @InjectMock
    BankMapper bankMapper;

    @Test
    void testSearch() {
        // Dati di test per la risposta del servizio
        List<BankEntity> bankList = new ArrayList<>();
        BankEntity bank = new BankEntity(); // Crea un oggetto Bank e imposta le sue proprietà se necessario
        bankList.add(bank);
        PageInfo<BankEntity> pageInfoEntity = new PageInfo<>(0, 10, 1, 1, bankList);

        // Dati di test per il mapping a DTO
        List<BankDTO> dtoList = new ArrayList<>();
        BankDTO bankDTO = new BankDTO(); // Crea un oggetto BankDTO e imposta le sue proprietà se necessario
        dtoList.add(bankDTO);
        PageInfo<BankDTO> pageInfoDTO = new PageInfo<>(0, 10, 1, 1, dtoList);

        // Mock dei servizi
        when(bankService.searchBanks(anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(Uni.createFrom().item(pageInfoEntity));
        when(bankMapper.toDtoPaged(any(PageInfo.class))).thenReturn(pageInfoDTO);

        // Esecuzione della richiesta e verifica del risultato
        PageInfo<BankDTO> result = given()
                .when()
                .queryParam("pageIndex", 0)
                .queryParam("pageSize", 10)
                .queryParam("acquirerId", "acquirerId")
                .queryParam("denomination", "denomination")
                .queryParam("clientId", "clientId")
                .get("/api/v1/user-service/banks/search")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {});

        // Verifica che i risultati corrispondano a quelli aspettati
        assertEquals(dtoList.size(), result.getResults().size());
        assertEquals(pageInfoDTO.getItemsFound(), result.getItemsFound());
        assertEquals(pageInfoDTO.getTotalPages(), result.getTotalPages());

        // Verifica che i servizi siano stati chiamati esattamente una volta
        verify(bankService, times(1)).searchBanks(anyInt(), anyInt(), anyString(), anyString(), anyString());
        verify(bankMapper, times(1)).toDtoPaged(any(PageInfo.class));
    }

    @Test
    void testSearchEmptyList() {
        // Dati di test per la risposta del servizio con lista vuota
        List<BankEntity> bankList = new ArrayList<>();
        PageInfo<BankEntity> pageInfoEntity = new PageInfo<>(0, 10, 0, 1, bankList);

        // Dati di test per il mapping a DTO
        List<BankDTO> dtoList = new ArrayList<>();
        PageInfo<BankDTO> pageInfoDTO = new PageInfo<>(0, 10, 0, 1, dtoList);

        // Mock dei servizi
        when(bankService.searchBanks(anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(Uni.createFrom().item(pageInfoEntity));
        when(bankMapper.toDtoPaged(any(PageInfo.class))).thenReturn(pageInfoDTO);

        // Esecuzione della richiesta e verifica del risultato
        PageInfo<BankDTO> result = given()
                .when()
                .queryParam("pageIndex", 0)
                .queryParam("pageSize", 10)
                .queryParam("acquirerId", "acquirerId")
                .queryParam("denomination", "denomination")
                .queryParam("clientId", "clientId")
                .get("/api/v1/user-service/banks/search")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {});

        // Verifica che i risultati siano vuoti
        assertEquals(0, result.getResults().size());
        assertEquals(pageInfoDTO.getItemsFound(), result.getItemsFound());
        assertEquals(pageInfoDTO.getTotalPages(), result.getTotalPages());

        // Verifica che i servizi siano stati chiamati esattamente una volta
        verify(bankService, times(1)).searchBanks(anyInt(), anyInt(), anyString(), anyString(), anyString());
        verify(bankMapper, times(1)).toDtoPaged(any(PageInfo.class));
    }

    @Test
    void testInsert() {
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        bankInsertionDTO.setAcquirerId("Acquirer123");
        bankInsertionDTO.setDenomination("Sample Bank");
        bankInsertionDTO.setLimit(10000);
        bankInsertionDTO.setPeriod(QuotaPeriodType.MONTH);
        bankInsertionDTO.setBurstLimit(500);
        bankInsertionDTO.setRateLimit(0.05);

        when(bankService.insertBank(bankInsertionDTO))
                .thenReturn(Uni.createFrom().item(bankPresentationDTO));

        BankPresentationDTO result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankInsertionDTO)
                .when()
                .post("api/v1/user-service/banks/insert")
                .then()
                .statusCode(200)
                .extract()
                .as(BankPresentationDTO.class);

        assertEquals(bankPresentationDTO, result);
    }

    @Test
    void testUpdate() {
        BankUpdateDTO bankUpdateDTO = new BankUpdateDTO();
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        bankUpdateDTO.setAcquirerId("Acquirer123");
        bankUpdateDTO.setDenomination("Updated Bank Name");
        bankUpdateDTO.setLimit(20000);
        bankUpdateDTO.setPeriod(QuotaPeriodType.MONTH);
        bankUpdateDTO.setBurstLimit(1000);
        bankUpdateDTO.setRateLimit(0.10);

        when(bankService.updateBank(bankUpdateDTO))
                .thenReturn(Uni.createFrom().item(bankPresentationDTO));

        BankPresentationDTO result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankUpdateDTO)
                .when()
                .put("api/v1/user-service/banks/update")
                .then()
                .statusCode(200)
                .extract()
                .as(BankPresentationDTO.class);

        assertEquals(bankPresentationDTO, result);
    }

    @Test
    void testDisable() {
        String acquirerId = "Acquirer123";

        when(bankService.disable(acquirerId))
                .thenReturn(Uni.createFrom().voidItem());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("api/v1/user-service/banks/disable/" + acquirerId)
                .then()
                .statusCode(204);
    }

    @Test
    void testGetBank() {
        String acquirerId = "Acquirer123";
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();

        when(bankService.findByAcquirerId(acquirerId))
                .thenReturn(Uni.createFrom().item(bankPresentationDTO));

        BankPresentationDTO result = given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("api/v1/user-service/banks/" + acquirerId)
                .then()
                .statusCode(200)
                .extract()
                .as(BankPresentationDTO.class);

        assertEquals(bankPresentationDTO, result);
    }

    @Test
    void testGetAll() {
        List<BankEntity> banks = new ArrayList<>();
        BankEntity bank = new BankEntity();
        banks.add(bank);

        List<BankDTO> bankDTOs = new ArrayList<>();
        BankDTO bankDTO = new BankDTO();
        bankDTOs.add(bankDTO);

        when(bankService.getAll()).thenReturn(Uni.createFrom().item(banks));
        when(bankMapper.toDTOList(any())).thenReturn(bankDTOs);

        List<BankDTO> result = given()
                .when().get("api/v1/user-service/banks/")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", BankDTO.class);

        assertEquals(1, result.size());
        assertEquals(bankDTO, result.get(0));

        verify(bankService, times(1)).getAll();
        verify(bankMapper, times(1)).toDTOList(banks);
    }

    @Test
    void testGetAllEmptyList() {
        List<BankEntity> emptyList = Collections.emptyList();

        when(bankService.getAll()).thenReturn(Uni.createFrom().item(emptyList));

        List<BankDTO> result = given()
                .when().get("api/v1/user-service/banks/")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", BankDTO.class);

        assertEquals(0, result.size());

        verify(bankService, times(1)).getAll();
    }

}
