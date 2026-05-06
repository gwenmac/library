package library.repositories;

import library.entities.Gauge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GaugeRepository extends JpaRepository<Gauge, Long> {

    @Query("SELECT g FROM Gauge g LEFT JOIN FETCH g.entries WHERE g.user.id = :userId")
    List<Gauge> findAllWithEntriesByUserId(@Param("userId") Long userId);

    Optional<Gauge> findByIdAndUserId(Long id, Long userId);
}
