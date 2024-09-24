package it.gov.pagopa.atmlayer.service.userservice.repository;


import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.entity.User;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {
    public Uni<User> findByIdCustom(String userId) {
        return find("select u from User u left join fetch u.userProfiles where u.userId = :userId",
                Parameters.with("userId", userId)).firstResult();
    }

    public PanacheQuery<User> findAllCustom() {
        return find("select u from User u left join fetch u.userProfiles");
    }

    public Uni<PageInfo<User>> findByFilters(Map<String, Object> params, int pageIndex, int pageSize) {
        StringBuilder dataQuery = new StringBuilder("select distinct u from User u left join fetch u.userProfiles up where 1=1 ");

        params.forEach((key, value) -> {
            if (key.equalsIgnoreCase("name") || key.equalsIgnoreCase("surname") || key.equalsIgnoreCase("userId")) {
                dataQuery.append("and lower(u.").append(key).append(") LIKE lower(concat(:").append(key).append(", '%')) ");
            }
        });

        if (params.containsKey("profileId")) {
            dataQuery.append("and exists (select 1 from UserProfiles up2 where up2.user = u and up2.profile.profileId = :profileId) ");
        }

        dataQuery.append("order by u.lastUpdatedAt DESC");

        Uni<List<User>> allUsersUni = find(dataQuery.toString(), params).list();

        return allUsersUni.onItem().transformToUni(allUsers -> {
            int totalCount = allUsers.size();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            List<User> paginatedUsers = allUsers.stream()
                    .skip(pageIndex * pageSize)
                    .limit(pageSize)
                    .toList();

            return Uni.createFrom().item(new PageInfo<>(pageIndex, pageSize, totalCount, totalPages, paginatedUsers));
        });
    }


}
