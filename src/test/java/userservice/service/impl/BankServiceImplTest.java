package userservice.service.impl;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.enums.AppErrorCodeEnum;
import it.gov.pagopa.atmlayer.service.userservice.exception.AtmLayerException;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.repository.BankRepository;
import it.gov.pagopa.atmlayer.service.userservice.service.ApiKeyService;
import it.gov.pagopa.atmlayer.service.userservice.service.CognitoService;
import it.gov.pagopa.atmlayer.service.userservice.service.impl.BankServiceImpl;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class BankServiceImplTest {

    @Mock
    BankMapper bankMapper;
    @Mock
    BankRepository bankRepository;
    @Mock
    CognitoService cognitoService;
    @Mock
    ApiKeyService apiKeyService;
    @InjectMocks
    BankServiceImpl bankService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchBankSuccess() {
        List<BankEntity> bankList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankList.add(bankEntity);
        int pageIndex = 0;
        int pageSize = 10;
        String acquirerId = "acquirer123";
        String denomination = "Test Bank";
        String clientId = "client456";

        PageInfo<BankEntity> expectedPageInfo = new PageInfo<>(0, 10, 1, 1, bankList);

        when(bankRepository.findByFilters(anyMap(), eq(pageIndex), eq(pageSize)))
                .thenReturn(Uni.createFrom().item(expectedPageInfo));

        Uni<PageInfo<BankEntity>> result = bankService.searchBanks(pageIndex, pageSize, acquirerId, denomination, clientId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expectedPageInfo);
    }

    @Test
    void testSearchBanksEmptyFilters() {
        List<BankEntity> bankList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankList.add(bankEntity);

        int pageIndex = 0;
        int pageSize = 10;
        String acquirerId = null;
        String denomination = "";
        String clientId = null;

        Map<String, Object> filters = new HashMap<>();
        filters.put("acquirerId", acquirerId);
        filters.put("denomination", denomination);
        filters.put("clientId", clientId);
        filters.remove(null);
        filters.values().removeAll(Collections.singleton(null));
        filters.values().removeAll(Collections.singleton(""));

        PageInfo<BankEntity> expectedPageInfo = new PageInfo<>(0, 10, 1, 1, bankList);

        when(bankRepository.findByFilters(eq(filters), eq(pageIndex), eq(pageSize)))
                .thenReturn(Uni.createFrom().item(expectedPageInfo));

        Uni<PageInfo<BankEntity>> result = bankService.searchBanks(pageIndex, pageSize, acquirerId, denomination, clientId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expectedPageInfo);
    }

    @Test
    void testFindBankByIdFound() {
        String acquirerId = "test-acquirer-id";
        BankEntity bankEntity = new BankEntity();

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().item(bankEntity));

        Uni<BankEntity> result = bankService.findBankById(acquirerId);

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(bankEntity);
    }

    @Test
    void testFindBankByIdNotFound() {
        String acquirerId = "non-existent-id";

        when(bankRepository.findById(acquirerId)).thenReturn(Uni.createFrom().nullItem());

        AtmLayerException thrownException = assertThrows(AtmLayerException.class, () -> bankService.findBankById(acquirerId).await().indefinitely());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), thrownException.getStatusCode());
        assertEquals(AppErrorCodeEnum.BANK_NOT_FOUND.getErrorCode(), thrownException.getErrorCode());
    }

    @Test
    void testGetAll() {
        List<BankEntity> bankEntitiesList = new ArrayList<>();
        BankEntity bankEntity = new BankEntity();
        bankEntitiesList.add(bankEntity);

        PanacheQuery<BankEntity> panacheQuery = mock(PanacheQuery.class);

        when(bankRepository.findAll()).thenReturn(panacheQuery);
        when(panacheQuery.list()).thenReturn(Uni.createFrom().item(bankEntitiesList));

        Uni<List<BankEntity>> result = bankService.getAll();

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(bankEntitiesList);
    }

}
