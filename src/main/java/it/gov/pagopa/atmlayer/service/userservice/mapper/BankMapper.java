package it.gov.pagopa.atmlayer.service.userservice.mapper;


import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankPresentationDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.ApiKeyDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.ClientCredentialsDTO;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import it.gov.pagopa.atmlayer.service.userservice.model.UsagePlanDTO;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "cdi")
public abstract class BankMapper {

    public abstract BankDTO toDTO(BankEntity bankEntity);

    public List<BankDTO> toDTOList(List<BankEntity> list) {
        return list.stream().map(this::toDTO).toList();
    }

    public BankEntity toEntityInsertion(BankInsertionDTO bankInsertionDTO) {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setEnabled(true);
        bankEntity.setAcquirerId(bankInsertionDTO.getAcquirerId());
        bankEntity.setDenomination(bankInsertionDTO.getDenomination());
        return bankEntity;
    }

    public abstract PageInfo<BankDTO> toDtoPaged(PageInfo<BankEntity> pagedBanks);

    public BankPresentationDTO toPresentationDTO(BankEntity bankEntity, ApiKeyDTO apiKeyDTO, ClientCredentialsDTO clientCredentialsDTO, UsagePlanDTO usagePlanDTO) {
        BankPresentationDTO bankPresentationDTO = new BankPresentationDTO();
        bankPresentationDTO.setAcquirerId(bankEntity.getAcquirerId());
        bankPresentationDTO.setDenomination(bankEntity.getDenomination());
        bankPresentationDTO.setClientId(bankEntity.getClientId());
        bankPresentationDTO.setClientSecret(clientCredentialsDTO.getClientSecret());
        bankPresentationDTO.setApiKeyId(apiKeyDTO.getId());
        bankPresentationDTO.setApiKeySecret(apiKeyDTO.getValue());
        bankPresentationDTO.setUsagePlanId(usagePlanDTO.getId());
        bankPresentationDTO.setLimit(usagePlanDTO.getLimit());
        bankPresentationDTO.setPeriod(usagePlanDTO.getPeriod());
        bankPresentationDTO.setBurstLimit(usagePlanDTO.getBurstLimit());
        bankPresentationDTO.setRateLimit(usagePlanDTO.getRateLimit());
        bankPresentationDTO.setCreatedAt(bankEntity.getCreatedAt());
        bankPresentationDTO.setLastUpdatedAt(bankEntity.getLastUpdatedAt());
        return bankPresentationDTO;
    }

}
