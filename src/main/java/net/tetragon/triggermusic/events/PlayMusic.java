package net.tetragon.triggermusic.events;

import net.minecraft.network.packet.CustomPayload;
import net.tetragon.triggermusic.TriggerMusic;
import dev.architectury.event.events.common.TickEvent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayMusic implements TickEvent.Player {
    //static Identifier PACKET_POSITION = new Identifier("trigger-music", "packet-position");
    @Override
    public void tick(PlayerEntity instance) {
        if (!instance.getWorld().isClient) return;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        //buf.writeBlockPos(minecraftClient.player.getBlockPos());
        //buf.writeIdentifier(minecraftClient.world.getRegistryKey().getValue());
        //buf.writeString(instance.getEntityName());
        TriggerMusic.check();
        ClientPlayNetworking.send(new TriggerMusic.Packet(minecraftClient.player.getBlockPos(), minecraftClient.world.getRegistryKey().getValue(), instance.getUuidAsString()));
    }
}
