package library.repositories;

import library.entities.GaugeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GaugeEntryRepository extends JpaRepository<GaugeEntry, Long> {

    List<GaugeEntry> findByGaugeIdOrderByCreatedAtAsc(Long gaugeId);
}
