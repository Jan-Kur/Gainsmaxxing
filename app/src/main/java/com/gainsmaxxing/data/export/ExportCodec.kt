package com.gainsmaxxing.data.export

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

object ExportCodec {
    const val FORMAT_VERSION = 1
    const val MIME_TYPE = "application/json"
    const val FILE_EXTENSION = "json"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(GainsmaxxingExport::class.java)

    fun encode(export: GainsmaxxingExport): String =
        adapter.indent("  ").toJson(export)

    fun decode(json: String): GainsmaxxingExport =
        adapter.fromJson(json) ?: error("Export file is empty or invalid")

    fun validate(export: GainsmaxxingExport) {
        require(export.formatVersion == FORMAT_VERSION) {
            "Unsupported backup version ${export.formatVersion}"
        }
    }

    fun buildFileName(instant: Instant = Instant.now()): String {
        val date = instant.atZone(java.time.ZoneOffset.UTC).toLocalDate()
        return "gainsmaxxing-backup-$date.$FILE_EXTENSION"
    }
}
