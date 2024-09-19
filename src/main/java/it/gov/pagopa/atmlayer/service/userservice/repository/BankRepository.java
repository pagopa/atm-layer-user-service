package it.gov.pagopa.atmlayer.service.userservice.repository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.atmlayer.service.userservice.entity.BankEntity;
import it.gov.pagopa.atmlayer.service.userservice.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BankRepository implements PanacheRepositoryBase<BankEntity, String> {

    public Uni<PageInfo<BankEntity>> findByFilters(Map<String, Object> params, int pageIndex, int pageSize) {
        StringBuilder query = new StringBuilder("select b from BankEntity b where 1=1 ");
        Map<String, Object> queryParams = new HashMap<>();

        params.forEach((key, value) -> {
            query.append("and lower(b.").append(key).append(") LIKE lower(concat(concat(:").append(key).append(", '%'), '%')) ");
            queryParams.put(key, "%" + value + "%");
        });

        query.append("order by b.lastUpdatedAt DESC");

        PanacheQuery<BankEntity> queryResult = find(query.toString(), queryParams).page(Page.of(pageIndex, pageSize));

        return queryResult.count()
                .onItem().transformToUni(count -> {
                    int totalCount = count.intValue();
                    int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                    return queryResult.list()
                            .onItem().transform(list -> new PageInfo<>(pageIndex, pageSize, totalCount, totalPages, list));
                });
    }


    public Uni<List<BankEntity>> findAllById(String acquirerId) {
        String sql = "SELECT * FROM bank WHERE acquirer_id = :acquirerId order by last_updated_at DESC";
        return Panache.getSession()
                .onItem()
                .transformToUni(session ->
                        session.createNativeQuery(sql, BankEntity.class)
                                .setParameter("acquirerId", acquirerId)
                                .getResultList());
    }
}
