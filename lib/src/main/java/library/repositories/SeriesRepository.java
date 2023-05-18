package library.repositories;

import library.entities.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface SeriesRepository extends JpaRepository<Series, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO series (title, english_sort_title, ongoing, available_count, read_all_owned, own_all, finished) " +
            "VALUES (:title, :english_sort_title, :ongoing, :available_count, :read_all_owned, :own_all, :finished)",
            nativeQuery = true)
    void insertSeries(@Param("title") String title,
                      @Param("english_sort_title") String english_sort_title,
                      @Param("ongoing") Boolean ongoing,
                      @Param("available_count") Integer available_count,
                      @Param("read_all_owned") Boolean read_all_owned,
                      @Param("own_all") Boolean own_all,
                      @Param("finished") Boolean finished);
}