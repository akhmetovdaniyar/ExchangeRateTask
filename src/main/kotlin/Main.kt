import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.NullPointerException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

fun main() {

    try {
        val scanner = Scanner(System.`in`)

        println("Введите дату")
        val date = formatDate(scanner.nextLine())

        println("Введите валюту")
        val code = scanner.nextLine()

        val mainUrl = getMainUrl(date)
        val todayCurses: ValCurs? = getDataNetwork(mainUrl)
        val valute = todayCurses!!.getValuteByCode(code)

        println("${valute!!.charCode} (${valute.name}): ${valute.value}")

    } catch (_: DateTimeParseException) {
        println("Ошибка: Неправильно введена дата")
    } catch (_: NullPointerException) {
        println("Ошибка: Неправильно введена валюта")
    }
}

fun getMainUrl(date: String): String {
    val url1 = "https://www.cbr.ru/scripts/XML_daily.asp"
    return "$url1?date_req=$date"
}

fun getDataNetwork(stringUrl: String): ValCurs? {

    val url = URL(stringUrl)
    val connection = url.openConnection() as HttpURLConnection

    connection.requestMethod = "GET"
    connection.connectTimeout = 2000
    connection.readTimeout = 2000

    val responseCode = connection.responseCode

    var todayCurs: ValCurs? = null

    if (responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        val serializer = org.simpleframework.xml.core.Persister()
        todayCurs = serializer.read(ValCurs::class.java, response.trimIndent())
        reader.close()
    } else {
        println("Ошибка при выполнении запроса. Код ответа: $responseCode")
    }
    connection.disconnect()
    return todayCurs
}

fun ValCurs.getValuteByCode(charCode: String): Valute? {
    return valutes?.find { it.charCode == charCode }
}

fun formatDate(inputDate: String): String {
    val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatterOutput = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val date = LocalDate.parse(inputDate, formatterInput)
    return date.format(formatterOutput)
}