package it.gov.pagopa.atmlayer.service.userservice.repository;


import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import it.gov.pagopa.atmlayer.service.userservice.entity.Profile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfileRepository implements PanacheRepositoryBase<Profile, Integer> {
}
