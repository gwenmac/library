package library.repositories;

import library.entities.BookTag;
import org.springframework.data.repository.CrudRepository;

public interface BookTagRepository extends CrudRepository<BookTag, Long> {
}