package library.repositories;

import library.entities.Book;
import library.entities.Series;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    List<Series> findAllByHouseholdId(Long householdId);

    Optional<Series> findByIdAndHouseholdId(Long id, Long householdId);

    List<Series> findByBooks(Set<Book> books);
}