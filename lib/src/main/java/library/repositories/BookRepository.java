package library.repositories;

import library.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO book (title, english_sort_title, series_id, vol_num, language_id, furigana, ln_level, status_id) " +
            "VALUES (:title, :english_sort_title, :series_id, :vol_num, :language_id, :furigana, :ln_level, :status_id)",
            nativeQuery = true)
    void insertBook(@Param("title") String title,
                    @Param("english_sort_title") String english_sort_title,
                    @Param("series_id") Integer series_id,
                    @Param("vol_num") Integer vol_num,
                    @Param("language_id") Integer language_id,
                    @Param("furigana") Boolean furigana,
                    @Param("ln_level") Integer ln_level,
                    @Param("status_id") Integer status_id);
}