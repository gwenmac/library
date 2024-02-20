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

    @Column(name = "english_sort_title")
    private String englishSortTitle;

    @Column(name = "is_ongoing")
    private Boolean isOngoing;

    @Column(name = "num_available")
    private Integer numAvailable;

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

    public String getEnglishSortTitle() {
        return englishSortTitle;
    }

    public void setEnglishSortTitle(String englishSortTitle) {
        this.englishSortTitle = englishSortTitle;
    }

    public Boolean getOngoing() {
        return isOngoing;
    }

    public void setOngoing(Boolean ongoing) {
        isOngoing = ongoing;
    }

    public Integer getNumAvailable() {
        return numAvailable;
    }

    public void setNumAvailable(Integer numAvailable) {
        this.numAvailable = numAvailable;
    }
}