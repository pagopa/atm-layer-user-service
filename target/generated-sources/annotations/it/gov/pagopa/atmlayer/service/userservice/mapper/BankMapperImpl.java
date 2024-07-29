package it.gov.pagopa.atmlayer.service.userservice.mapper;

import it.gov.pagopa.atmlayer.service.userservice.dto.BankDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-29T12:54:59+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Oracle Corporation)"
)
@ApplicationScoped
public class BankMapperImpl extends BankMapper {

    @Override
    public BankDTO toDTO(BankEntity bankEntity) {
        if ( bankEntity == null ) {
            return null;
        }

        BankDTO.BankDTOBuilder bankDTO = BankDTO.builder();

        bankDTO.acquirerId( bankEntity.getAcquirerId() );
        bankDTO.denomination( bankEntity.getDenomination() );
        bankDTO.clientId( bankEntity.getClientId() );
        bankDTO.apiKeyId( bankEntity.getApiKeyId() );
        bankDTO.usagePlanId( bankEntity.getUsagePlanId() );
        bankDTO.createdAt( bankEntity.getCreatedAt() );
        bankDTO.lastUpdatedAt( bankEntity.getLastUpdatedAt() );

        return bankDTO.build();
    }

    @Override
    public PageInfo<BankDTO> toDtoPaged(PageInfo<BankEntity> pagedBanks) {
        if ( pagedBanks == null ) {
            return null;
        }

        Integer page = null;
        Integer limit = null;
        Integer itemsFound = null;
        Integer totalPages = null;
        List<BankDTO> results = null;

        page = pagedBanks.getPage();
        limit = pagedBanks.getLimit();
        itemsFound = pagedBanks.getItemsFound();
        totalPages = pagedBanks.getTotalPages();
        results = toDTOList( pagedBanks.getResults() );

        PageInfo<BankDTO> pageInfo = new PageInfo<BankDTO>( page, limit, itemsFound, totalPages, results );

        return pageInfo;
    }
}
