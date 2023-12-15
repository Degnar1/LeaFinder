import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Feature.Type
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    // Wywołanie Google Vision API
    val client = ImageAnnotatorClient.create()

    // Ustawienie lokalizacji pliku obrazka
    val fileName = "path/to/your/image.jpg"

    // Konwersja pliku obrazka na obiekt Image z biblioteki Google Cloud
    val image = Image.newBuilder().setImage(Files.readAllBytes(Paths.get(fileName))).build()

    // Określenie cech obrazka, które chcemy wykryć
    val features = listOf(
        Feature.newBuilder().setType(Type.LABEL_DETECTION).build(),
        Feature.newBuilder().setType(Type.TEXT_DETECTION).build()
    )

    // Przygotowanie żądania do API
    val request = AnnotateImageRequest.newBuilder().addAllFeatures(features).setImage(image).build()

    // Wywołanie API i otrzymanie odpowiedzi
    val response = client.batchAnnotateImages(listOf(request))

    // Wydrukowanie wykrytych informacji na konsoli
    response.forEach {
        println("Image label: ${it.labelAnnotationsList}")
        println("Image text: ${it.textAnnotationsList}")
    }

    // Zamknięcie klienta
    client.close()
}