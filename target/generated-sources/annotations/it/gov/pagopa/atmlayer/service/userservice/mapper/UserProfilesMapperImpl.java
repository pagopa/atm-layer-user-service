package it.gov.pagopa.atmlayer.service.userservice.mapper;

import it.gov.pagopa.atmlayer.service.userservice.dto.UserProfilesDTO;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfiles;
import it.gov.pagopa.atmlayer.service.userservice.entity.UserProfilesPK;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-26T14:46:20+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Amazon.com Inc.)"
)
@ApplicationScoped
public class UserProfilesMapperImpl extends UserProfilesMapper {

    @Override
    public UserProfilesDTO toDTO(UserProfiles userProfiles) {
        if ( userProfiles == null ) {
            return null;
        }

        UserProfilesDTO.UserProfilesDTOBuilder userProfilesDTO = UserProfilesDTO.builder();

        userProfilesDTO.userId( userProfilesUserProfilesPKUserId( userProfiles ) );
        userProfilesDTO.profileId( userProfilesUserProfilesPKProfileId( userProfiles ) );
        userProfilesDTO.createdAt( userProfiles.getCreatedAt() );
        userProfilesDTO.lastUpdatedAt( userProfiles.getLastUpdatedAt() );

        return userProfilesDTO.build();
    }

    @Override
    public List<UserProfilesDTO> toDtoList(List<UserProfiles> userProfilesList) {
        if ( userProfilesList == null ) {
            return null;
        }

        List<UserProfilesDTO> list = new ArrayList<UserProfilesDTO>( userProfilesList.size() );
        for ( UserProfiles userProfiles : userProfilesList ) {
            list.add( toDTO( userProfiles ) );
        }

        return list;
    }

    private String userProfilesUserProfilesPKUserId(UserProfiles userProfiles) {
        if ( userProfiles == null ) {
            return null;
        }
        UserProfilesPK userProfilesPK = userProfiles.getUserProfilesPK();
        if ( userProfilesPK == null ) {
            return null;
        }
        String userId = userProfilesPK.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private int userProfilesUserProfilesPKProfileId(UserProfiles userProfiles) {
        if ( userProfiles == null ) {
            return 0;
        }
        UserProfilesPK userProfilesPK = userProfiles.getUserProfilesPK();
        if ( userProfilesPK == null ) {
            return 0;
        }
        int profileId = userProfilesPK.getProfileId();
        return profileId;
    }
}
