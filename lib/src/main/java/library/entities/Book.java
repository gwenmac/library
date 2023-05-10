package library.entities;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "series_id")
    private Series series;

    @Column(name = "title")
    private String title;

    @Column(name = "vol_num")
    private Integer volNum;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "furigana")
    private Boolean furigana;

    @Column(name = "ln_level")
    private Integer lnLevel;

    @Column(name = "english_sort_name")
    private String englishSortName;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @Column(name = "start_ts")
    private Timestamp startTs;

    @Column(name = "complete_ts")
    private Timestamp completeTs;

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

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Boolean getFurigana() {
        return furigana;
    }

    public void setFurigana(Boolean furigana) {
        this.furigana = furigana;
    }

    public Integer getLnLevel() {
        return lnLevel;
    }

    public void setLnLevel(Integer lnLevel) {
        this.lnLevel = lnLevel;
    }

    public String getEnglishSortName() {
        return englishSortName;
    }

    public void setEnglishSortName(String englishSortName) {
        this.englishSortName = englishSortName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getStartTs() {
        return startTs;
    }

    public void setStartTs(Timestamp startTs) {
        this.startTs = startTs;
    }

    public Timestamp getCompleteTs() {
        return completeTs;
    }

    public void setCompleteTs(Timestamp completeTs) {
        this.completeTs = completeTs;
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
}