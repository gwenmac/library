package library.controllers;
import library.entities.Author;
import library.entities.Book;
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
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void createBook_withTitleOnly_returnsCreated() throws Exception {
        Book saved = new Book();
        saved.setId(1L);
        saved.setTitle("Test Book");
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
        Author author = new Author();
        author.setId(1L);
        author.setName("Author One");
        Series series = new Series();
        series.setId(1L);
        series.setName("My Series");
        Book saved = new Book();
        saved.setId(2L);
        saved.setTitle("Full Book");
        saved.setDescription("A great book");
        saved.setPageCount(300);
        saved.setSeries(series);
        saved.setSeriesOrder(1);
        when(seriesRepository.findById(1L)).thenReturn(Optional.of(series));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(saved);
        String json = "{\"title\": \"Full Book\", \"description\": \"A great book\", \"pageCount\": 300, \"seriesId\": 1, \"seriesOrder\": 1, \"authorIds\": [1]}";
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
    void createBook_withTitleOnly_exactUiPayload_returnsCreated() throws Exception {
        Book saved = new Book();
        saved.setId(1L);
        saved.setTitle("Test Book");
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // This is the exact payload the Vue UI sends when only the title is filled in
        String json = "{\"title\":\"Test Book\",\"description\":\"\",\"pageCount\":null,\"seriesId\":null,\"seriesOrder\":null,\"authorIds\":[]}";

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
}
