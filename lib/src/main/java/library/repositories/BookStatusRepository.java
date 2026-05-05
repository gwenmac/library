package library.repositories;
import library.entities.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface BookStatusRepository extends JpaRepository<BookStatus, Long> {
    Optional<BookStatus> findByBookId(Long bookId);
}
