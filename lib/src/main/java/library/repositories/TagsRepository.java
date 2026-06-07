package library.repositories;

import library.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagsRepository  extends JpaRepository<Tag, Long> {
    List<Tag> findAllByHouseholdId(Long householdId);
    Optional<Tag> findByIdAndHouseholdId(Long id, Long householdId);
    Optional<Tag> findByNameAndHouseholdId(String name, Long householdId);
}
