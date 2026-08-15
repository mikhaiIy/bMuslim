package com.bmuslim.app.research

import android.content.res.AssetManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.charset.CodingErrorAction

internal class ResearchCatalogRepository(
    private val parser: ResearchCatalogParser = ResearchCatalogParser(),
) {
    fun load(assets: AssetManager): ResearchCatalogResult = try {
        assets.open(ASSET_PATH).use(::load)
    } catch (_: IOException) {
        ResearchCatalogResult.Rejected
    } catch (_: RuntimeException) {
        ResearchCatalogResult.Rejected
    }

    fun load(input: InputStream): ResearchCatalogResult {
        return try {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_BYTES) return ResearchCatalogResult.Rejected
                output.write(buffer, 0, read)
            }
            val text = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(output.toByteArray()))
                .toString()
            parser.parse(text)
        } catch (_: IOException) {
            ResearchCatalogResult.Rejected
        } catch (_: RuntimeException) {
            ResearchCatalogResult.Rejected
        }
    }

    companion object {
        internal const val ASSET_PATH = "amuslim/starter-v0.2/catalog.draft.json"
        internal const val MAX_BYTES = 512 * 1024
    }
}
