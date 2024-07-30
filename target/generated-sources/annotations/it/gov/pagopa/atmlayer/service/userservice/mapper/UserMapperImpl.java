package it.gov.pagopa.atmlayer.service.userservice.mapper;

import it.gov.pagopa.atmlayer.service.userservice.dto.UserWithProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-30T10:08:01+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Oracle Corporation)"
)
@ApplicationScoped
public class UserMapperImpl extends UserMapper {

    @Override
    public List<UserWithProfilesDTO> toDTOList(List<User> list) {
        if ( list == null ) {
            return null;
        }

        List<UserWithProfilesDTO> list1 = new ArrayList<UserWithProfilesDTO>( list.size() );
        for ( User user : list ) {
            list1.add( toProfilesDTO( user ) );
        }

        return list1;
    }

    @Override
    public PageInfo<UserWithProfilesDTO> toFrontEndDTOListPaged(PageInfo<User> input) {
        if ( input == null ) {
            return null;
        }

        List<UserWithProfilesDTO> results = null;
        Integer page = null;
        Integer limit = null;
        Integer itemsFound = null;
        Integer totalPages = null;

        results = toDTOList( input.getResults() );
        page = input.getPage();
        limit = input.getLimit();
        itemsFound = input.getItemsFound();
        totalPages = input.getTotalPages();

        PageInfo<UserWithProfilesDTO> pageInfo = new PageInfo<UserWithProfilesDTO>( page, limit, itemsFound, totalPages, results );

        return pageInfo;
    }
}
