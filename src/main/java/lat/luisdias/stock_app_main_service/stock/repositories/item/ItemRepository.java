package lat.luisdias.stock_app_main_service.stock.repositories.item;

import lat.luisdias.stock_app_main_service.stock.dto.Item.*;
import lat.luisdias.stock_app_main_service.stock.entities.item.Item;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    String BASE_QUERY = """
                SELECT i.id AS id,
                    itemModel AS itemModel,
                    b AS box,
                    inv AS inventory,
                    i.comment AS comment,
                    i.status AS status,
                    i.available AS available,
                    i.created AS created,
                    i.updated AS updated
                FROM Item i
                JOIN i.itemModel itemModel
                JOIN i.inventory inv
                LEFT JOIN i.box b
            """;

    @Query(BASE_QUERY + " WHERE inv.id = :inventoryId")
    List<GetItemProjection> findAllWithProjectionByInventoryId(@Param("inventoryId") Long inventoryId);

    @Query(BASE_QUERY + " WHERE inv.id = :inventoryId AND b IS NOT NULL")
    List<GetItemProjection> findAllWithProjectionByInventoryIdAndBoxNotNull(@Param("inventoryId") Long inventoryId);

    @Query(BASE_QUERY)
    List<GetItemProjection> findAllWithProjection();

    @Query(BASE_QUERY + " WHERE i.id = :itemId")
    GetItemProjection findByIdWithProjection(@Param("itemId") Long itemId);

    String OVERVIEW_STATS_QUERY = """
                SELECT
                    COUNT(i) AS totalItems,
                    SUM(CASE WHEN i.available = true THEN 1 ELSE 0 END) AS availableItems,
                    SUM(CASE WHEN i.available = false THEN 1 ELSE 0 END) AS unavailableItems,
                    SUM(CASE WHEN i.status = 0 THEN 1 ELSE 0 END) AS testedOk,
                    SUM(CASE WHEN i.status = 1 THEN 1 ELSE 0 END) AS toTest,
                    SUM(CASE WHEN i.status = 2 THEN 1 ELSE 0 END) AS faulty
                FROM Item i
            """;

    @Query(OVERVIEW_STATS_QUERY)
    ItemOverviewStatsProjection getItemOverviewStatsProjection();

    @Query("SELECT i.itemModel.model AS model, i.itemModel.id AS modelId, COUNT(i) AS count FROM Item i GROUP BY i.itemModel.id, i.itemModel.model")
    List<ItemCountByModelProjection> countItemsByModel();

    @Query("SELECT i.itemModel.itemCategory.name AS category, COUNT(i) AS count FROM Item i GROUP BY i.itemModel.itemCategory.name")
    List<ItemCountByCategoryProjection> countItemsByCategory();

    @Query("SELECT i.inventory.name AS inventory, COUNT(i) AS count FROM Item i GROUP BY i.inventory.name")
    List<ItemCountByInventoryProjection> countItemsByInventory();

    @Query("SELECT COALESCE(b.name, 'Unboxed') AS box, COUNT(i) AS count FROM Item i LEFT JOIN i.box b GROUP BY b.name")
    List<ItemCountByBoxProjection> countItemsByBox();

    @Query("""
                SELECT i FROM Item i
                LEFT JOIN FETCH i.box
                JOIN FETCH i.itemModel
                JOIN FETCH i.inventory
                WHERE (:itemModelId IS NULL OR i.itemModel.id = :itemModelId)
                AND (:boxId IS NULL OR (:boxId = 0 AND i.box IS NULL) OR i.box.id = :boxId)
                AND (:inventoryId IS NULL OR i.inventory.id = :inventoryId)
                AND (:status IS NULL OR i.status = :status)
                AND (:available IS NULL OR i.available = :available)
                AND (:fromItemId IS NULL OR i.id >= :fromItemId)
                AND (:toItemId IS NULL OR i.id <= :toItemId)
                AND (CAST(:fromUpdated AS timestamp) IS NULL OR i.updated >= :fromUpdated)
                AND (CAST(:toUpdated AS timestamp) IS NULL OR i.updated <= :toUpdated)
            """
    )
    Page<GetItemProjection> findFiltered(
            @Param("itemModelId") Long itemModelId,
            @Param("boxId") Long boxId,
            @Param("inventoryId") Long inventoryId,
            @Param("status") ItemStatus status,
            @Param("available") Boolean available,
            @Param("fromItemId") Long fromItemId,
            @Param("toItemId") Long toItemId,
            @Param("fromUpdated") Timestamp fromUpdated,
            @Param("toUpdated") Timestamp toUpdated,
            Pageable pageable
    );
}
