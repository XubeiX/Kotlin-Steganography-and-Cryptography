package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.nio.file.InvalidPathException
import javax.imageio.ImageIO
import kotlin.io.path.Path

enum class Task(var message: String) {
    EXIT("Bye!"),
    HIDE("Hiding message in image."),
    SHOW("Obtaining message from image."),
    ERROR("")
}

fun createTask(command: String) =
    Task.values().firstOrNull{it.name == command.uppercase() } ?: Task.ERROR.apply { message = "Wrong task: $command" }


fun main() {
    val cryptographer = Cryptographer()

    do {
        println("Task (hide, show, exit):")
        val command = readln()
        val task = createTask(command)
        try {
            when (task) {
                Task.HIDE -> startHidingMessage(cryptographer)
                Task.SHOW -> showHidedMessage(cryptographer)
                else -> println(task.message)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    } while (command != "exit")
}

fun startHidingMessage(crypto: Cryptographer) {
    println("Input image file:")
    val inputFileName = readln()

    println("Output image file:")
    val outputFile = readln()

    println("Message to hide:")
    val message = readln()

    println("Password:")
    val password = readln()

    crypto.encodeMessageInImage(inputFileName, outputFile, message, password)
}

fun showHidedMessage(crypto: Cryptographer) {
    println("Input image file:")
    val inputFile = readln()

    print("Password:")
    val password = readln()

    crypto.decodeImage(inputFile, password)
}


