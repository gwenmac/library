package library.repositories;
import library.entities.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface BookStatusRepository extends JpaRepository<BookStatus, Long> {
    Optional<BookStatus> findByBookIdAndUserId(Long bookId, Long userId);
    List<BookStatus> findAllByBookId(Long bookId);
    List<BookStatus> findAllByUserId(Long userId);
}
