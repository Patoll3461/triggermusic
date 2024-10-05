package net.tetragon.triggermusic;

import dev.architectury.platform.Mod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.tetragon.triggermusic.check.StructureCheck;
import net.tetragon.triggermusic.events.ObtainAllStructuresEvent;
import net.tetragon.triggermusic.events.PlayMusic;
import net.tetragon.triggermusic.soundevents.ModSoundEvents;

import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.data.DataGenerator;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;

import java.util.*;

public class TriggerMusic implements ModInitializer {

	public static final String MOD_ID = "triggermusic";
	public static Boolean isPlaying = true;
	public static MusicSound prevMusic;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Boolean isInStructure = false;

	public static int ticksLeft = 1000;
	public static int ticksPassed = 5502;
	static String prevEntity = "";

	private static final String[] hot = {"minecraft:badlands", "minecraft:desert", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "terralith:ancient_sands", "terralith:ashen_savanna", "terralith:bryce_canyon", "terralith:desert_canyon", "terralith:desert_spires", "terralith:hot_shrubland", "terralith:lush_desert", "terralith:painted_mountains", "terralith:red_oasis", "terralith:sandstone_valley", "terralith:white_mesa", "terralith:cave/desert_caves", "terralith:warped_mesa", "terralith:desert_oasis"};
	private static final String[] hotLike = {"minecraft:savanna", "minecraft:savanna_plateau", "terralith:arid_highlands", "terralith:fractured_savanna", "terralith:savanna_badlands", "terralith:savanna_slopes", "terralith:shrubland"};
	private static final String[] tropical = {"minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:mangrove_swamp", "minecraft:sparse_jungle", "minecraft:stony_peaks", "terralith:amethyst_rainforest", "terralith:amethyst_canyon", "terralith:jungle_mountains", "terralith:orchid_swamp", "terralith:rocky_jungle", "terralith:tropical_jungle", "terralith:cave/underground_jungle"};
	private static final String[] nether = {"minecraft:basalt_deltas", "minecraft:crimson_forest", "minecraft:nether_wastes", "minecraft:soul_sand_valley", "minecraft:warped_forest"};
	private static final String[] water = {"minecraft:beach", "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:deep_ocean", "minecraft:lukewarm_ocean", "minecraft:ocean", "minecraft:river", "minecraft:warm_ocean", "terralith:warm_river", "terralith:alpha_islands", "terralith:alpha_islands_winter"};
	private static final String[] adventure = {"minecraft:stony_peaks", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills", "terralith:alpine_highlands", "terralith:forested_highlands", "terralith:granite_cliffs", "terralith:gravel_cliffs", "terralith:haze_mountain", "terralith:highlands", "terralith:stony_spires", "terralith:white_cliffs", "terralith:windswept_spires", "terralith:yellowstone", "terralith:yosemite_cliffs"};
	private static final String[] moderate = {"minecraft:beach", "minecraft:birch_forest", "minecraft:flower_forest", "minecraft:forest", "minecraft:meadow", "minecraft:old_growth_birch_forest", "minecraft:plains", "minecraft:river", "minecraft:stony_shore", "minecraft:sunflower_plains", "terralith:blooming_valley", "terralith:brushland", "terralith:highlands", "terralith:lavender_forest", "terralith:lavender_valley", "terralith:moonlight_grove", "terralith:moonlight_valley", "terralith:steppe", "terralith:temperate_highlands", "terralith:valley_clearing", "terralith:white_cliffs", "terralith:yellowstone"};
	private static final String[] cold = {"minecraft:deep_frozen_ocean", "minecraft:frozen_ocean", "minecraft:frozen_peaks", "minecraft:frozen_river", "minecraft:grove", "minecraft:ice_spikes", "minecraft:jagged_peaks", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:snowy_slopes", "minecraft:snowy_taiga", "terralith:alpine_grove", "terralith:emerald_peaks", "terralith:frozen_cliffs", "terralith:glacial_chasm", "terralith:ice_marsh", "terralith:rocky_mountains", "terralith:scarlet_mountains", "terralith:siberian_grove", "terralith:snowy_badlands", "snowy_maple_forest", "terralith:snowy_shield", "terralith:wintry_forest", "terralith:wintry_lowlands", "terralith:cave/ice_caves", "terralith:cave/frostfire_caves"};
	private static final String[] boreal = {"minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga", "minecraft:taiga", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills", "terralith:alpine_highlands", "terralith:birch_taiga", "terralith:cold_shrubland", "terralith:forested_highlands", "terralith:gravel_desert", "terralith:lush_valley", "terralith:rocky_shrubland", "terralith:shield_clearing", "terralith:shield", "terralith:siberian_taiga", "terralith:yosemite_lowlands"};
	private static final String[] mountain = {"minecraft:medaow", "minecraft:stony_peaks", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills", "minecraft:wooded_badlands", "terralith:alpine_highlands", "terralith:forested_highlands", "terralith:arid_highlands", "terralith:blooming_plateau", "terralith:caldera", "terralith:granite_cliffs", "terralith:gravel_cliffs", "terralith:haze_mountain", "terralith:highlands", "terralith:savanna_slopes", "terralith:painted_mountains", "terralith:stony_spires", "terralith:windswept_spires", "terralith:yosemite_cliffs"};
	private static final String[] underground = {"minecraft:dripstone_cave", "minecraft:lush_caves", "terralith:cave/andesite_cave", "terralith:cave/diorite_cave", "terralith:cave/granite_cave", "terralith:cave/deep_caves", "terralith:cave/mantle_caves", "terralith:cave/tuff_caves", "terralith:cave/fungal_caves"};
	private static final String[] end = {"minecraft:end_barrens", "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:small_end_island", "minecraft:the_end", "minecraft:the_void", "nullscape:void_barrens", "nullscape:crystal_peaks", "nullscape:shadwowlands"};
	private static final String[] volcano = {"terralith:basalt_cliffs", "terralith:volcanic_crater", "terralith:volcanic_peaks", "terralith:cave/thermal_caves"};
	private static final String[] sakura = {"minecraft:cherry_grove", "terralith:sakura_grove", "terralith:sakura_valley", "terralith:snowy_cherry_grove", "terralith:cave/crystal_caves"};
	private static final String[] sky = {"terralith:skylands", "terralith:skylands_autumn", "terralith:skylands_spring", "terralith:skylands_summer", "terralith:skylands_winter"};
	//private static final String[] VANILLA_IDS = {".menu", ".game", ".overworld", ".end", ".nether.basalt_deltas", ".water", ".creative", ".dragon", ".nether.crimson_forest", ".nether.nether_wastes", ".nether.soul_sand_valley", ".nether.warped_forest", ".overworld.cherry_grove", ".overworld.deep_dark", ".overworld.desert", ".overworld.dripstone_caves", ".overworld.flower_forest", ".overworld.forest", ".overworld.frozen_peaks", ".overworld.grove", ".overworld.jagged_peaks", ".overworld.jungle", ".overworld.lush_caves", ".overworld.meadow", ".overworld.old_growth_taiga", ".overworld.snowy_slopes", ".overworld.sparse_jungle", ".overworld.stony_peaks", ".overworld.swamp", ".underwater", "/game/a_familiar_room", "/game/an_ordinary_day", "/game/ancestry", "/game/bromeliad", "/game/calm1", "/game/calm2", "/game/calm3", "/game/comforting_memories", "/game/creative/creative1", "/game/creative/creative2", "/game/creative/creative3", "/game/creative/creative4", "/game/creative/creative5", "/game/creative/creative6", "/game/crescent_dunes", "/game/echo_in_the_wind", "/game/end/boss", "/game/end/credits", "/game/end/end", "/game/hal1", "/game/hal2", "/game/hal3", "/game/hal4", "/game/infinite_amethyst", "/game/left_to_bloom", "/game/nether/crimson_forest/chrysopoeia", "/game/nether/nether1", "/game/nether/nether2", "/game/nether/nether3", "/game/nether/nether4", "/game/nether/nether_wastes/rubedo", "/game/soulsand_valley/so_below", "/game/nuance1", "/game/nuance2", "/game/one_more_day", "/game/piano1", "/game/piano2", "/game/piano3", "/game/stand_tall", "/game/swamp/aerie", "/game/swamp/firebugs", "/game/swamp/labyrinthine", "/game/water/axolotl", "/game/water/dragon_fish", "/game/water/shuniji", "/game/wending", "/menu/menu1", "/menu/menu2", "/menu/menu3", "/menu/menu4"};


	/*public record PacketPosition(BlockPos blockPos, Identifier identifier, String string) implements CustomPayload {
		public static final CustomPayload.Id<PacketPosition> ID = new CustomPayload.Id<>(Identifier.of("triggermusic:packet-position"));
		public static final PacketCodec<RegistryByteBuf, PacketPosition> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, PacketPosition::blockPos,
				Identifier.PACKET_CODEC, PacketPosition::identifier,
				PacketCodecs.STRING, PacketPosition::string,
				PacketPosition::new);
		// should you need to send more data, add the appropriate record parameters and change your codec:
		// public static final PacketCodec<RegistryByteBuf, BlockHighlightPayload> CODEC = PacketCodec.tuple(
		//         BlockPos.PACKET_CODEC, BlockHighlightPayload::blockPos,
		//         PacketCodecs.INTEGER, BlockHighlightPayload::myInt,
		//         Uuids.PACKET_CODEC, BlockHighlightPayload::myUuid,
		//         BlockHighlightPayload::new
		// );
		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}*/

	public record Packet(BlockPos blockPos, Identifier identifier, String string) implements CustomPayload {
		public static final CustomPayload.Id<Packet> ID = new Id<>(Identifier.of("triggermusic:packet"));
		public static final PacketCodec<RegistryByteBuf, Packet> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, Packet::blockPos,
				Identifier.PACKET_CODEC, Packet::identifier,
				PacketCodecs.STRING, Packet::string,
				Packet::new
		);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	@Override
	public void onInitialize() {
		// In your common initializer method
		PayloadTypeRegistry.playS2C().register(TriggerMusicClient.BlockHighlightPayload.ID, TriggerMusicClient.BlockHighlightPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(Packet.ID, Packet.CODEC);
		/*ServerPlayNetworking.registerGlobalReceiver(PacketPosition.ID, (payload, context) -> {
			context.server().execute(() -> {
				//ClientBlockHighlighting.highlightBlock(client, payload.blockPos());
				net.tetragon.triggermusic.PacketPosition.handle(context.server(), context.server().getPlayerManager().getPlayer(payload.string), payload.blockPos, payload.identifier, payload.string);
			});
		});*/
		ServerPlayNetworking.registerGlobalReceiver(Packet.ID, (payload, context) -> {
			context.server().execute(() -> {
				PacketPosition.handle(context.server(), context.server().getPlayerManager().getPlayer(UUID.fromString(payload.string)), payload.blockPos, payload.identifier, payload.string);
			});
		});
		LifecycleEvent.SERVER_LEVEL_LOAD.register(new ObtainAllStructuresEvent());
		TickEvent.PLAYER_POST.register(new PlayMusic());
		//Exec every tick
		//ClientTickEvents.START_CLIENT_TICK.register(client -> {

	}    //}

	public static void check() {
		if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
			//isPlaying = false;
			return;
		}
		ticksLeft--;
		ticksPassed++;
		//LOGGER.info(String.valueOf(ticksLeft) + " " + String.valueOf(ticksPassed) + " ticksLeft and ticks passed");
		//LOGGER.info(prevEntity + " prevEntity");
		//LOGGER.info(StructureCheck.getStructure());
		//If a song from the mod is playing and a sound from mc starts stop the mc song
		//LOGGER.info(String.valueOf(PacketStructure.wasRaid) + " wasRaid");
		if (PacketStructure.wasRaid) return;
		if (!PacketStructure.identifier.toString().equals("minecraft:")) {
			//TriggerMusic.LOGGER.info(PacketStructure.identifier.toString());
			MinecraftClient.getInstance().getSoundManager().stopSounds(MinecraftClient.getInstance().getMusicType().getSound().getKey().get().getValue(), SoundCategory.MUSIC);
			if (prevEntity.equals(PacketStructure.identifier.toString())) {
				return;
			}
			MinecraftClient.getInstance().getSoundManager().stopAll();
			if (PacketStructure.identifier.toString().equals("minecraft:warden")) {
				MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.GLOOM, SoundCategory.MUSIC, 1f, 1);
				prevEntity = "minecraft:warden";
			} else if (PacketStructure.identifier.toString().equals("minecraft:wither1")) {
				if (Arrays.stream(cold).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.COLGERA1, SoundCategory.MUSIC, 0.7f, 1);
				} else if (Arrays.stream(nether).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.GOHMA, SoundCategory.MUSIC, 0.7f, 1);
				} else if (Arrays.stream(end).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.GANON2, SoundCategory.MUSIC, 0.7f, 1);
				} else if (Arrays.stream(hot).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.NABORIS, SoundCategory.MUSIC, 0.7f, 1);
				} else {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.CALAMITY1, SoundCategory.MUSIC, 0.7f, 1);
				}
				prevEntity = "minecraft:wither1";
			} else if (PacketStructure.identifier.toString().equals("minecraft:wither2")) {
				if (Arrays.stream(cold).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.COLGERA2, SoundCategory.MUSIC, 0.7f, 1);
				} else if (Arrays.stream(nether).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.GOHMA, SoundCategory.MUSIC, 0.7f, 1);
				} else if (Arrays.stream(end).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.GANON3, SoundCategory.MUSIC, 1f, 1);
				} else if (Arrays.stream(hot).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.MOLGERA, SoundCategory.MUSIC, 1f, 1);
				} else {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.CALAMITY2, SoundCategory.MUSIC, 0.7f, 1);
				}
				prevEntity = "minecraft:wither2";
			} else if (PacketStructure.identifier.toString().equals("minecraft:ender_dragon")) {
				ArrayList<SoundEvent> soundEvents = new ArrayList<>();
				soundEvents.add(ModSoundEvents.DRAGON);
				soundEvents.add(ModSoundEvents.BEAST);
				soundEvents.add(ModSoundEvents.FINAL_BATTLE);
				Collections.shuffle(soundEvents);
				MinecraftClient.getInstance().player.playSoundToPlayer(soundEvents.get(0), SoundCategory.MUSIC, 0.7f, 1f);
				prevEntity = "minecraft:ender_dragon";
			} else if (PacketStructure.identifier.toString().equals("minecraft:evoker")) {
				MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.KOHGA, SoundCategory.MUSIC, 0.5f, 1f);
				prevEntity = "minecraft:evoker";
			} else if (PacketStructure.identifier.toString().equals("minecraft:pillager") && !PacketStructure.wasRaid) {
				int temp = (Math.random() <= 0.5) ? 1 : 2;
				if (temp == 1) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.STRONG_TOTK, SoundCategory.MUSIC, 0.5f, 1f);
				} else {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.STRONG_BOTW, SoundCategory.MUSIC, 0.5f, 1f);
				}
				prevEntity = "minecraft:pillager";
			} else if (PacketStructure.identifier.toString().equals("minecraft:sheep")) {
				MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.HAMSTER, SoundCategory.MUSIC, 0.5f, 1f);
				prevEntity = "minecraft:sheep";
			} else if (PacketStructure.identifier.toString().equals("minecraft:enderman")) {
				if (!Arrays.stream(end).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.WEATHERTOP, SoundCategory.MUSIC, 1f, 1f);
					prevEntity = "minecraft:enderman";
				}
			} else if (PacketStructure.identifier.toString().equals("minecraft:villager")) {
				MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.WHISPER, SoundCategory.MUSIC, 0.3f, 1f);
				prevEntity = "minecraft:villager";
			}
			return;
		} else if (!prevEntity.equals("minecraft:")) {
			if (!prevEntity.equals("minecraft:pillager") && !PacketStructure.wasRaid) {
				MinecraftClient.getInstance().getSoundManager().stopAll();
			} else {
				MinecraftClient.getInstance().getSoundManager().stopSounds(MinecraftClient.getInstance().getMusicType().getSound().getKey().get().getValue(), SoundCategory.MUSIC);
			}
			prevEntity = "minecraft:";
		}
		if (ticksPassed < 5500) {
			MinecraftClient.getInstance().getSoundManager().stopSounds(MinecraftClient.getInstance().getMusicType().getSound().getKey().get().getValue(), SoundCategory.MUSIC);
		}
		//If a mc song plays stop the method
		if (prevMusic == null) {
			if (MinecraftClient.getInstance().getMusicTracker().isPlayingType(MinecraftClient.getInstance().getMusicType())) {
				prevMusic = MinecraftClient.getInstance().getMusicType();
				return;
			}
		} else if (MinecraftClient.getInstance().getMusicTracker().isPlayingType(prevMusic)) {
			return;
		} else {
			prevMusic = MinecraftClient.getInstance().getMusicType();
		}

		long dayCount = MinecraftClient.getInstance().world.getTimeOfDay() / 24000L;
		long dayTime = MinecraftClient.getInstance().world.getTimeOfDay() - (dayCount * 24000L);
		TriggerMusic.LOGGER.info(String.valueOf(ticksPassed));
		if (ticksPassed > 5500) {
			if (PacketStructure.hostileCount >= 3) {
				MinecraftClient.getInstance().getSoundManager().stopAll();
				if (Arrays.stream(hot).toList().contains(StructureCheck.getBiome()) || Arrays.stream(hotLike).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.BATTLE_MOLDUGA, SoundCategory.MUSIC, 0.5f, 1);
				} else if (Arrays.stream(nether).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.BATTLE_DUNGEON, SoundCategory.MUSIC, 0.5f, 1);
				} else if (Arrays.stream(end).toList().contains(StructureCheck.getBiome())) {
					ArrayList<SoundEvent> soundEvents = new ArrayList<>();
					soundEvents.add(ModSoundEvents.BATTLE_DIVINE);
					soundEvents.add(ModSoundEvents.GANONDORF);
					soundEvents.add(ModSoundEvents.GODSKIN);
					Collections.shuffle(soundEvents);
					TriggerMusic.LOGGER.info(soundEvents.toString());
					MinecraftClient.getInstance().player.playSoundToPlayer(soundEvents.get(0), SoundCategory.MUSIC, 0.7f, 1f);
				} else if (Arrays.stream(sky).toList().contains(StructureCheck.getBiome())) {
					MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.FLUX, SoundCategory.MUSIC, 0.6f, 1);
				} else if (MinecraftClient.getInstance().player.getY() < -6) {
					int temp = (Math.random() <= 0.5) ? 1 : 2;
					if ( temp == 1) {
						MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.BATTLE_CHASM, SoundCategory.MUSIC, 0.5f, 1f);
					} else {
						MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.FROX, SoundCategory.MUSIC, 0.5f, 1f);
					}
				} else {
					if (PacketStructure.hostileCount >= 3) {
						if (dayTime > 12000 && dayTime < 23000) {
							MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.CAVALRY, SoundCategory.MUSIC, 0.5f, 1f);
						} else {
							int temp = (Math.random() <= 0.5) ? 1 : 2;
							if ( temp == 1) {
								MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.BATTLE_BOTW, SoundCategory.MUSIC, 0.5f, 1f);
							} else {
								MinecraftClient.getInstance().player.playSoundToPlayer(ModSoundEvents.BATTLE_TOTK, SoundCategory.MUSIC, 0.5f, 1f);
							}
						}
					}
				}
				//set ticksPassed to 0
				ticksPassed = 0;
				//get random ticks left
				Random rnd = new Random();
				ticksLeft = rnd.nextInt(5500, 14000);
				//start playing
				isPlaying = true;
				return;
			}
		}
		//decreases tickLeft before next song starts
		ArrayList<SoundEvent> soundEvents = new ArrayList<>();
		//checks biome
		String biome = StructureCheck.getBiome();
		//LOGGER.info(biome.toString());
		//makes game able to start new song if no ticks are left before next song
		if (ticksLeft <= 0) {
			//LOGGER.info("ticksLeft is smaller than 0");
			PacketStructure.previous = "";
			PacketStructure.previousStruct = "";
			isPlaying = false;
		}
		//If smth from the mod plays stop the method
		if (isPlaying) {
			//TriggerMusic.LOGGER.info("isPlaying is true");
			return;
		}

		if (PacketStructure.wasRaid) {
			//TriggerMusic.LOGGER.info("wasRaid");
			return;
		}
		//check biome lists
		if (Arrays.stream(hot).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.DESERT_FIELD);
			soundEvents.add(ModSoundEvents.GERUDO_RUINS);
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.LANAYRU);
			}
		}
		if (Arrays.stream(hotLike).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.GEOGLYPH);
			soundEvents.add(ModSoundEvents.WASTELAND);
		}
		if (Arrays.stream(tropical).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.DRAGONHEAD);
			soundEvents.add(ModSoundEvents.SHRINE_CAVE);
			soundEvents.add(ModSoundEvents.THUNDERHEAD);
			soundEvents.add(ModSoundEvents.ZONAI);
			soundEvents.add(ModSoundEvents.GREENPATH);
		}
		if (Arrays.stream(nether).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.DEATH_MOUNTAIN);
			soundEvents.add(ModSoundEvents.ELDIN);
			soundEvents.add(ModSoundEvents.FIELD_VOLCANO);
			soundEvents.add(ModSoundEvents.END);
		}
		if (Arrays.stream(water).toList().contains(biome)) {
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.LURELIN_DAY);
				soundEvents.add(ModSoundEvents.SEASIDE);
				soundEvents.add(ModSoundEvents.LURELIN_DAY);
				soundEvents.add(ModSoundEvents.SEASIDE);
				soundEvents.add(ModSoundEvents.KASS);
				soundEvents.add(ModSoundEvents.KASS_COVER);
				soundEvents.add(ModSoundEvents.STORM);
			} else if (dayTime > 12000 && dayTime < 23000) {
				soundEvents.add(ModSoundEvents.LURELIN_NIGHT);
				soundEvents.add(ModSoundEvents.LIURNIA);
			}
		}
		if (Arrays.stream(adventure).toList().contains(biome)) {
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.KASS);
				soundEvents.add(ModSoundEvents.KASS_COVER);
				soundEvents.add(ModSoundEvents.STORM);
				soundEvents.add(ModSoundEvents.EDORAS);
				soundEvents.add(ModSoundEvents.SHORTCUT);
			}
		}
		if (Arrays.stream(moderate).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.LIMGRAVE);
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.FIELD_DAY_BOTW);
				soundEvents.add(ModSoundEvents.FIELD_DAY_TOTK);
				soundEvents.add(ModSoundEvents.RIDING_DAY);
				soundEvents.add(ModSoundEvents.PAST);
			} else if (dayTime > 12000 && dayTime < 23000) {
				soundEvents.add(ModSoundEvents.FIELD_NIGHT_BOTW);
				soundEvents.add(ModSoundEvents.FIELD_NIGHT_TOTK);
				soundEvents.add(ModSoundEvents.RIDING_NIGHT);
				soundEvents.add(ModSoundEvents.HOPES);
			}

			if (dayTime > 12000 && dayTime < 14000) {
				soundEvents.clear();
				soundEvents.add(ModSoundEvents.ELVES);
			}
		}
		if (Arrays.stream(cold).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.FIELD_COLD);
			soundEvents.add(ModSoundEvents.SNOWY);
			//soundEvents.add(ModSoundEvents.RITO_FROZEN);
			if (MinecraftClient.getInstance().world.isRaining()) {
				soundEvents.add(ModSoundEvents.WIND_TEMPLE);
				soundEvents.add(ModSoundEvents.EDGE);
			}
		}
		if (Arrays.stream(boreal).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.ROCKY);
			soundEvents.add(ModSoundEvents.TEMPLE);
		}
		if (Arrays.stream(mountain).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.MOUNTAIN);
			soundEvents.add(ModSoundEvents.RUINS);
		}
		if (Arrays.stream(end).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.DEPTHS);
			soundEvents.add(ModSoundEvents.ASCEND);
			//soundEvents.add(ModSoundEvents.GANONDORF);
			soundEvents.add(ModSoundEvents.LIGHTROOT);
			soundEvents.add(ModSoundEvents.RIVER);
		}
		if (Arrays.stream(volcano).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.DEATH_MOUNTAIN);
			soundEvents.add(ModSoundEvents.ELDIN);
			soundEvents.add(ModSoundEvents.FIELD_VOLCANO);
		}
		if (Arrays.stream(sakura).toList().contains(biome)) {
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.KAKARIKO_DAY);
			} else if (dayTime > 12000 && dayTime < 23000) {
				soundEvents.add(ModSoundEvents.KAKARIKO_NIGHT);
			}
		}
		if (Arrays.stream(sky).toList().contains(biome)) {
			soundEvents.add(ModSoundEvents.SKY);
			soundEvents.add(ModSoundEvents.SKYDIVE);
			soundEvents.add(ModSoundEvents.SKY_GRAVITY);
		}
		if (biome.equals("minecraft:swamp")) {
			soundEvents.add(ModSoundEvents.LIURNIA);
		}
		if (biome.equals("minecraft:dark_forest")) {
			soundEvents.add(ModSoundEvents.LABYRINTH);
			soundEvents.add(ModSoundEvents.LABYRINTH_DEPTHS);
		}
		if (biome.equals("terralith:cloud_forest")) {
			soundEvents.add(ModSoundEvents.LABYRINTH_SKY);
		}
		if (biome.equals("minecraft:deep_dark")) {
			soundEvents.add(ModSoundEvents.BENEATH_CASTLE);
			//soundEvents.add(ModSoundEvents.GANON_CASTLE);
			soundEvents.add(ModSoundEvents.SPIRIT);
		}
		if (biome.equals("terralith:mirage_isles")) {
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.KOROK_DAY);
			} else if (dayTime > 12000 && dayTime < 23000) {
				soundEvents.add(ModSoundEvents.KOROK_NIGHT);
			}
		}
		if (biome.equals("minecraft:warped_forest")) {
			soundEvents.clear();
			soundEvents.add(ModSoundEvents.SCARLET);
		}
		if (MinecraftClient.getInstance().world.isRaining() && MinecraftClient.getInstance().player.getBlockPos().getY() > 64 && !Arrays.stream(cold).toList().contains(biome) && !Arrays.stream(hot).toList().contains(biome) && !Arrays.stream(hotLike).toList().contains(biome)) {
			soundEvents.clear();
			if (dayTime > 0 && dayTime < 11000) {
				soundEvents.add(ModSoundEvents.ZORA_DAY);
				soundEvents.add(ModSoundEvents.TEARS);
			} else if (dayTime > 12000 && dayTime < 23000) {
				soundEvents.add(ModSoundEvents.ZORA_NIGHT);
			}
		}
		if (MinecraftClient.getInstance().world.isThundering() && MinecraftClient.getInstance().player.getBlockPos().getY() > 64 && !Arrays.stream(cold).toList().contains(biome) && !Arrays.stream(cold).toList().contains(biome) && !Arrays.stream(hot).toList().contains(biome) && !Arrays.stream(hotLike).toList().contains(biome)) {
			soundEvents.clear();
			soundEvents.add(ModSoundEvents.GLEEOK);
		}
		if (!biome.equals("minecraft:deep_dark")) {
			if (MinecraftClient.getInstance().player.getBlockPos().getY() < 0) {
				soundEvents.clear();
				soundEvents.add(ModSoundEvents.DEPTHS);
				soundEvents.add(ModSoundEvents.DEPTHS_DIVE);
				soundEvents.add(ModSoundEvents.MINE);
			} else if (Arrays.stream(underground).toList().contains(biome)) {
				soundEvents.clear();
				soundEvents.add(ModSoundEvents.CAVE_DAY);
				soundEvents.add(ModSoundEvents.CAVE_NIGHT);
				soundEvents.add(ModSoundEvents.WATERFALL);
				soundEvents.add(ModSoundEvents.CRYSTAL);
			} else if (MinecraftClient.getInstance().player.getBlockPos().getY() < 50) {
				soundEvents.clear();
				soundEvents.add(ModSoundEvents.CAVE_DAY);
				soundEvents.add(ModSoundEvents.CAVE_NIGHT);
				soundEvents.add(ModSoundEvents.CROSSROADS);
				soundEvents.add(ModSoundEvents.SEWERS);
			}
		}
		//LOGGER.info(soundEvents.toString());
		if (soundEvents.isEmpty()) return;
		Collections.shuffle(soundEvents);
		MinecraftClient.getInstance().player.playSoundToPlayer(soundEvents.get(0), SoundCategory.MUSIC, 0.7f, 1f);
		//set ticksPassed to 0
		ticksPassed = 0;
		//get random ticks left
		Random rnd = new Random();
		ticksLeft = rnd.nextInt(5500, 14000);
		//start playing
		isPlaying = true;
	}
}

