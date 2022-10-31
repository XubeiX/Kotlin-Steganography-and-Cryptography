package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.nio.file.InvalidPathException
import javax.imageio.ImageIO
import javax.naming.SizeLimitExceededException
import kotlin.experimental.xor

class Cryptographer {

    fun decodeImage(inputPath: String, password: String){
        if (!checkFileExist(inputPath))
            throw FileNotFoundException("Can't read input file!")

        if(!checkFileFormatIsPNG(inputPath))
            throw InvalidPathException(inputPath, "Input file must be a png file!")

        val image = readImage(File(inputPath))

        val message = mutableListOf<Int>()
        val endSignLength = convertBytesToBits(addEndingSign(ByteArray(0))).size

        start@ for (y in 0 until image.height) {
            for (x in 0 until image.width){
                message.add(Color(image.getRGB(x,y)).blue % 2)
                if(message.size >= endSignLength && message.size % 8 == 0 && endSignatureMatch(message)){
                    println("Message:")
                    println(encodeDecodeMessage(decodeRawMessage(message, endSignLength), password))
                    break@start
                }
            }
        }
    }

    fun encodeMessageInImage(inputPath: String, outputPath: String, message: String, password: String) {
        if (!checkFileExist(inputPath))
            throw FileNotFoundException("Can't read input file!")

        if(!checkFileFormatIsPNG(inputPath))
            throw InvalidPathException(inputPath, "Input file must be a png file!")

        if(!checkFileFormatIsPNG(outputPath))
            throw InvalidPathException(outputPath, "Output file must be a png file!")

        val inputFile = File(inputPath)
        val outputFile = File(outputPath)

        val image = readImage(inputFile)

        if(canHideMessage(image, message)) {
            val processedImage = hideMessageInImage(image, encodeDecodeMessage(message, password))

            println("Message saved in $outputPath image.")
            ImageIO.write(processedImage, "png", outputFile)
        }
        else
        {
            throw SizeLimitExceededException("The input image is not large enough to hold this message.")
        }
    }

    private fun encodeDecodeMessage(message: String, password: String) : String {
        val messageInBytes = message.encodeToByteArray()
        val passwordInBytes = password.encodeToByteArray()

        return messageInBytes.mapIndexed{index, byte -> byte xor passwordInBytes[index % passwordInBytes.size]}.toByteArray().decodeToString()
    }

    private fun hideMessageInImage(image: BufferedImage, message: String) : BufferedImage {
        val messageBits = convertMessageToBits(message)

        var accu = 0
        image.apply {
            main@ for (y in 0 until this.height) {
                for (x in 0 until this.width) {
                    if(accu < messageBits.size){
                        val color = Color(this.getRGB(x,y))
                        this.setRGB(x, y, Color(color.red, color.green, color.blue.and(254).or(messageBits[accu]) % 256).rgb)
                        accu++
                    } else {
                        break@main
                    }
                }
            }
        }
        return image
    }

    private fun convertMessageToBits(message: String): List<Int> {
        return convertBytesToBits(message.encodeToByteArray().run { addEndingSign(this) } )
    }

    private fun decodeRawMessage(messageBits: List<Int>, endSignLength: Int) = messageBits.subList(0, messageBits.size - endSignLength).joinToString("").chunked(8).map{it.toInt(2).toChar()}.joinToString("")

    private fun addEndingSign(array: ByteArray): ByteArray {
        return array.toMutableList().apply {
            add(0)
            add(0)
            add(3)
        }.toByteArray()
    }

    private fun endSignatureMatch(messageBits: List<Int>): Boolean {
        val signature = convertBytesToBits(addEndingSign(ByteArray(0)))
        val signatureLength = signature.size
        return messageBits.takeLast(signatureLength) == signature
    }

    private fun convertBytesToBits(bytesArray: ByteArray) = bytesArray.flatMap { it.toString(2).padStart(8,'0').chunked(1) }.map { it.toInt() }

    private fun  canHideMessage(image: BufferedImage, message: String) : Boolean {
        val maxImageSize = image.width * image.height //bits of blue channel
        val messageSize = convertMessageToBits(message).size //size in bits + endingSign in bits
        return  maxImageSize >= messageSize
    }

    private fun readImage(file: File) : BufferedImage {
        try {
            return ImageIO.read(file)
        }catch (e: Exception){
            throw Exception("The input image is not large enough to hold this message.")
        }
    }

    fun checkFileExist(path: String) = File(path).exists()
    fun checkFileFormatIsPNG(path: String) = File(path).extension == "png"
}