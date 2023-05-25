package kr.goldenmine.inuminecraftlauncher.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileUtil {
}

fun deleteRecursive(file: File) {
    if (file.isDirectory) {
        for (child in file.listFiles()) {
            deleteRecursive(child)
        }
    }
    file.delete()
}

fun getFileSHA256(file: File): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val inputStream = file.inputStream()
    val buffer = ByteArray(8192)
    var read = inputStream.read(buffer, 0, buffer.size)

    while (read > 0) {
        messageDigest.update(buffer, 0, read)
        read = inputStream.read(buffer, 0, buffer.size)
    }

    val hashBytes = messageDigest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun getFileBase64(filePath: String): String {
    val fileBytes = Files.readAllBytes(Paths.get(filePath))
    val encodedBytes = Base64.getEncoder().encode(fileBytes)
    return String(encodedBytes)
}


fun getFileSHA1(file: File): String {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    val inputStream = file.inputStream()
    val buffer = ByteArray(8192)
    var read = inputStream.read(buffer, 0, buffer.size)

    while (read > 0) {
        messageDigest.update(buffer, 0, read)
        read = inputStream.read(buffer, 0, buffer.size)
    }

    val hashBytes = messageDigest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}


fun listFilesRecursively(dir: File): List<File> {
    val files = mutableListOf<File>()
    if(dir.exists()) {
        for (file in dir.listFiles()!!) {
            if (file.isDirectory) {
                files.addAll(listFilesRecursively(file))
            } else {
                files.add(file)
            }
        }
    }
    return files
}

fun getFileMD5(file: String): String {
    val md = MessageDigest.getInstance("MD5")
    val fis = FileInputStream(file)
    val buffer = ByteArray(8192)
    var read: Int = fis.read(buffer)

    while (read > 0) {
        md.update(buffer, 0, read)
        read = fis.read(buffer)
    }

    val md5 = md.digest()

    val sb = StringBuilder()
    for (i in md5.indices) {
        sb.append(Integer.toHexString(0xFF and md5[i].toInt()))
    }

    return sb.toString()
}

fun unzipJar(jarFile: File, destDir: File) {
    // Create output directory if it doesn't exist
    if (!destDir.exists()) {
        destDir.mkdirs()
    }

    // Open the JAR file as a ZipInputStream
    val zipStream = ZipInputStream(jarFile.inputStream())

    // Read each entry in the JAR file and extract it to the output directory
    var entry: ZipEntry? = zipStream.nextEntry
    while (entry != null) {
        val entryFile = File(destDir, entry.name)

        if (entry.isDirectory) {
            entryFile.mkdirs()
        } else {
            // Create parent directories if they don't exist
            if (!entryFile.parentFile.exists()) {
                entryFile.parentFile.mkdirs()
            }

            // Extract file contents
            val buffer = ByteArray(1024)
            var bytesRead = zipStream.read(buffer)
            val outStream = FileOutputStream(entryFile)
            while (bytesRead != -1) {
                outStream.write(buffer, 0, bytesRead)
                bytesRead = zipStream.read(buffer)
            }
            outStream.close()
        }

        entry = zipStream.nextEntry
    }

    zipStream.close()
}