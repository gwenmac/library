package library.repositories;

import library.entities.WishlistedBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistedBookRepository extends JpaRepository<WishlistedBook, Long> {

    List<WishlistedBook> findAllByHouseholdIdOrderBySortTitleAsc(Long householdId);

    Optional<WishlistedBook> findByIdAndHouseholdId(Long id, Long householdId);
}