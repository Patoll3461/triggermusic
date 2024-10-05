package net.tetragon.triggermusic;

import com.google.common.base.Optional;
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.TrialChamberData;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.structure.*;
import org.apache.commons.logging.Log;
import net.minecraft.world.gen.structure.StructureType;

import java.lang.reflect.AccessFlag;
import java.util.*;

public class PacketPosition {
    private BlockPos pos;
    private Identifier dim;
    private String name;
    private static Set<RegistryKey<Structure>> registryKeys = new HashSet<RegistryKey<Structure>>();
    public static Boolean healthWasUnderHalf = false;


    public PacketPosition(BlockPos pos, Identifier dim, String name/*, RegistryKey<World> registryKey*/){
        this.pos = pos;
        this.dim = dim;
        this.name = name;
        //this.registryKey = registryKey;
    }

    public static void write(PacketByteBuf buf, PacketPosition packetPosition){
        buf.writeBlockPos(packetPosition.pos);
        buf.writeIdentifier(packetPosition.dim);
        buf.writeString(packetPosition.name);
        //buf.writeRegistryKey(packetPosition.registryKey);
    }

    public static PacketPosition read(PacketByteBuf buf){
        return new PacketPosition(buf.readBlockPos(), buf.readIdentifier(), buf.readString()/*, buf.readRegistryKey(RegistryKey.ofRegistry(buf.readIdentifier()))*/);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, BlockPos pos, Identifier dim, String name) {
        //RegistryKey<World> registryKey = buf.readRegistryKey(RegistryKey.ofRegistry(buf.readIdentifier()));
        //TriggerMusic.LOGGER.info("Server");
        registryKeys.add(StructureKeys.VILLAGE_DESERT);
        registryKeys.add(StructureKeys.VILLAGE_PLAINS);
        registryKeys.add(StructureKeys.VILLAGE_SAVANNA);
        registryKeys.add(StructureKeys.VILLAGE_SNOWY);
        registryKeys.add(StructureKeys.VILLAGE_TAIGA);
        PacketPosition packetPosition = new PacketPosition(pos, dim, name);
        ServerWorld world = (ServerWorld) player.getWorld();
        PacketByteBuf bufsend = new PacketByteBuf(Unpooled.buffer());
        String struct = "";
        Boolean isRaid = false;
        Identifier identifier = Identifier.of("");
        int hostileCount = 0;
        for (Entity entity : world.getOtherEntities(player, new Box(player.getX() - 30, player.getY() - 30, player.getZ() - 30, player.getX() + 30, player.getY() + 30, player.getZ() + 30))) {
            //TriggerMusic.LOGGER.info(Registries.ENTITY_TYPE.getId(entity.getType()).toString());
            if (entity.isLiving()) {
                LivingEntity ent = (LivingEntity) entity;
                if (ent instanceof net.minecraft.entity.mob.HostileEntity) {
                    HostileEntity entity1 = (HostileEntity) ent;
                    if (entity1.getTarget() instanceof PlayerEntity) {
                        hostileCount++;
                    }
                }
            }
        }
        if (testForStructure(StructureKeys.ANCIENT_CITY, player, world)) {
            struct = "ancient_city";
        } else if (testForStructure(StructureKeys.BASTION_REMNANT, player, world)) {
            struct = "bastion";
        } else if (testForStructure(StructureKeys.END_CITY, player, world)) {
            struct = "end_city";
        } else if (testForStructure(StructureKeys.FORTRESS, player, world)) {
            struct = "fortress";
        } else if (testForStructure(StructureKeys.MANSION, player, world)) {
            struct = "mansion";
        } else if (testForStructure(StructureKeys.MONUMENT, player, world)) {
            struct = "monument";
        } else if (testForStructure(StructureKeys.PILLAGER_OUTPOST, player, world)) {
            struct = "outpost";
        } else if (testForStructure(StructureKeys.STRONGHOLD, player, world)) {
            struct = "stronghold";
        } else if (testForStructure(StructureKeys.TRAIL_RUINS, player, world)) {
            struct = "trail_ruins";
        } else if (testForStructure(StructureKeys.VILLAGE_PLAINS, player, world) || testForStructure(StructureKeys.VILLAGE_TAIGA, player, world)) {
            struct = "moderate_village";
        } else if (testForStructure(StructureKeys.VILLAGE_DESERT, player, world) || testForStructure(StructureKeys.VILLAGE_SAVANNA, player, world)) {
            struct = "hot_village";
        } else if (testForStructure(StructureKeys.VILLAGE_SNOWY, player, world)) {
            struct = "cold_village";
        } else if (testForStructure(StructureKeys.TRIAL_CHAMBERS, player, world)) {
            TriggerMusic.LOGGER.info(String.valueOf(hostileCount));
            if (hostileCount > 0) {
                struct = "trial_danger";
                if (player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(Registries.STATUS_EFFECT.get(Identifier.of("minecraft:trial_omen"))))) {
                    struct = "trial_omen";
                }
            } else {
                struct = "trial_chambers";
            }
        }

        if (!(world.getRegistryKey().getValue().getPath().equals(dim.getPath()))) return;
        if (world.getRaidAt(pos) != null) {
            if (world.getRaidAt(pos).isFinished()) {
                isRaid = false;
                //TriggerMusic.LOGGER.info("raid finished");
            } else {
                //TriggerMusic.LOGGER.info("raid not finished" + world.getRaidAt(pos).isActive());
                isRaid = world.getRaidAt(pos).isActive();
                //bufsend.writeBoolean(world.getRaidAt(pos).isActive());
            }
        } else {
            //TriggerMusic.LOGGER.info("false (no raid)");
            isRaid = false;
            //bufsend.writeBoolean(false);
        }

        for (Entity entity : world.getOtherEntities(player, new Box(player.getX() - 60, player.getY() - 300, player.getZ() - 60, player.getX() + 60, player.getY() + 300, player.getZ() + 60))) {
            if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:evoker")) {
                if (entity instanceof EvokerEntity) {
                    EvokerEntity evokerEntity = (EvokerEntity) entity;
                    if (evokerEntity.getTarget() == player && struct.equals("mansion")) {
                        identifier = Registries.ENTITY_TYPE.getId(entity.getType());
                    }
                }
            } else if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:warden")) {
                identifier = Registries.ENTITY_TYPE.getId(entity.getType());
            } else if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:pillager")) {
                if (entity instanceof PillagerEntity) {
                    PillagerEntity pillagerEntity = (PillagerEntity) entity;
                    if (pillagerEntity.isPatrolLeader() && pillagerEntity.getTarget() == player) {
                        identifier = Registries.ENTITY_TYPE.getId(entity.getType());
                    }
                }
            } else if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:sheep")) {
                if (entity instanceof SheepEntity) {
                    SheepEntity sheepEntity = (SheepEntity) entity;
                    if (sheepEntity.isBaby() && sheepEntity.getColor().equals(DyeColor.PINK)) {
                        identifier = Registries.ENTITY_TYPE.getId(entity.getType());
                    }
                }
            } else if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:wither")) {
                if (entity instanceof WitherEntity) {
                    WitherEntity witherEntity = (WitherEntity) entity;
                    //TriggerMusic.LOGGER.info(witherEntity.getInvulnerableTimer() + " inv timer" + healthWasUnderHalf + " healthunder");
                    if (witherEntity.getHealth() < witherEntity.getMaxHealth() / 2 && witherEntity.getInvulnerableTimer() < 70) {
                        identifier = Identifier.of("minecraft:wither2");
                        healthWasUnderHalf = true;
                    } else {
                        identifier = Identifier.of("minecraft:wither1");
                    }
                    if (healthWasUnderHalf) {
                        identifier = Identifier.of(("minecraft:wither2"));
                    }
                    if (witherEntity.isDead()) {
                        //TriggerMusic.LOGGER.info("dead");
                        //TriggerMusic.LOGGER.info("upps");
                        healthWasUnderHalf = false;
                    }
                }
            } else if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:villager")) {
                if (entity instanceof VillagerEntity) {
                    VillagerEntity villagerEntity = (VillagerEntity) entity;
                    if (villagerEntity.isReadyToBreed()) {
                        identifier = Registries.ENTITY_TYPE.getId(entity.getType());
                    }
                }
            }//TriggerMusic.LOGGER.info(foundWither + " found");
        }

        for (Entity entity : world.getOtherEntities(player, new Box(player.getX() - 200, player.getY() - 300, player.getZ() - 200, player.getX() + 200, player.getY() + 300, player.getZ() + 200))) {
            if (Registries.ENTITY_TYPE.getId(entity.getType()).toString().equals("minecraft:ender_dragon")) {
                identifier = Registries.ENTITY_TYPE.getId(entity.getType());
            }
        }

        if (!identifier.toString().equals("minecraft:wither1") && !identifier.toString().equals("minecraft:wither2")) {
            healthWasUnderHalf = false;
            //TriggerMusic.LOGGER.info("hi");
        }
        /*bufsend.writeString(struct);
        bufsend.writeBoolean(isRaid);
        bufsend.writeInt(hostileCount);
        bufsend.writeIdentifier(identifier);*/
        ServerPlayNetworking.send(server.getPlayerManager().getPlayer(UUID.fromString(name)), new TriggerMusicClient.BlockHighlightPayload(struct, isRaid, hostileCount, identifier));
    }

    public static Boolean testForStructure(RegistryKey<Structure> structureRegistryKey, ServerPlayerEntity player, ServerWorld world) {
        return  LocationPredicate.Builder.createStructure(world.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(world.getRegistryManager().get(RegistryKeys.STRUCTURE).get(structureRegistryKey))).build().test((ServerWorld) player.getWorld(), player.getX(), player.getY(), player.getZ());
    }
}
