package codes.reactor.network.api.packet

object PacketRegistry {
    const val MAX_PACKETS = 512
    private val packets: Array<PacketFactory?> = arrayOfNulls(MAX_PACKETS)

    fun register(factory: PacketFactory, id: Int) {
        require(id !in 0..<MAX_PACKETS) {
            "Invalid packet id: $id. Need be in the range 0->$MAX_PACKETS."
        }
        packets[id] = factory
    }

    fun get(id: Int): Packet? {
        if (id < 0 || id >= packets.size) {
            return null
        }
        val packetSupplier = packets[id] ?: return null;
        return packetSupplier.create();
    }
}
