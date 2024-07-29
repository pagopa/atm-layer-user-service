package it.gov.pagopa.atmlayer.service.userservice.mapper;

import it.gov.pagopa.atmlayer.service.userservice.dto.ProfileCreationDto;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import it.gov.pagopa.atmlayer.service.userservice.model.ProfileDTO;
import jakarta.enterprise.context.ApplicationScoped;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-29T15:35:15+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Oracle Corporation)"
)
@ApplicationScoped
public class ProfileMapperImpl extends ProfileMapper {

    @Override
    public Profile toEntity(ProfileCreationDto profileCreationDto) {
        if ( profileCreationDto == null ) {
            return null;
        }

        Profile profile = new Profile();

        profile.setProfileId( profileCreationDto.getProfileId() );
        profile.setDescription( profileCreationDto.getDescription() );

        return profile;
    }

    @Override
    public ProfileDTO toDto(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileDTO profileDTO = new ProfileDTO();

        profileDTO.setDescription( profile.getDescription() );
        profileDTO.setProfileId( profile.getProfileId() );
        profileDTO.setCreatedAt( profile.getCreatedAt() );
        profileDTO.setLastUpdatedAt( profile.getLastUpdatedAt() );

        return profileDTO;
    }
}
