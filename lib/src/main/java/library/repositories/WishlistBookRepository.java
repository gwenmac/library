package library.repositories;

import library.entities.WishlistBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistBookRepository extends JpaRepository<WishlistBook, Long> {

    List<WishlistBook> findAllByHouseholdIdOrderBySortTitleAsc(Long householdId);

    Optional<WishlistBook> findByIdAndHouseholdId(Long id, Long householdId);
}