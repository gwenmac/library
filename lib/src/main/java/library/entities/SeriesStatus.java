package library.entities;

public enum SeriesStatus {
    NOT_STARTED,   // no books have been started
    IN_PROGRESS,   // at least one book started, not all completed
    COMPLETED      // all books are completed
}