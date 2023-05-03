package huythanh0x.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

val rootDirectory = "/Volumes/UDEMY 1/"

const val GB_UNIT = 1024 * 1024 * 1024
const val MB_UNIT = 1024 * 1024
const val KB_UNIT = 1024
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("You are in the home page, get into index to get access into the courses")
        }
        get("/index") {
            val responseContent = getHtmlIndexFromDir(rootDirectory)
            call.respondText(responseContent.toString(), ContentType.Text.Html)
        }
        get("/index/") {
            val responseContent = getHtmlIndexFromDir(rootDirectory)
            call.respondText(responseContent.toString(), ContentType.Text.Html)
        }
        get("/index/{pathParameter...}") {
            val pathParameter =
                call.parameters.getAll("pathParameter")?.joinToString("/") ?: throw Exception("PATH IS NOT RESOLVABLE")
            val directory = rootDirectory + pathParameter
            if (File(directory).isFile) {
                if (directory.endsWith(".mp4") || directory.endsWith(".mp3")) {
                    val file = File(directory)
                    call.respond(LocalFileContent(file))
                } else {
                    call.respondFile(File(directory))
                }
            } else {
                val responseContent = getHtmlIndexFromDir(directory)
                call.respondText(responseContent.toString(), ContentType.Text.Html)
            }
        }
    }
}

fun getHtmlIndexFromDir(directory: String): StringBuilder {
    val file = File(directory)
    val responseContent = StringBuilder()
    responseContent.append("<html><body>")
    responseContent.append("<h1>Index of ${directory}</h1>")
    responseContent.addIndexTopBar(file.absolutePath)
    responseContent.append("<hr>")
    if (file.parentFile != null) {
        responseContent.append("""<a href="/index/${file.parentFile.relativeTo(File(rootDirectory))}">../</a><br>""")
    }
    file.listFiles()?.sortedWith(compareBy<File> { it.isDirectory }.thenBy { it.extension }.thenBy { it.name })
        ?.forEach { subFile ->
            responseContent.append("""<a href="/index/${subFile.relativeTo(File(rootDirectory))}">${getFileSize(subFile)} ${subFile.name}</a><br>""")
        }
    responseContent.append("</body></html>")
    return responseContent
}

fun getFileSize(file: File): String {
    return if (file.length() > GB_UNIT) {
        "${file.length() / GB_UNIT} GB"
    } else if (file.length() > MB_UNIT) {
        "${file.length() / MB_UNIT} MB"
    } else if (file.length() > KB_UNIT) {
        "${file.length() / KB_UNIT} KB"
    } else {
        "${file.length()} B"
    }
}

fun StringBuilder.addIndexTopBar(directory: String) {
    var subDirs = directory.replace(rootDirectory,"").split("/")
    if(rootDirectory.contains(directory)){
        subDirs = listOf("/")
    }
    for (idx in subDirs.indices){
        println(subDirs.slice(0..idx))
        val urlToCurrentDir = subDirs.slice(0..idx).joinToString(separator = "/")
        this.append("""<a href="/index/${urlToCurrentDir}">${subDirs[idx]}</a>/""")
    }
}