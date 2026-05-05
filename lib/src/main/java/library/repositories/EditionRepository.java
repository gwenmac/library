package library.repositories;

import library.entities.Edition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditionRepository extends JpaRepository<Edition, Long> {
    List<Edition> findByName(String name);
}
