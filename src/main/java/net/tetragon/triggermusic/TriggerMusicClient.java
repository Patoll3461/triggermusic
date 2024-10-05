package net.tetragon.triggermusic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TriggerMusicClient implements ClientModInitializer {

    public record BlockHighlightPayload(String structure, Boolean isRaid, Integer hostileCount, Identifier identifier) implements CustomPayload {
        public static final CustomPayload.Id<BlockHighlightPayload> ID = new CustomPayload.Id<>(Identifier.of("triggermusic:packet-structure"));
        public static final PacketCodec<RegistryByteBuf, BlockHighlightPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, BlockHighlightPayload::structure,
                PacketCodecs.BOOL, BlockHighlightPayload::isRaid,
                PacketCodecs.INTEGER, BlockHighlightPayload::hostileCount,
                Identifier.PACKET_CODEC, BlockHighlightPayload::identifier,
                BlockHighlightPayload::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Override
    public void onInitializeClient() {
        // In your client-only initializer method
        ClientPlayNetworking.registerGlobalReceiver(BlockHighlightPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                PacketStructure.receive(MinecraftClient.getInstance(), payload.structure, payload.isRaid, payload.hostileCount, payload.identifier);
            });
        });
    }
}
