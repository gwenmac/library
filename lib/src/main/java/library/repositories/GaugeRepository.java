package library.repositories;

import library.entities.Gauge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GaugeRepository extends JpaRepository<Gauge, Long> {

    @Query("SELECT g FROM Gauge g LEFT JOIN FETCH g.entries")
    List<Gauge> findAllWithEntries();
}
