package library.repositories;

import library.entities.Series;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    List<Series> findAllByHouseholdId(Long householdId);

    Optional<Series> findByIdAndHouseholdId(Long id, Long householdId);
}