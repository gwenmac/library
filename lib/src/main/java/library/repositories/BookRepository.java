package library.repositories;

import library.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByHouseholdIdOrderBySortTitleAsc(Long householdId);

    Optional<Book> findByIdAndHouseholdId(Long id, Long householdId);
}