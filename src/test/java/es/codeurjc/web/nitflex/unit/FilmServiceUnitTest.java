package es.codeurjc.web.nitflex.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mapstruct.factory.Mappers;

import es.codeurjc.web.nitflex.model.Film;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.FilmRepository;
import es.codeurjc.web.nitflex.repository.UserRepository;
import es.codeurjc.web.nitflex.service.FilmService;
import es.codeurjc.web.nitflex.service.exceptions.FilmNotFoundException;
import es.codeurjc.web.nitflex.utils.ImageUtils;
import es.codeurjc.web.nitflex.dto.film.CreateFilmRequest;
import es.codeurjc.web.nitflex.dto.film.FilmMapper;

public class FilmServiceUnitTest {

    // Mockeamos los repositorios y las dependencias para no hacer uso de la base de
    // datos
    // y poder centrarnos en la lógica de la clase FilmService

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageUtils imageUtils;

    @InjectMocks
    private FilmService filmService;

    private FilmMapper filmMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        filmMapper = Mappers.getMapper(FilmMapper.class);
        filmService = new FilmService(filmRepository, userRepository, imageUtils, filmMapper);
    }

    @Test // Gabriel
    public void whenDeleteExistingFilm_thenFilmMustRemovedFromRepositoryAndUsersFavorites() {
        // Arrange
        long filmId = 1L;
        Film film = new Film();
        film.setId(filmId);
        Set<User> usersThatLiked = new HashSet<>();

        // Crear varios usuarios y añadir la película a sus favoritos
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setId((long) i);
            user.setFavoriteFilms(new ArrayList<>());
            user.getFavoriteFilms().add(film);
            usersThatLiked.add(user);
            film.addUser(user);

        }

        // Uso de mock para que simule la busqueda de la película en el repositorio
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(film));

        // Act
        filmService.delete(filmId);

        // Assert
        assertThat(filmRepository.findById(filmId)).isNotIn(filmRepository);
        for (User user : usersThatLiked) {
            assertThat(user.getFavoriteFilms()).doesNotContain(film);
        }
    }
    @Test // Cassiel
    public void whenSavefilmWithoutImageAndValidTitle_thenIsSavedInRepository(){
        // Arrange
        CreateFilmRequest filmRequest = new CreateFilmRequest("Spider-Man: Cruzando el Multiverso", "Synopsis", 2025, "+12");
        //Act
        filmService.save(filmRequest);
        // Assert
        verify(filmRepository).save(any(Film.class));

    }
    @Test // Pablo
    public void whenDeleteNonExistingFilm_thenDoesntDeleteAndThrowsException() {
        // Arrange
        long filmId = 1L;
        String expectedErrorMessage = "Film not found with id: " + filmId;

        when(filmRepository.findById(filmId)).thenReturn(Optional.empty());

        // Act & Assert
        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> {
            filmService.delete(filmId);
        }, "Se espera que se lance FilmNotFoundException");

        // Verifico el mensaje de la excepción
        assertThat(exception.getMessage()).isEqualTo(expectedErrorMessage);

        // Compruebo que no se ha eliminado del repositorio
        verify(filmRepository, never()).deleteById(filmId);
    }

    @Test // Marcos
    public void whenSaveFilmWithoutImageAndEmptyTitle_thenDoesntSaveAndThrowsException() {
        // Given
        CreateFilmRequest filmRequest = new CreateFilmRequest("", "Synopsis", 2025, "+12");

        // When Then
        assertThatThrownBy(() -> filmService.save(filmRequest, (Blob) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The title is empty");

        verify(filmRepository, never()).save(any());

    }

}