package com.jheng.bay.extensions

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.awt.Desktop
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Paths

internal fun ByteArray.write_to_file(path: String) {
    FileOutputStream(path).use {
        it.write(this)
    }
}

internal fun ByteArray.write_and_open(path: String) {
    write_to_file(path)
    open(path)
}

internal fun open(path: String) {
    val desktop = Desktop.getDesktop()
    val absolutePath = Paths.get(path).toAbsolutePath()
    val uri = URI("file://$absolutePath")
    try {
        desktop.browse(uri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ByteArrayResource.to_response_entity(
        file_name: String,
        media_type: String
): ResponseEntity<ByteArrayResource> {
    val header = HttpHeaders().apply {
        contentDisposition = ContentDisposition.parse("attachment; filename=$file_name")
    }
    return ResponseEntity.ok()
            .headers(header)
            .contentLength(this.contentLength())
            .contentType(MediaType.valueOf(media_type))
            .body(this)
}
