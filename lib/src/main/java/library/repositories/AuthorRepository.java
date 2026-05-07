package library.repositories;

import library.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByName(String name);

    List<Author> findAllByHouseholdId(Long householdId);

    Optional<Author> findByIdAndHouseholdId(Long id, Long householdId);
}
