package library.entities;

import javax.persistence.*;

@Entity
@Table(name = "series")
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "ongoing")
    private Boolean ongoing;

    @Column(name = "available_count")
    private Integer availableCount;

    @Column(name = "read_all_owned")
    private Boolean readAllOwned;

    @Column(name = "own_all")
    private Boolean ownAll;

    @Column(name = "english_sort_title")
    private String englishSortTitle;

    @Column(name = "finished")
    private Boolean finished;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getOngoing() {
        return ongoing;
    }

    public void setOngoing(Boolean ongoing) {
        this.ongoing = ongoing;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

    public Boolean getReadAllOwned() {
        return readAllOwned;
    }

    public void setReadAllOwned(Boolean readAllOwned) {
        this.readAllOwned = readAllOwned;
    }

    public Boolean getOwnAll() {
        return ownAll;
    }

    public void setOwnAll(Boolean ownAll) {
        this.ownAll = ownAll;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getEnglishSortTitle() {
        return englishSortTitle;
    }

    public void setEnglishSortTitle(String englishSortTitle) {
        this.englishSortTitle = englishSortTitle;
    }
}
