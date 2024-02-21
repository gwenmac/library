package library.repositories;

import library.entities.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookGenreRepository extends JpaRepository<BookGenre, Long> {
    List<BookGenre> findByBookId(Long bookId);
}