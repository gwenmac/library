package library.controllers;

import library.entities.*;
import lombok.Getter;

import java.util.List;

@Getter
public class SuggesterRequestBody {
    private Integer minLength;
    private Integer maxLength;
    private boolean includeNoPageCount;
    private List<Language> languages;
    private List<Tag> tags;
    private List<Genre> genres;
    private List<Status> statuses;
    private boolean includeNoStatus;
    private boolean wantNewSeries;
    private boolean wantStartedSeries;
    private boolean wantStandalone;
}