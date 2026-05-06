package library.repositories;

import library.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByUserIdOrderBySortTitleAsc(Long userId);

    Optional<Book> findByIdAndUserId(Long id, Long userId);
}