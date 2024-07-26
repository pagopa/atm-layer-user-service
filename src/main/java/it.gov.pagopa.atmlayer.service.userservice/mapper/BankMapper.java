package it.gov.pagopa.atmlayer.service.userservice.mapper;


import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.dto.BankInsertionDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
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
        bankEntity.setApiKeyId(bankInsertionDTO.getApiKeyId());
        bankEntity.setUsagePlanId(bankInsertionDTO.getUsagePlanId());
        return bankEntity;
    }

    public abstract PageInfo<BankDTO> toDtoPaged(PageInfo<BankEntity> pagedBanks);

}
