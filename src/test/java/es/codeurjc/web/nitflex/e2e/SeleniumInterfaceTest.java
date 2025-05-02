package es.codeurjc.web.nitflex.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import es.codeurjc.web.nitflex.Application;
import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.UserRepository;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SeleniumInterfaceTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        testUser = new User("testuser", "testuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (testUser != null) {
            userRepository.delete(testUser);
        }
    }

    @Test
    public void testAddNewFilmWithoutImage() {
        // Accede a la página principal de la aplicación
        driver.get("http://localhost:" + this.port);

        // Espera a que el botón de crear nueva película sea interactivo y haz clic en
        // él
        WebElement newFilmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("create-film")));
        newFilmButton.click();

        // Datos de prueba para la nueva película
        String testTitle = "Barbie";
        String testYear = "2023";
        String testSynopsis = "Barbie (Margot Robbie) lleva una vida ideal en Barbieland...";
        String testAgeRating = "+7";

        // GENERACIÓN

        // Rellena el formulario con los datos de la película
        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        titleField.sendKeys(testTitle);

        WebElement yearField = driver.findElement(By.id("releaseYear"));
        yearField.sendKeys(testYear);

        WebElement synopsisField = driver.findElement(By.id("synopsis"));
        synopsisField.sendKeys(testSynopsis);

        // Selecciona la clasificación de edad desde el dropdown
        WebElement ageRatingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("ageRating")));
        ageRatingDropdown.click();
        WebElement ageRatingOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//select[@id='ageRating']/option[text()='+7']")));
        ageRatingOption.click();

        // Guarda la nueva película
        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();

        // VERIFICACIONES

        // Verifica que el título aparece en la página
        WebElement filmTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), '" + testTitle + "')]")));
        assertTrue(filmTitle.isDisplayed() && filmTitle.getText().trim().equals(testTitle),
                "El título de la película debería ser visible y coincidir en la página generada");

        // Verifica que el año de estreno es correcto
        WebElement releaseYear = driver.findElement(By.xpath(
                "//*[contains(text(), '" + testYear + "')]"));
        assertTrue(releaseYear.isDisplayed() && releaseYear.getText().contains(testYear),
                "El año de lanzamiento debería ser visible y coincidir en la página generada");

        // Verifica que la sinopsis es correcta
        WebElement synopsis = driver.findElement(By.xpath(
                "//*[contains(text(), '" + testSynopsis + "')]"));
        assertTrue(synopsis.isDisplayed() && synopsis.getText().contains(testSynopsis),
                "La sinopsis debería ser visible y coincidir en la página generada");

        // Verifica que la clasificación por edad es correcta
        WebElement ageRating = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'ui large label')]")));
        assertTrue(ageRating.isDisplayed() && ageRating.getText().trim().contains(testAgeRating),
                "La clasificación por edad debería ser visible y coincidir en la página generada");
    }

    @Test
    public void testAddNewFilmThenDelete() {
        driver.get("http://localhost:" + this.port);

        // Crear nueva película
        String testTitle = "Prueba";
        WebElement newFilmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("create-film")));
        newFilmButton.click();

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        titleField.sendKeys(testTitle);

        WebElement yearField = driver.findElement(By.id("releaseYear"));
        yearField.sendKeys("1995");

        WebElement synopsisField = driver.findElement(By.id("synopsis"));
        synopsisField.sendKeys("Película de acción y aventura.");

        WebElement ageRatingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("ageRating")));
        ageRatingDropdown.click();
        WebElement ageRatingOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//select[@id='ageRating']/option[text()='+7']")));
        ageRatingOption.click();

        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();

        // Verificar que la película aparece en la lista
        WebElement filmTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), '" + testTitle + "')]")));
        assertTrue(filmTitle.isDisplayed(), "La película debería aparecer en la lista.");

        // Eliminar la película
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("remove-film")));
        deleteButton.click();
        //Navegar a pantalla principal
        WebElement homeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("all-films")));
        homeButton.click();

        // Verificar que la película ya no aparece
        try {
            filmTitle = driver.findElement(By.xpath("//*[contains(text(), '" + testTitle + "')]"));
            assertTrue(false, "La película debería haber sido eliminada.");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            // Se espera que la película ya no esté presente, lo que indica que fue eliminada correctamente.
            assertTrue(true, "La película ha sido eliminada correctamente.");
        }
    }
    // T3 EJ4
    @Test
    public void testAddNewFilm_ThenEditTitleChangeIsSaved() {
        driver.get("http://localhost:" + this.port);

        // Create Movie
        String testTitle = "Blackhawk Down";
        String updatedTitle = testTitle + " - parte 2";
        String testYear = "2001";
        String testSynopsis = "Octubre de 1993. Soldados americanos de élite son enviados a Mogadiscio ...";

        WebElement newFilmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("create-film")));
        newFilmButton.click();

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        titleField.sendKeys(testTitle);

        WebElement yearField = driver.findElement(By.id("releaseYear"));
        yearField.sendKeys(testYear);

        WebElement synopsisField = driver.findElement(By.id("synopsis"));
        synopsisField.sendKeys(testSynopsis);

        WebElement ageRatingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("ageRating")));
        ageRatingDropdown.click();
        WebElement ageRatingOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//select[@id='ageRating']/option[text()='+18']")));
        ageRatingOption.click();

        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();

        // Verify title appears in details view
        WebElement filmTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("film-title")));
        assertTrue(filmTitle.isDisplayed() && filmTitle.getText().trim().equals(testTitle),
                "El título de la película debería aparecer correctamente.");

        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-film")));
        editButton.click();

        // Edit movie title
        WebElement editTitleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        editTitleField.clear();
        editTitleField.sendKeys(updatedTitle);

        WebElement saveEditButton = driver.findElement(By.id("save"));
        saveEditButton.click();

        // Verify movie title its updated
        WebElement updatedFilmTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("film-title")));
        assertTrue(updatedFilmTitle.isDisplayed() && updatedFilmTitle.getText().trim().equals(updatedTitle),
                "El título de la película debería haberse actualizado correctamente.");
    }
    @Test
    public void testAddNewFilmWithoutTitle() {
        // Accede a la página principal de la aplicación
        driver.get("http://localhost:" + this.port);
    
        // Espera a que el botón de crear nueva película sea interactivo y haz clic en él
        WebElement newFilmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("create-film")));
        newFilmButton.click();
    
        // Datos de prueba para la nueva película sin título
        String testYear = "2023";
        String testSynopsis = "Tras encontrarse con Gwen Stacy, el agradable vecindario de Brooklyn en el que vive Mike Morales se ve transportado al multiverso, donde Spiderman conocerá a nuevos personajes y vivirá aventuras increíbles.";
        // String testAgeRating = "+7";
    
        // Rellena el formulario con los datos de la película, dejando el título vacío
        WebElement yearField = driver.findElement(By.id("releaseYear"));
        yearField.sendKeys(testYear);
    
        WebElement synopsisField = driver.findElement(By.id("synopsis"));
        synopsisField.sendKeys(testSynopsis);
    
        // Selecciona la clasificación de edad desde el dropdown
        WebElement ageRatingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("ageRating")));
        ageRatingDropdown.click();
        WebElement ageRatingOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//select[@id='ageRating']/option[text()='+7']")));
        ageRatingOption.click();
    
        // Intenta guardar la nueva película
        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();
    
        // Aumenta el tiempo de espera y usa una condición diferente
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'The title is empty')]")));
        assertTrue(errorMessage.isDisplayed(), "Debería mostrarse un mensaje de error indicando que el título es obligatorio.");
    
        // Verifica que la película no aparece en la página principal
        driver.get("http://localhost:" + this.port);
        boolean filmExists = driver.findElements(By.xpath("//*[contains(text(), '" + testSynopsis + "')]")).isEmpty();
        assertTrue(filmExists, "La película no debería aparecer en la página principal.");
    }
}