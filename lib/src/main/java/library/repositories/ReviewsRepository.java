package library.repositories;

import library.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewsRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookIdAndUserId(Long bookId, Long userId);
    List<Review> findAllByBookId(Long bookId);
    List<Review> findAllByUserId(Long userId);
}
