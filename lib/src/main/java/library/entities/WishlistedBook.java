package library.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import library.util.JapaneseUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "wishlisted_book")
public class WishlistedBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "sort_title")
    private String sortTitle;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Household user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "release_date")
    private LocalDateTime releaseDate = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "wishlisted_book_author",
            joinColumns = @JoinColumn(name = "wishlisted_book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    private void populateSortName() {
        if (title == null) return;

        if (JapaneseUtil.containsJapanese(title)) {
            this.sortTitle = JapaneseUtil.toRomaji(title);
        } else {
            this.sortTitle = title.replaceFirst("(?i)^(the|a)\\s+", "");
        }
    }

    @PrePersist
    @PreUpdate
    private void onWishlistedBookChange() {
        populateSortName();
    }
}
