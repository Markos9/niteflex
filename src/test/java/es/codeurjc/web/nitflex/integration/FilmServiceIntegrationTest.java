package es.codeurjc.web.nitflex.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.codeurjc.web.nitflex.model.Film;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.FilmRepository;
import es.codeurjc.web.nitflex.repository.UserRepository;
import es.codeurjc.web.nitflex.service.FilmService;
import es.codeurjc.web.nitflex.ImageTestUtils;
import es.codeurjc.web.nitflex.dto.film.CreateFilmRequest;
import es.codeurjc.web.nitflex.dto.film.FilmDTO;
import es.codeurjc.web.nitflex.dto.film.FilmSimpleDTO;
import es.codeurjc.web.nitflex.utils.ImageUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUtils imageUtils;

    private Film film;
    private List<User> users;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Crear una película
        film = new Film();
        film.setTitle("Original Title");
        film.setSynopsis("Original Synopsis");
        film = filmRepository.save(film);

        // Crear varios usuarios y añadir la película a sus favoritos
        users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setFavoriteFilms(new ArrayList<>());
            user.getFavoriteFilms().add(film);
            film.getUsersThatLiked().add(user);
            user = userRepository.save(user);
            // para poder recorrerlos después con el forecah los guardo en el arraylist
            users.add(user);
        }
    }

    @Test
    @Transactional
    public void whenUpdateFilmTitleAndSynopsis_thenChangesAreSavedAndFavoritesRemain() {
        // Arrange
        long filmId = film.getId();
        FilmSimpleDTO updatedFilm = new FilmSimpleDTO(filmId, "Updated Title", "Updated Synopsis",
                film.getReleaseYear(), film.getAgeRating());

        // Act
        filmService.update(filmId, updatedFilm);

        // Assert
        Film updatedFilmFromDb = filmRepository.findById(filmId).orElseThrow();
        assertThat(updatedFilmFromDb.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedFilmFromDb.getSynopsis()).isEqualTo("Updated Synopsis");

        for (User user : users) {
            User userFromDb = userRepository.findById(user.getId()).orElseThrow();
            assertThat(userFromDb.getFavoriteFilms()).contains(updatedFilmFromDb);
        }
    }

    @Test // Cassiel
    @Transactional
    public void whenDeleteExistFilm_thenIsDeletedInRepositoryAndFavoriteListUsers() {

        // Arrange
        long filmId = film.getId();
        // Act
        // Verificar que la película existe previamente en el repositorio
        assertThat(filmRepository.existsById(filmId)).isTrue(); // Verificar que la película está en la base de datos
        filmService.delete(filmId);
        // Assert
        assertThat(filmRepository.existsById(filmId)).isFalse();
        for (User user : users) {
            User userFromDb = userRepository.findById(user.getId()).orElseThrow();
            assertThat(userFromDb.getFavoriteFilms()).doesNotContain(film);
        }
    }

    // Marcos
    @Test
    @Transactional
    public void whenUpdateFilmTitleAndSynopsis_thenImageRemainsTheSame() throws SQLException, IOException {
        // Given
        Film imageFilm = new Film();

        imageFilm.setTitle("Original Title");
        imageFilm.setSynopsis("Original Synopsis");
        Blob image = imageUtils.multiPartFileImageToBlob(ImageTestUtils.createSampleImage());
        imageFilm.setPosterFile(image);
        imageFilm = filmRepository.save(film);

        Long filmId = imageFilm.getId();
        FilmSimpleDTO updatedFilm = new FilmSimpleDTO(filmId, "Updated Title", "Updated Synopsis",
                film.getReleaseYear(), film.getAgeRating());

        Blob originalImage = film.getPosterFile();

        // When
        filmService.update(filmId, updatedFilm, null);

        // Then
        Film updatedFilmFromDb = filmRepository.findById(filmId).orElseThrow();
        assertThat(updatedFilmFromDb.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedFilmFromDb.getSynopsis()).isEqualTo("Updated Synopsis");

        assertTrue(ImageTestUtils.areSameBlob(updatedFilmFromDb.getPosterFile(), originalImage));
    }

    @Test // Pablo
    @Transactional
    public void whenFilmIsAddIsSavedAndReturnsCreatedFilm() {
        // Arrange
        CreateFilmRequest filmtoSave = new CreateFilmRequest("Saved Title", "Saved Synopsis", film.getReleaseYear(),
                film.getAgeRating());

        // Act
        FilmDTO savedFilm = filmService.save(filmtoSave);

        // Assert
        long filmId = savedFilm.id();
        Film SavedFilmFromDb = filmRepository.findById(filmId).orElseThrow();
        assertThat(SavedFilmFromDb.getTitle()).isEqualTo("Saved Title");
        assertThat(SavedFilmFromDb.getSynopsis()).isEqualTo("Saved Synopsis");

        assertTrue(filmService.exist(filmId));

    }

}