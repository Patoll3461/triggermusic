package net.tetragon.triggermusic;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.tetragon.triggermusic.soundevents.ModSoundEvents;
import dev.architectury.platform.Mod;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class PacketStructure {
    private String structure;
    public static String previousStruct = "";
    public static String previous = "";
    public static Boolean wasRaid = false;
    public static int hostileCount = 0;
    public static Identifier identifier = Identifier.of("");
    private static int ticksLeft = 6001;

    public PacketStructure(String structure){
        this.structure = structure;
    }

    public static void write(PacketByteBuf buf, PacketStructure packetStructure){
        buf.writeString(packetStructure.structure);
    }

    public static PacketStructure read(PacketByteBuf buf){
        return new PacketStructure(buf.readString());
    }

    public static void receive(MinecraftClient client, String structure, Boolean isRaid, Integer hostileCount, Identifier identifier) {
        //TriggerMusic.LOGGER.info("Client");
        PacketStructure.identifier = identifier;
        PacketStructure.hostileCount = hostileCount;
        if (ticksLeft < 6001)  {
            ticksLeft--;
            //TriggerMusic.LOGGER.info(ticksLeft + " ticksleft struct");
        }
        if (ticksLeft <= 0) {
            previous = "";
            previousStruct = "";
            ticksLeft = 6001;
        }
        //TriggerMusic.LOGGER.info("client side");
        /*String structure = buf.readString();
        Boolean isRaid = buf.readBoolean();
        hostileCount = buf.readInt();
        identifier = buf.readIdentifier();*/

        //TriggerMusic.LOGGER.info(structure + " structure " + previous + " previous " + previousStruct + " previousStruct");
        //TriggerMusic.LOGGER.info(String.valueOf(hostileCount) + " hostileCount");
        //TriggerMusic.LOGGER.info(structure);
        PacketStructure packetStructure = new PacketStructure(structure);
        client.execute(() -> {
            //TriggerMusic.LOGGER.info(String.valueOf(ticksLeft));
            long dayCount = MinecraftClient.getInstance().world.getTimeOfDay() / 24000L;
            long dayTime = MinecraftClient.getInstance().world.getTimeOfDay() - (dayCount * 24000L);
            if (wasRaid != isRaid){
                if (wasRaid && !isRaid) {
                    client.getSoundManager().stopAll();
                    //TriggerMusic.LOGGER.info("stopped");
                }
                wasRaid = !wasRaid;
                //TriggerMusic.LOGGER.info(wasRaid + " wasRaid");
            } else {
                if (isRaid) {
                    //TriggerMusic.LOGGER.info("return");
                    MinecraftClient.getInstance().getSoundManager().stopSounds(MinecraftClient.getInstance().getMusicType().getSound().getKey().get().getValue(), SoundCategory.MUSIC);
                    return;
                }
            }
            if (isRaid) {
                client.getSoundManager().stopAll();
                MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.MONSTER, SoundCategory.MUSIC, 1f, 1);
                //TriggerMusic.LOGGER.info("raid");
                wasRaid = true;
                //TriggerMusic.LOGGER.info(isRaid + "isRaid" + wasRaid + "wasRaid");
                return;
            } else {
                //TriggerMusic.LOGGER.info("no Raid");
                wasRaid = false;
            }
            if (structure.equals("") && !previous.equals("")) {
                if (ticksLeft > 6000) {
                    ticksLeft = 6000;
                }
                previous = "";
                //TriggerMusic.isInStructure = false;
                return;
            } else if (!structure.equals("")) {
                //TriggerMusic.isInStructure = true;
                if (ticksLeft > 6000) {
                    ticksLeft = 6000;
                }
                //return;
            }

            // Check if the structure has changed
            if (structure.equals(previous) || previousStruct.equals(structure)) {
                return;
            }

            // Stop all sounds
            client.getSoundManager().stopAll();
            Random rnd = new Random();
            TriggerMusic.ticksLeft = rnd.nextInt(6000, 10000);
            TriggerMusic.ticksPassed = 0;
            ticksLeft = 6000;
            //TriggerMusic.LOGGER.info(structure);

            // Play the appropriate sound based on the structure
            if (structure.equals("stronghold")) {
                int temp = (Math.random() <= 0.5) ? 1 : 2;
                if ( temp == 1) {
                    client.player.playSoundToPlayer(ModSoundEvents.CASTLE, SoundCategory.MUSIC, 0.31f, 1f);
                } else {
                    client.player.playSoundToPlayer(ModSoundEvents.CORE, SoundCategory.MUSIC, 0.31f, 1f);
                }
            } else if (structure.equals("end_city")) {
                int temp = (Math.random() <= 0.5) ? 1 : 2;
                if (temp == 1) {
                    client.player.playSoundToPlayer(ModSoundEvents.CASTLE_SKY, SoundCategory.MUSIC, 0.6f, 1f);
                } else {
                    client.player.playSoundToPlayer(ModSoundEvents.LEYNDELL, SoundCategory.MUSIC, 0.6f, 1f);
                }
            } else if (structure.equals("fortress") || structure.equals("bastion")) {
                client.player.playSoundToPlayer(ModSoundEvents.MEDIUM, SoundCategory.MUSIC, 0.31f, 1f);
            } else if (structure.equals("monument")) {
                client.player.playSoundToPlayer(ModSoundEvents.ZORA_SLUDGE, SoundCategory.MUSIC, 0.31f, 1f);
            } else if (structure.equals("mansion") || structure.equals("outpost")) {
                int temp = (Math.random() <= 0.5) ? 1 : 2;
                if (temp == 1) {
                    client.player.playSoundToPlayer(ModSoundEvents.YIGA, SoundCategory.MUSIC, 0.31f, 1f);
                } else {
                    client.player.playSoundToPlayer(ModSoundEvents.STORMVEIL, SoundCategory.MUSIC, 0.31f, 1f);
                }
            } else if (structure.equals("moderate_village")) {
                if (dayTime > 0 && dayTime < 11000) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(4) + 1;

                    switch (randomNumber) {
                        case 1:
                            client.player.playSoundToPlayer(ModSoundEvents.HATENO_DAY, SoundCategory.MUSIC, 0.6f, 1f);
                            break;
                        case 2:
                            client.player.playSoundToPlayer(ModSoundEvents.TARREY, SoundCategory.MUSIC, 0.6f, 1f);
                            break;
                        case 3:
                            client.player.playSoundToPlayer(ModSoundEvents.HOBBTIS, SoundCategory.MUSIC, 0.6f, 1f);
                            break;
                        case 4:
                            client.player.playSoundToPlayer(ModSoundEvents.ROUNDTABLE, SoundCategory.MUSIC, 0.6f, 1f);
                            break;
                    }
                } else if (dayTime > 12000 && dayTime < 23000) {
                    client.player.playSoundToPlayer(ModSoundEvents.HATENO_NIGHT, SoundCategory.MUSIC, 0.31f, 1f);
                }
            } else if (structure.equals("hot_village")) {
                if (dayTime > 0 && dayTime < 11000) {
                    client.player.playSoundToPlayer(ModSoundEvents.GERUDO_DAY, SoundCategory.MUSIC, 0.31f, 1f);
                } else if (dayTime > 12000 && dayTime < 23000) {
                    client.player.playSoundToPlayer(ModSoundEvents.GERUDO_NIGHT, SoundCategory.MUSIC, 0.31f, 1f);
                }
            } else if (structure.equals("cold_village")) {
                int temp = (Math.random() <= 0.5) ? 1 : 2;
                if ( temp == 1) {
                    client.player.playSoundToPlayer(ModSoundEvents.RITO_FROZEN, SoundCategory.MUSIC, 0.31f, 1f);
                } else {
                    client.player.playSoundToPlayer(ModSoundEvents.SNOWDIN, SoundCategory.MUSIC, 0.31f, 1f);
                }
            } else if (structure.equals("trail_ruins")) {
                client.player.playSoundToPlayer(ModSoundEvents.RUINSU, SoundCategory.MUSIC, 1f, 1f);
            } else if (structure.equals("ancient_city")) {
                client.player.playSoundToPlayer(ModSoundEvents.GANON_CASTLE, SoundCategory.MUSIC, 0.31f, 1f);
            } else if (structure.equals("trial_chambers"))  {
                client.player.playSoundToPlayer(ModSoundEvents.MONSTER_AMBIENCE, SoundCategory.MUSIC, 0.31f, 1f);
            } else if (structure.equals("trial_danger")) {
                ArrayList<SoundEvent> soundEvents = new ArrayList<>();
                soundEvents.add(ModSoundEvents.BOSS_BOKO);
                soundEvents.add(ModSoundEvents.MONSTER_FORCES);
                soundEvents.add(ModSoundEvents.FOE);
                Collections.shuffle(soundEvents);
                MinecraftClient.getInstance().player.playSoundToPlayer(soundEvents.get(0), SoundCategory.MUSIC, 0.7f, 1f);
            } else if (structure.equals("trial_omen")) {
                ArrayList<SoundEvent> soundEvents = new ArrayList<>();
                soundEvents.add(ModSoundEvents.AVATAR);
                soundEvents.add(ModSoundEvents.WARRIORS);
                soundEvents.add(ModSoundEvents.GODRICK);
                soundEvents.add(ModSoundEvents.MARGIT);
                //soundEvents.add(ModSoundEvents.SENTINEL);
                Collections.shuffle(soundEvents);
                MinecraftClient.getInstance().player.playSoundToPlayer(soundEvents.get(0), SoundCategory.MUSIC, 0.7f, 1f);
            }
            previous = structure;
            previousStruct = structure;
        });
    }
}
