package library.entities;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "english_sort_title")
    private String englishSortTitle;

    @Column(name = "author")
    private String author;

    @ManyToOne
    @JoinColumn (name = "series")
    @Nullable
    private Series series;

    @Column(name = "vol_num")
    @ColumnDefault("1")
    private Integer volNum;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "has_furigana")
    @Nullable
    private Boolean hasFurigana;

    @Column(name = "reading_level")
    @Nullable
    private Integer level;

    @ManyToOne
    @JoinColumn(name = "status")
    private Status status;

    @UpdateTimestamp
    @Column(name = "dlu", insertable=false)
    private Timestamp dlu;

    @CreationTimestamp
    @Column(name = "doe", updatable = false)
    private Timestamp doe;

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Nullable
    public Series getSeries() {
        return series;
    }

    public void setSeries(@Nullable Series series) {
        this.series = series;
    }

    public Integer getVolNum() {
        return volNum;
    }

    public void setVolNum(Integer volNum) {
        this.volNum = volNum;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Nullable
    public Boolean getHasFurigana() {
        return hasFurigana;
    }

    public void setHasFurigana(@Nullable Boolean hasFurigana) {
        this.hasFurigana = hasFurigana;
    }

    @Nullable
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@Nullable Integer level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getDlu() {
        return dlu;
    }

    public void setDlu(Timestamp dlu) {
        this.dlu = dlu;
    }

    public Timestamp getDoe() {
        return doe;
    }

    public void setDoe(Timestamp doe) {
        this.doe = doe;
    }
}