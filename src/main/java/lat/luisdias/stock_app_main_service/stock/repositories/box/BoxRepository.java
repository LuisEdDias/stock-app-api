package lat.luisdias.stock_app_main_service.stock.repositories.box;

import lat.luisdias.stock_app_main_service.stock.dto.box.GetBoxBasicProjection;
import lat.luisdias.stock_app_main_service.stock.dto.box.GetBoxReferenceProjection;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.box.BoxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {
    String BASE_QUERY = """
                    SELECT b.id AS id,
                    b.name AS name,
                    inv.name AS inventory,
                    b.description AS description,
                    b.status AS status,
                    b.created AS created,
                    b.updated AS updated
                FROM Box b
                JOIN b.inventory inv
            """;

    @Query("SELECT b FROM Box b JOIN FETCH b.inventory LEFT JOIN FETCH b.items WHERE b.id = :id")
    Optional<Box> findByIdWithItems(@Param("id") Long id);

    @Query(BASE_QUERY)
    List<GetBoxBasicProjection> getAllBoxBasicProjections();

    @Query("SELECT b.id AS id, b.name AS name, b.inventory.id AS inventoryId, b.inventory.name AS inventoryName FROM Box b")
    List<GetBoxReferenceProjection> getAllBoxReferenceProjections();

    @Query("""
                SELECT b FROM Box b
                JOIN FETCH b.inventory
                WHERE (:inventoryId IS NULL OR b.inventory.id = :inventoryId)
                AND (:boxStatus IS NULL OR b.status = :boxStatus)
                AND (:fromBoxId IS NULL OR b.id >= :fromBoxId)
                AND (:toBoxId IS NULL OR b.id <= :toBoxId)
                AND (CAST(:fromUpdated AS timestamp) IS NULL OR b.updated >= :fromUpdated)
                AND (CAST(:toUpdated AS timestamp) IS NULL OR b.updated <= :toUpdated)
            """
    )
    Page<GetBoxBasicProjection> findFiltered(
            @Param("inventoryId") Long inventoryId,
            @Param("boxStatus") BoxStatus boxStatus,
            @Param("fromBoxId") Long fromBoxId,
            @Param("toBoxId") Long toBoxId,
            @Param("fromUpdated") Timestamp fromUpdated,
            @Param("toUpdated") Timestamp toUpdated,
            Pageable pageable
    );
}
