package library.entities;

import javax.persistence.*;

@Entity
@Table(name = "series")
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "ongoing")
    private Boolean ongoing;

    @Column(name = "available_count")
    private Integer availableCount;

    @Column(name = "read_all_owned")
    private Boolean readAllOwned;

    @Column(name = "own_all")
    private Boolean ownAll;

    @Column(name = "finished")
    private Boolean finished;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
