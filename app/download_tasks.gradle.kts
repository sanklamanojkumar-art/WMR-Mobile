import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

val assetDir = extra["ASSET_DIR"] as String
val modelFile = file("$assetDir/face_landmarker.task")
val modelUrl = "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task"
val expectedSha256 = "64184e229b263107bc2b804c6625db1341ff2bb731874b0bcc2fe6544e0bc9ff"

fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count <= 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

val downloadFaceModel = tasks.register("downloadFaceLandmarkerModel") {
    outputs.file(modelFile)
    doLast {
        modelFile.parentFile?.mkdirs()
        if (!modelFile.exists() || modelFile.sha256() != expectedSha256) {
            val temp = file("$modelFile.tmp")
            try {
                URI(modelUrl).toURL().openStream().use { Files.copy(it, temp.toPath(), StandardCopyOption.REPLACE_EXISTING) }
                val actual = temp.sha256()
                check(actual == expectedSha256) { "Face model SHA-256 mismatch: $actual" }
                Files.move(temp.toPath(), modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } finally { temp.delete() }
        }
    }
}

tasks.register("verifyFaceModel") {
    dependsOn(downloadFaceModel)
    doLast { check(modelFile.exists() && modelFile.length() > 0L && modelFile.sha256() == expectedSha256) }
}
tasks.named("preBuild") { dependsOn(downloadFaceModel) }
