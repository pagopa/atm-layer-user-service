package it.gov.pagopa.atmlayer.service.userservice.repository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class BankRepository implements PanacheRepositoryBase<BankEntity, String> {

    public Uni<PageInfo<BankEntity>> findByFilters(Map<String, Object> params, int pageIndex, int pageSize) {
        String queryFilters = params.keySet().stream()
                .map(key -> "b." + key + " = :" + key)
                .collect(Collectors.joining(" and "));

        PanacheQuery<BankEntity> queryResult = find(("select b from BankEntity b")
                .concat(queryFilters.isBlank() ? "" : " where " + queryFilters)
                .concat(" order by b.lastUpdatedAt DESC"), params)
                .page(Page.of(pageIndex, pageSize));

        return queryResult.count()
                .onItem().transformToUni(count -> {
                    int totalCount = count.intValue();
                    int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                    return queryResult.list()
                            .onItem()
                            .transform(list -> new PageInfo<>(pageIndex, pageSize, totalCount, totalPages, list));
                });
    }


    public Uni<List<BankEntity>> findAllById(String acquirerId) {
        String sql = "SELECT * FROM banks WHERE acquirer_id = :acquirerId order by last_updated_at DESC";
        return Panache.getSession()
                .onItem()
                .transformToUni(session ->
                        session.createNativeQuery(sql, BankEntity.class)
                                .setParameter("acquirerId", acquirerId)
                                .getResultList());
    }
}
