package codes.reactor.network.internal.io.compression

import com.github.luben.zstd.Zstd
import codes.reactor.network.internal.NetworkInternalConnector
import io.netty.buffer.ByteBuf

import io.netty.buffer.ByteBufAllocator

object PacketCompression {

    fun compress(src: ByteBuf, dst: ByteBuf, dstOffset: Int, maxDstSize: Int): Int {
        val srcSize = src.readableBytes()
        if (srcSize == 0) return 0

        val srcNio = src.internalNioBuffer(src.readerIndex(), srcSize)
        val dstNio = dst.nioBuffer(dstOffset, maxDstSize)

        val compressedSize = Zstd.compress(dstNio, srcNio, NetworkInternalConnector.config.zstdCompressionLevel)

        if (Zstd.isError(compressedSize.toLong())) {
            throw ProtocolException("Zstd compression failed: ${Zstd.getErrorName(compressedSize.toLong())}")
        }

        return compressedSize
    }

    fun decompress(alloc: ByteBufAllocator, src: ByteBuf, srcOffset: Int, srcLength: Int, maxDecompressedSize: Int): ByteBuf {
        val srcNio = src.internalNioBuffer(srcOffset, srcLength)

        val decompressedSize = Zstd.getFrameContentSize(srcNio)
        validateContentSize(decompressedSize, maxDecompressedSize)

        val sizeInt = decompressedSize.toInt()

        val dst = alloc.buffer(sizeInt)

        return try {
            val dstNio = dst.nioBuffer(0, sizeInt)
            val result = Zstd.decompress(dstNio, srcNio)

            if (Zstd.isError(result.toLong())) {
                throw ProtocolException("Zstd decompression failed: ${Zstd.getErrorName(result.toLong())}")
            }

            dst.writerIndex(result)
        } catch (e: Exception) {
            dst.release()
            throw e
        }
    }

    private fun validateContentSize(size: Long, max: Int) {
        if (size == -1L) throw ProtocolException("Zstd frame content size is unknown")
        if (size == -2L) throw ProtocolException("Zstd frame content is invalid")
        if (size > max) throw ProtocolException("Decompressed size $size exceeds maximum $max")
    }
}
