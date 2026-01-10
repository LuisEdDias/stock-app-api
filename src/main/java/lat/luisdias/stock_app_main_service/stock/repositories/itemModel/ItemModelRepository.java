package lat.luisdias.stock_app_main_service.stock.repositories.itemModel;

import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.GetModelReferenceProjection;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.ItemModelStatisticsProjection;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemModelRepository extends JpaRepository<ItemModel, Long> {
    String STATS_QUERY = """
                SELECT 'total' AS type, NULL AS category, COUNT(*) AS count
                FROM item_model
                UNION ALL
                SELECT 'active', NULL, COUNT(*) FROM item_model WHERE active = true
                UNION ALL
                SELECT 'inactive', NULL, COUNT(*) FROM item_model WHERE active = false
                UNION ALL
                SELECT 'byCategory', c.name, COUNT(*)
                FROM item_model m
                JOIN category c ON c.id = m.item_category_id
                GROUP BY c.name
            """;

    Optional<ItemModel> findByModel(String model);

    @Query("SELECT i FROM ItemModel i JOIN FETCH i.itemCategory")
    List<ItemModel> findAllBasicInfo();

    @Query("SELECT i FROM ItemModel i JOIN FETCH i.itemCategory LEFT JOIN FETCH i.imgLinks WHERE i.id = :id")
    Optional<ItemModel> findByIdWithImages(@Param("id") Long id);

    @Query(value = STATS_QUERY, nativeQuery = true)
    List<ItemModelStatisticsProjection> getItemModelStats();

    @Query("SELECT m.id AS id, m.model AS model FROM ItemModel m")
    List<GetModelReferenceProjection> getAllModelReferences();
}
