package net.tetragon.triggermusic.check;

import net.tetragon.triggermusic.TriggerMusic;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StructureCheck {

    public static String getStructure() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null){
            return "no world";
        }
        //Map<Structure, LongSet> structureReferences = Objects.requireNonNull(client.world.getServer()).getWorld(client.world.getRegistryKey()).getStructureAccessor().getStructureReferences(client.player.getBlockPos());
        //Map<Structure, LongSet> structureReferences = client.world.getWorldChunk(client.player.getBlockPos()).getStructureReferences();
        //TriggerMusic.LOGGER.info(MinecraftClient.getInstance().world.getWorldChunk(client.player.getBlockPos()).getPos().getCenterAtY(64).toString());
        //TriggerMusic.LOGGER.info(structureReferences.toString());
        /*for (Map.Entry<Structure, LongSet> entry : structureReferences.entrySet()) {
            Structure structure = entry.getKey();
            return structure.getType().toString();
        }*/
        //return "no structure";
        return "j";
    }

    public static String getBiome() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null){
            return "no world";
        }
        return client.world.getBiome(client.player.getBlockPos()).getKey().get().getValue().toString();
    }
}
