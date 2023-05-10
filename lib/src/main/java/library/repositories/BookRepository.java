package library.repositories;

import library.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO book (title, series_id, vol_num, language_id, furigana, ln_level, english_sort_name, status_id, start_ts, complete_ts) " +
            "VALUES (:title, :series_id, :vol_num, :language_id, :furigana, :ln_level, :english_sort_name, :status_id, :start_ts, :complete_ts)",
            nativeQuery = true)
    void insertBook(@Param("title") String title,
                    @Param("series_id") Integer series_id,
                    @Param("vol_num") Integer vol_num,
                    @Param("language_id") Integer language_id,
                    @Param("furigana") Boolean furigana,
                    @Param("ln_level") Integer ln_level,
                    @Param("english_sort_name") String english_sort_name,
                    @Param("status_id") Integer status_id,
                    @Param("start_ts") Timestamp start_ts,
                    @Param("complete_ts") Timestamp complete_ts);
}