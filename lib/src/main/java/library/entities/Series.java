package library.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "series")
public class Series {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Book> books = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SeriesStatus status = SeriesStatus.NOT_STARTED;

    // Called automatically when the Series is loaded from the DB
    @PostLoad
    public void recalculateStatus() {
        if (books == null || books.isEmpty()) {
            this.status = SeriesStatus.NOT_STARTED;
            return;
        }

        long completed = books.stream()
                .filter(b -> b.getBookStatus() != null &&
                        "Completed".equalsIgnoreCase(b.getBookStatus().getStatus().getName()))
                .count();

        boolean anyStarted = books.stream()
                .anyMatch(b -> b.getBookStatus() != null);

        if (completed == books.size()) {
            this.status = SeriesStatus.COMPLETED;
        } else if (anyStarted) {
            this.status = SeriesStatus.IN_PROGRESS;
        } else {
            this.status = SeriesStatus.NOT_STARTED;
        }
    }
}