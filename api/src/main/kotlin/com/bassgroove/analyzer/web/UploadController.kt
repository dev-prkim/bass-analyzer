package com.bassgroove.analyzer.web

import com.bassgroove.analyzer.bus.SqsBus
import com.bassgroove.analyzer.domain.*
import com.bassgroove.analyzer.infra.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.PutObjectArgs
import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api")
class UploadController(
    private val recRepo: RecordingRepository,
    private val jobRepo: AnalysisJobRepository,
    private val bus: SqsBus,
    private val minio: MinioClient,
    @Value("\${app.storage.bucket}") private val bucket: String,
    @Value("\${app.publicUploadBase}") private val publicBase: String,
    @Value("\${app.queues.request}") private val requestQueue: String
) {
    private val om = jacksonObjectMapper()

    @PostMapping("/recordings", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart("file") file: MultipartFile,
               @RequestParam(required = false) bpm: Double?): Map<String, Any> {

        ensureBucket()

        val key = "${UUID.randomUUID()}-${file.originalFilename}"
        minio.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(key)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType ?: "application/octet-stream")
                .build()
        )
        val uri = "${publicBase.trimEnd('/')}/$key"

        val rec = recRepo.save(Recording(original = file.originalFilename ?: "upload", uri = uri))
        val job = jobRepo.save(AnalysisJob(recording = rec, bpmHint = bpm))

        val payload = mapOf(
            "schemaVersion" to 1,
            "jobId" to job.id.toString(),
            "recordingId" to rec.id.toString(),
            "media" to mapOf("type" to "http", "uri" to uri),
            "hints" to mapOf("bpm" to bpm, "timeSig" to "4/4", "subdivision" to 2),
            "createdAt" to Instant.now().toString()
        )
        bus.send(requestQueue, om.writeValueAsString(payload))

        return mapOf("recordingId" to rec.id, "jobId" to job.id, "status" to job.status)
    }

    private fun ensureBucket() {
        val exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
    }
}
