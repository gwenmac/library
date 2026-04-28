package library.entities;

import library.util.JapaneseUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authors;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "book_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "book_languages",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private Set<Language> languages = new HashSet<>();

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private BookStatus bookStatus;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    @ManyToOne
    @JoinColumn(name = "series_id")  // nullable — not every book belongs to a series
    private Series series;

    @Column(name = "series_order")
    private Integer seriesOrder; // e.g. 1, 2, 3 — null if not in a series

    @Column(name = "sort_title")
    private String sortTitle;

    private void populateSortName() {
        if (title == null) return;

        if (JapaneseUtil.containsJapanese(title)) {
            this.sortTitle = JapaneseUtil.toRomaji(title);
        } else {
            this.sortTitle = title;
        }
    }

    @PrePersist
    @PreUpdate
    private void onBookChange() {
        populateSortName();

        if (series != null) {
            series.recalculateStatus();
        }
    }
}