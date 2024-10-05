package net.tetragon.triggermusic.events;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.tetragon.triggermusic.PacketPosition;
import net.tetragon.triggermusic.TriggerMusic;
import net.tetragon.triggermusic.soundevents.ModSoundEvents;
import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import net.minecraft.world.gen.structure.StructureType;

import java.awt.*;
import java.util.logging.Logger;

public class ObtainAllStructuresEvent implements LifecycleEvent.ServerLevelState {
    private boolean started = false;

    @Override
    public void act(ServerWorld world) {
        if (started) return;
        started = true;
        //ModSoundEvents.registerSoundEvents();
        //ServerPlayNetworking.registerGlobalReceiver(new Identifier("trigger-music", "packet-position"), PacketPosition::handle);
    }
}
