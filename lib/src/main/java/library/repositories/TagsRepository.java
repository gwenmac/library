package library.repositories;

import library.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository  extends JpaRepository<Tag, Long> {
}
