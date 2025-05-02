# Informe de cobertura

Cobertura obtenida de instrucciones: 46%

Cobertura de las ramas: 33%

### ¿Qué clases/métodos crees que faltan por cubrir con pruebas?

Respecto a los modelos...

Film: tiene una cobertura de 47% y creo que sería coherente y beneficioso probar los métodos removeUser() y removeReview() de esta clase para ver que se borran los usuarios que han dado like y las reseñas puestas en cada pelicula

Respecto a ImageUtils...

Estaría bien probar el resto de métodos para usar imágenes, ya que solo usamos multiPartFileImageToBlob(MultipartFile) y deberíamos comprobar el resto de métodos como remoteImageToBlob(String) y localImageToBlob(String). Además comprobar las excepciones cuando no se logra cambiar de imagen (sea cual sea) a blob

Respecto a los servicios (Services)

Se debería testear el borrar un usuario y obtenerlos a través de dicha clase, ya que se hizo a través del repositorio directamente.

Se debería hacer mucho más testing con las reseñas, ya que prácticamente no se ha verificado que funcionen bien. Se debería comprobar el método addReview() y deleteReview()

Tampoco se prueban apenas los métodos relacionados con el sistema de favoritos, como addToFavorites() o removeFromFavorites().

Respecto al paquete de configuración (Configuration)

Creemos que sería correcto incorporar pruebas unitarias o de integración a los metodos de gestión de errores RestErrorHandler y WebErrorHandler.

### ¿Qué clases/métodos crees que no hace falta cubrir con pruebas?

La clase de DatabaseInitializer y de Application no habría que testearlas de forma intensiva, ya que la primera solo sirve para guardar en los repositorios las películas y usuarios predefinidos por los profesores y en segundo lugar, para tener el main de la aplicación y poder lanzarla.

Respecto a los modelos, creo que es inútil (menos los métodos que comenté antes) ya que en su mayoría son getters y setters

Los mappers y DTOs, creo que en sí probarlos de forma unitaria es irrelevante ya que solo sirven como estructuras de datos para transferir información entre capas y normalmente no contienen lógica compleja. Su correcto funcionamiento se valida indirectamente a través de pruebas de integración o pruebas del servicio que los utilizan.

UserModelAttributes no es tan necesaria testearla de forma unitaria, ya que no contiene lógica propia compleja solo devuelve datos desde UserComponent.

La lógica de UserWebController tampoco consideramos que sea lo suficientemente compleja como para tener que cubrirla.
