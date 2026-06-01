package library.repositories;

import library.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findAllByHouseholdIdOrderBySortTitleAsc(Long householdId);

    Optional<Book> findByIdAndHouseholdId(Long id, Long householdId);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.household.id = :householdId")
    long countByHouseholdId(@Param("householdId") Long householdId);
}