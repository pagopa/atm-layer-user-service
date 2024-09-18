package userservice.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.mapper.BankMapper;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigateway.model.QuotaPeriodType;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class BankMapperTest {

    @Inject
    BankMapper bankMapper;

    @Test
    void testToDTO() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        BankEntity bankEntity = new BankEntity();
        bankEntity.setAcquirerId("acquirer-id");
        bankEntity.setDenomination("denomination");
        bankEntity.setClientId("client-id");
        bankEntity.setApiKeyId("api-key-id");
        bankEntity.setUsagePlanId("usage-plan-id");
        bankEntity.setCreatedAt(now);
        bankEntity.setLastUpdatedAt(now);

        BankDTO expectedDTO = BankDTO.builder()
                .acquirerId("acquirer-id")
                .denomination("denomination")
                .clientId("client-id")
                .apiKeyId("api-key-id")
                .usagePlanId("usage-plan-id")
                .createdAt(now)
                .lastUpdatedAt(now)
                .build();

        BankDTO actualDTO = bankMapper.toDTO(bankEntity);

        assertEquals(expectedDTO.getAcquirerId(), actualDTO.getAcquirerId());
        assertEquals(expectedDTO.getDenomination(), actualDTO.getDenomination());
        assertEquals(expectedDTO.getClientId(), actualDTO.getClientId());
        assertEquals(expectedDTO.getApiKeyId(), actualDTO.getApiKeyId());
        assertEquals(expectedDTO.getUsagePlanId(), actualDTO.getUsagePlanId());
        assertEquals(expectedDTO.getCreatedAt(), actualDTO.getCreatedAt());
        assertEquals(expectedDTO.getLastUpdatedAt(), actualDTO.getLastUpdatedAt());
    }

    @Test
    void testToDTOList() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        BankEntity bankEntity1 = new BankEntity("acquirer-id-1", "denomination-1", "client-id-1", "api-key-id-1", "usage-plan-id-1", true, now, now);
        BankEntity bankEntity2 = new BankEntity("acquirer-id-2", "denomination-2", "client-id-2", "api-key-id-2", "usage-plan-id-2", true, now, now);

        List<BankEntity> bankEntities = Arrays.asList(bankEntity1, bankEntity2);

        List<BankDTO> expectedDTOs = Arrays.asList(
                BankDTO.builder()
                        .acquirerId("acquirer-id-1")
                        .denomination("denomination-1")
                        .clientId("client-id-1")
                        .apiKeyId("api-key-id-1")
                        .usagePlanId("usage-plan-id-1")
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build(),
                BankDTO.builder()
                        .acquirerId("acquirer-id-2")
                        .denomination("denomination-2")
                        .clientId("client-id-2")
                        .apiKeyId("api-key-id-2")
                        .usagePlanId("usage-plan-id-2")
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build()
        );
        List<BankDTO> actualDTOs = bankMapper.toDTOList(bankEntities);
        assertEquals(expectedDTOs.size(), actualDTOs.size());
        for (int i = 0; i < expectedDTOs.size(); i++) {
            assertEquals(expectedDTOs.get(i), actualDTOs.get(i));
        }
    }

    @Test
    void testToEntityInsertion() {
        BankInsertionDTO bankInsertionDTO = new BankInsertionDTO();
        bankInsertionDTO.setAcquirerId("acquirer-id");
        bankInsertionDTO.setDenomination("denomination");

        BankEntity expectedEntity = new BankEntity();
        expectedEntity.setAcquirerId("acquirer-id");
        expectedEntity.setDenomination("denomination");
        expectedEntity.setEnabled(true);

        BankEntity actualEntity = bankMapper.toEntityInsertion(bankInsertionDTO);

        assertEquals(expectedEntity.getAcquirerId(), actualEntity.getAcquirerId());
        assertEquals(expectedEntity.getDenomination(), actualEntity.getDenomination());
        assertEquals(expectedEntity.getEnabled(), actualEntity.getEnabled());
    }

    @Test
    void testToDtoPaged() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        BankEntity bankEntity1 = new BankEntity("acquirer-id-1", "denomination-1", "client-id-1", "api-key-id-1", "usage-plan-id-1", true, now, now);
        BankEntity bankEntity2 = new BankEntity("acquirer-id-2", "denomination-2", "client-id-2", "api-key-id-2", "usage-plan-id-2", true, now, now);

        List<BankEntity> bankEntities = Arrays.asList(bankEntity1, bankEntity2);
        PageInfo<BankEntity> pagedBanks = new PageInfo<>(1, 10, 2, 1, bankEntities);

        List<BankDTO> expectedDTOs = Arrays.asList(
                BankDTO.builder()
                        .acquirerId("acquirer-id-1")
                        .denomination("denomination-1")
                        .clientId("client-id-1")
                        .apiKeyId("api-key-id-1")
                        .usagePlanId("usage-plan-id-1")
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build(),
                BankDTO.builder()
                        .acquirerId("acquirer-id-2")
                        .denomination("denomination-2")
                        .clientId("client-id-2")
                        .apiKeyId("api-key-id-2")
                        .usagePlanId("usage-plan-id-2")
                        .createdAt(now)
                        .lastUpdatedAt(now)
                        .build()
        );
        PageInfo<BankDTO> expectedPageInfo = new PageInfo<>(1, 10, 2, 1, expectedDTOs);
        PageInfo<BankDTO> actualPageInfo = bankMapper.toDtoPaged(pagedBanks);

        assertEquals(expectedPageInfo.getPage(), actualPageInfo.getPage());
        assertEquals(expectedPageInfo.getLimit(), actualPageInfo.getLimit());
        assertEquals(expectedPageInfo.getItemsFound(), actualPageInfo.getItemsFound());
        assertEquals(expectedPageInfo.getTotalPages(), actualPageInfo.getTotalPages());
        assertEquals(expectedPageInfo.getResults().size(), actualPageInfo.getResults().size());
        for (int i = 0; i < expectedPageInfo.getResults().size(); i++) {
            assertEquals(expectedPageInfo.getResults().get(i), actualPageInfo.getResults().get(i));
        }
    }

    @Test
    void testToPresentationDTO() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        BankEntity bankEntity = new BankEntity("acquirer-id", "denomination", "client-id", "api-key-id", "usage-plan-id", true, now, now);
        ApiKeyDTO apiKeyDTO = new ApiKeyDTO("api-key-id", "api-key-secret", "api-key-name");
        ClientCredentialsDTO clientCredentialsDTO = new ClientCredentialsDTO("client-id", "client-secret", "client-name");
        UsagePlanDTO usagePlanDTO = new UsagePlanDTO("usage-plan-id", "usage-plan-name", 100, QuotaPeriodType.MONTH, 10, 5.0);

        BankPresentationDTO expectedDTO = new BankPresentationDTO(
                "acquirer-id",
                "denomination",
                "client-id",
                "client-secret",
                "api-key-id",
                "api-key-secret",
                "usage-plan-id",
                100,
                QuotaPeriodType.MONTH,
                10,
                5.0,
                now,
                now
        );

        BankPresentationDTO actualDTO = bankMapper.toPresentationDTO(bankEntity, apiKeyDTO, clientCredentialsDTO, usagePlanDTO);

        assertEquals(expectedDTO.getAcquirerId(), actualDTO.getAcquirerId());
        assertEquals(expectedDTO.getDenomination(), actualDTO.getDenomination());
        assertEquals(expectedDTO.getClientId(), actualDTO.getClientId());
        assertEquals(expectedDTO.getClientSecret(), actualDTO.getClientSecret());
        assertEquals(expectedDTO.getApiKeyId(), actualDTO.getApiKeyId());
        assertEquals(expectedDTO.getApiKeySecret(), actualDTO.getApiKeySecret());
        assertEquals(expectedDTO.getUsagePlanId(), actualDTO.getUsagePlanId());
        assertEquals(expectedDTO.getLimit(), actualDTO.getLimit());
        assertEquals(expectedDTO.getPeriod(), actualDTO.getPeriod());
        assertEquals(expectedDTO.getBurstLimit(), actualDTO.getBurstLimit());
        assertEquals(expectedDTO.getRateLimit(), actualDTO.getRateLimit());
        assertEquals(expectedDTO.getCreatedAt(), actualDTO.getCreatedAt());
        assertEquals(expectedDTO.getLastUpdatedAt(), actualDTO.getLastUpdatedAt());
    }


}
