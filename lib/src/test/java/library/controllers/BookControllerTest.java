package library.controllers;

import library.entities.Author;
import library.entities.Book;
import library.entities.Genre;
import library.entities.Series;
import library.repositories.AuthorRepository;
import library.repositories.BookRepository;
import library.repositories.GenreRepository;
import library.repositories.SeriesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private SeriesRepository seriesRepository;
    @MockBean
    private AuthorRepository authorRepository;
    @MockBean
    private GenreRepository genreRepository;

    // ── GET /authors/all ────────────────────────────────────

    @Test
    void getAllAuthors_returnsAuthorList() throws Exception {
        Author a1 = new Author(); a1.setId(1L); a1.setName("Author A");
        Author a2 = new Author(); a2.setId(2L); a2.setName("Author B");
        when(authorRepository.findAll()).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/authors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Author A"));
    }

    // ── POST /authors ───────────────────────────────────────

    @Test
    void createAuthor_returnsCreated() throws Exception {
        Author saved = new Author(); saved.setId(1L); saved.setName("New Author");
        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        mockMvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Author\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Author"));
    }

    // ── GET /genres/all ─────────────────────────────────────

    @Test
    void getAllGenres_returnsGenreList() throws Exception {
        Genre g1 = new Genre(); g1.setId(1L); g1.setName("Fantasy");
        when(genreRepository.findAll()).thenReturn(List.of(g1));

        mockMvc.perform(get("/genres/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Fantasy"));
    }

    // ── POST /genres ────────────────────────────────────────

    @Test
    void createGenre_returnsCreated() throws Exception {
        Genre saved = new Genre(); saved.setId(1L); saved.setName("Sci-Fi");
        when(genreRepository.save(any(Genre.class))).thenReturn(saved);

        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sci-Fi\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sci-Fi"));
    }

    // ── GET /all ────────────��───────────────────────────────

    @Test
    void getAll_returnsBookList() throws Exception {
        Book b1 = new Book(); b1.setId(1L); b1.setTitle("Book One");
        Book b2 = new Book(); b2.setId(2L); b2.setTitle("Book Two");
        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book One"))
                .andExpect(jsonPath("$[1].title").value("Book Two"));
    }

    // ── POST /books ─────────────────────────────────────────

    @Test
    void createBook_withTitleOnly_returnsCreated() throws Exception {
        Book saved = new Book(); saved.setId(1L); saved.setTitle("Test Book");
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test Book\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void createBook_withAllFields_returnsCreated() throws Exception {
        Author author = new Author(); author.setId(1L); author.setName("Author One");
        Genre genre = new Genre(); genre.setId(1L); genre.setName("Fantasy");
        Series series = new Series(); series.setId(1L); series.setName("My Series");
        Book saved = new Book();
        saved.setId(2L); saved.setTitle("Full Book"); saved.setDescription("A great book");
        saved.setPageCount(300); saved.setSeries(series); saved.setSeriesOrder(1);

        when(seriesRepository.findById(1L)).thenReturn(Optional.of(series));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anyList())).thenReturn(List.of(genre));
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        String json = "{\"title\":\"Full Book\",\"description\":\"A great book\",\"pageCount\":300,\"seriesId\":1,\"seriesOrder\":1,\"authorIds\":[1],\"genreIds\":[1]}";
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Full Book"))
                .andExpect(jsonPath("$.description").value("A great book"))
                .andExpect(jsonPath("$.pageCount").value(300));
    }

    @Test
    void createBook_exactUiPayload_returnsCreated() throws Exception {
        Book saved = new Book(); saved.setId(1L); saved.setTitle("Test Book");
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        String json = "{\"title\":\"Test Book\",\"description\":\"\",\"pageCount\":null,\"seriesId\":null,\"seriesOrder\":null,\"authorIds\":[],\"genreIds\":[]}";
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void createBook_withInvalidSeriesId_returnsBadRequest() throws Exception {
        when(seriesRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Bad Book\", \"seriesId\": 999}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBook_withInvalidAuthorId_returnsBadRequest() throws Exception {
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Bad Book\", \"authorIds\": [999]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBook_withInvalidGenreId_returnsBadRequest() throws Exception {
        when(genreRepository.findAllById(anyList())).thenReturn(List.of());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Bad Book\", \"genreIds\": [999]}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /books/{id} ─────────────────────────────────────

    @Test
    void getById_returnsBook() throws Exception {
        Book book = new Book(); book.setId(1L); book.setTitle("Found Book");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Found Book"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/999"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /books/{id} ─────────────────────────────────────

    @Test
    void updateBook_updatesTitle() throws Exception {
        Book existing = new Book(); existing.setId(1L); existing.setTitle("Old Title");
        existing.setAuthors(new HashSet<>()); existing.setGenres(new HashSet<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void updateBook_clearsSeries() throws Exception {
        Series series = new Series(); series.setId(1L); series.setName("Old Series");
        Book existing = new Book(); existing.setId(1L); existing.setTitle("Book");
        existing.setSeries(series); existing.setAuthors(new HashSet<>()); existing.setGenres(new HashSet<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seriesId\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.series").isEmpty());
    }

    @Test
    void updateBook_notFound_returns404() throws Exception {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Nope\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_withInvalidSeriesId_returnsBadRequest() throws Exception {
        Book existing = new Book(); existing.setId(1L); existing.setTitle("Book");
        existing.setAuthors(new HashSet<>()); existing.setGenres(new HashSet<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(seriesRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seriesId\": 999}"))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /books/{id} ───────────────────────────────────

    @Test
    void deleteBook_returnsNoContent() throws Exception {
        Book existing = new Book(); existing.setId(1L); existing.setTitle("Doomed");
        existing.setAuthors(new HashSet<>()); existing.setGenres(new HashSet<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent());

        verify(bookRepository).delete(existing);
    }

    @Test
    void deleteBook_notFound_returns404() throws Exception {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/books/999"))
                .andExpect(status().isNotFound());

        verify(bookRepository, never()).delete(any());
    }
}
