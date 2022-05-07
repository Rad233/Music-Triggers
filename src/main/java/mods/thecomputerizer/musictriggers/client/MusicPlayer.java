package mods.thecomputerizer.musictriggers.client;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.platform.InputConstants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.GuiCurPlaying;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigCommands;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.config.ConfigTitleCards;
import mods.thecomputerizer.musictriggers.config.ConfigToml;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.audio.SetVolumeSound;
import mods.thecomputerizer.musictriggers.util.audio.SoundManipulator;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import mods.thecomputerizer.musictriggers.util.packets.ExecuteCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static mods.thecomputerizer.musictriggers.util.packets.CurSong.curSong;

@SuppressWarnings({"rawtypes", "ConstantConditions"})
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class MusicPlayer {
    public static final KeyMapping RELOAD = new KeyMapping("key.musictriggers.menu", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.musictriggers");

    public static List<String> curTrackList;
    public static List<String> fadeOutList;
    public static List<String> reverseTrackList;
    public static List<String> holder;
    public static String curTrack;
    public static String curTrackHolder;
    public static SetVolumeSound curMusic;
    public static Minecraft mc = Minecraft.getInstance();
    public static net.minecraft.client.sounds.SoundManager sh;
    public static int tickCounter = 0;
    public static boolean fadingIn = false;
    public static boolean fadingOut = false;
    public static boolean reverseFade = false;
    public static int tempFadeIn = 0;
    public static int tempFadeOut = 0;
    private static int savedFadeOut = 0;
    private static float saveVolIn = 1;
    private static float saveVolOut = 1;
    public static List<String> tempTitleCards = new ArrayList<>();
    public static boolean delay = false;
    public static int delayTime = 0;
    public static SoundEvent fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
    public static boolean playing = false;
    public static boolean reloading = false;
    public static boolean cards = true;
    public static boolean finish = false;
    public static HashMap<String, SetVolumeSound> musicLinker = new HashMap<>();
    public static HashMap<String, String[]> triggerLinker = new HashMap<>();
    public static HashMap<String, Float> volumeLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeInLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeOutLinker = new HashMap<>();
    public static HashMap<String, Integer> fadeInLinkerMax = new HashMap<>();
    public static HashMap<String, Integer> fadeOutLinkerMax = new HashMap<>();
    public static HashMap<String, Boolean> linkedFadingIn = new HashMap<>();
    public static HashMap<String, Boolean> linkedFadingOut = new HashMap<>();
    public static HashMap<String, Map<Integer, String[]>> loopLinker = new HashMap<>();
    public static HashMap<String, Map<Integer, Integer>> loopLinkerCounter = new HashMap<>();
    public static List<String> oncePerTrigger = new ArrayList<>();
    public static List<String> onceUntilEmpty = new ArrayList<>();
    private static String trackToDelete;
    private static int indexToDelete;
    private static List<String> playedEvents = new ArrayList<>();
    private static SoundInstance playedMusic;
    public static long curMusicTimer = 0;
    public static Channel curMusicSource;
    public static Map<Integer, Boolean> canPlayTitle = new HashMap<>();
    public static Map<Integer, Boolean> canPlayImage = new HashMap<>();
    public static boolean paused = false;
    private static String prevPlayingLink;
    private static String curLinkNum;
    private static boolean nullFromLink = false;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!reloading && !mc.isPaused()) {
            sh = mc.getSoundManager();
            if (curMusic != null && sh.soundEngine.instanceToChannel.get(curMusic) != null && sh.soundEngine.instanceToChannel.get(curMusic).channel != null) {
                if (!mc.isWindowActive() && Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(curMusic).channel).getState() != 0x1013) {
                    for (String is : musicLinker.keySet()) {
                        if (sh.soundEngine.instanceToChannel.get(musicLinker.get(is)) != null)
                            Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(musicLinker.get(is)).channel).pause();
                    }
                    paused = true;
                } else if (paused && mc.isWindowActive() && !playing && !mc.isPaused()) {
                    for (String is : musicLinker.keySet()) {
                        if (sh.soundEngine.instanceToChannel.get(musicLinker.get(is)) != null)
                            Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(musicLinker.get(is)).channel).play();
                    }
                    paused = false;
                } else if (paused && playing && Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(curMusic).channel).getState() != 0x1013) {
                    for (String is : musicLinker.keySet()) {
                        if (sh.soundEngine.instanceToChannel.get(musicLinker.get(is)) != null)
                            Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(musicLinker.get(is)).channel).pause();
                    }
                }
            }
            if (paused && mc.isWindowActive() && !playing && !mc.isPaused()) paused = false;
            if (playing && tickCounter % 10 == 0) {
                if (MusicPicker.player != null && (MusicPicker.player.getMainHandItem().getItem() instanceof MusicTriggersRecord))
                    fromRecord = ((MusicTriggersRecord) MusicPicker.player.getMainHandItem().getItem()).getSound();
                else fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
                playing = false;
                if (MusicPicker.player != null) {
                    for (int x = MusicPicker.player.chunkPosition().x - 3; x <= MusicPicker.player.chunkPosition().x + 3; x++) {
                        for (int z = MusicPicker.player.chunkPosition().z - 3; z <= MusicPicker.player.chunkPosition().z + 3; z++) {
                            Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntitiesPos();
                            for (BlockPos b : currentChunkTEPos) {
                                if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te && te.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) playing = true;
                            }
                        }
                    }
                }
            }
            if (tickCounter % 2 == 0 && !paused && mc.isWindowActive()) {
                if (curMusic!=null && sh.soundEngine.instanceToChannel.get(curMusic)!=null && sh.soundEngine.instanceToChannel.get(curMusic).channel!=null && !fadingIn && !fadingOut && !reverseFade && (linkedFadingOut.get(curLinkNum)==null || !linkedFadingOut.get(curLinkNum)) && (linkedFadingIn.get(curLinkNum)==null || !linkedFadingIn.get(curLinkNum)))
                    Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(curMusic).channel).setVolume(mc.options.getSoundSourceVolume(SoundSource.MUSIC)*volumeLinker.get(curLinkNum));
                if (MusicPicker.fishBool) MusicPicker.fishingStart++;
                if (MusicPicker.waterBool) MusicPicker.waterStart++;
                for (Map.Entry<String, Integer> stringListEntry : MusicPicker.triggerPersistence.entrySet()) {
                    String eventID = ((Map.Entry) stringListEntry).getKey().toString();
                    MusicPicker.triggerPersistence.putIfAbsent(eventID, 0);
                    if (MusicPicker.triggerPersistence.get(eventID) > 0) {
                        MusicPicker.triggerPersistence.put(eventID, MusicPicker.triggerPersistence.get(eventID) - 1);
                    }
                }
                if (curTrack != null && sh.isActive(curMusic) && ConfigToml.loopPoints.containsKey(curTrack)) {
                    for (String key : musicLinker.keySet()) {
                        if (loopLinker.get(key) != null) {
                            for (int i : loopLinker.get(key).keySet()) {
                                try {
                                    if (loopLinkerCounter.get(key).get(i) < Integer.parseInt(loopLinker.get(key).get(i)[0]) && Integer.parseInt(loopLinker.get(key).get(i)[2]) <= curMusicTimer) {
                                        MusicTriggers.logger.debug("Loop boundary passed");
                                        SoundManipulator.setMillisecondTimeForSource(musicLinker.get(key), Integer.parseInt(loopLinker.get(key).get(i)[1]));
                                        loopLinkerCounter.get(key).put(i, loopLinkerCounter.get(key).get(i) + 1);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RuntimeException("There was a problem while trying to loop - Check the log for details");
                                }
                            }
                        }
                    }
                }
                if (curTrack != null && sh.isActive(curMusic) && mc.screen instanceof GuiCurPlaying) ((GuiCurPlaying) mc.screen).setSlider(GuiCurPlaying.getSongPosInSeconds(curMusic));
                for (String key : linkedFadingIn.keySet()) {
                    ChannelAccess.ChannelHandle entry = sh.soundEngine.instanceToChannel.get(musicLinker.get(key));
                    if (entry != null && entry.channel != null) {
                        if (linkedFadingIn.get(key) && entry.channel.getState() != 0x1013) {
                            if (fadeInLinker.get(key) == 0) {
                                linkedFadingIn.put(key, false);
                                fadeInLinker.put(key, fadeInLinkerMax.get(key));
                                entry.channel.setVolume(volumeLinker.get(key) * mc.options.getSoundSourceVolume(SoundSource.MUSIC));
                            } else {
                                float calculatedVolume = volumeLinker.get(key) * (((float) (fadeInLinkerMax.get(key) - fadeInLinker.get(key))) / ((float) fadeInLinkerMax.get(key)));
                                calculatedVolume = calculatedVolume * mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                                entry.channel.setVolume(calculatedVolume);
                                fadeInLinker.put(key, fadeInLinker.get(key) - 1);
                            }
                        }
                    }
                }
                for (String key : linkedFadingOut.keySet()) {
                    ChannelAccess.ChannelHandle entry = sh.soundEngine.instanceToChannel.get(musicLinker.get(key));
                    if (entry != null && entry.channel != null) {
                        if (linkedFadingOut.get(key) && entry.channel.getState() != 0x1013) {
                            if (fadeOutLinker.get(key) == 0) {
                                linkedFadingOut.put(key, false);
                                fadeOutLinker.put(key, fadeOutLinkerMax.get(key));
                                entry.channel.setVolume(Float.MIN_VALUE * 1000);
                            } else {
                                float calculatedVolume = volumeLinker.get(key) * (((float) fadeOutLinker.get(key)) / ((float) fadeOutLinkerMax.get(key)));
                                calculatedVolume = calculatedVolume * mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                                entry.channel.setVolume(calculatedVolume);
                                fadeOutLinker.put(key, fadeOutLinker.get(key) - 1);
                            }
                        }
                    }
                }
                if (fadingIn && !fadingOut) {
                    reverseFade = false;
                    ChannelAccess.ChannelHandle entry = sh.soundEngine.instanceToChannel.get(curMusic);
                    if (entry != null && entry.channel != null) {
                        if (tempFadeIn == 0) {
                            fadingIn = false;
                            entry.channel.setVolume(saveVolIn * mc.options.getSoundSourceVolume(SoundSource.MUSIC));
                        } else {
                            float calculatedVolume = saveVolIn * (float) (((double) (MusicPicker.curFadeIn - tempFadeIn)) / ((double) MusicPicker.curFadeIn));
                            calculatedVolume = calculatedVolume * mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                            entry.channel.setVolume(calculatedVolume);
                            tempFadeIn -= 1;
                        }
                    } else clearSongs();
                }
                if (fadingOut && !reverseFade) {
                    ChannelAccess.ChannelHandle entry = sh.soundEngine.instanceToChannel.get(curMusic);
                    tempFadeIn = 0;
                    fadingIn = false;
                    if (entry != null && entry.channel != null) {
                        if (tempFadeOut == 0) clearSongs();
                        else {
                            if (curMusic == null) tempFadeOut = 0;
                            else {
                                float calculatedVolume = saveVolOut * (float) (((double) tempFadeOut) / ((double) savedFadeOut));
                                calculatedVolume = calculatedVolume * mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                                entry.channel.setVolume(calculatedVolume);
                                tempFadeOut -= 1;
                                if (holder != null && holder.equals(fadeOutList)) reverseFade = true;
                            }
                        }
                    } else clearSongs();
                } else if (fadingOut) {
                    fadingIn = false;
                    ChannelAccess.ChannelHandle entry = sh.soundEngine.instanceToChannel.get(curMusic);
                    if(entry!=null && entry.channel!=null) {
                        if (tempFadeOut >= savedFadeOut) {
                            fadingOut = false;
                            reverseFade = false;
                            entry.channel.setVolume(saveVolOut * mc.options.getSoundSourceVolume(SoundSource.MUSIC));
                            tempFadeOut = 0;
                        } else {
                            float calculatedVolume = saveVolOut * (float) (((double) tempFadeOut) / ((double) savedFadeOut));
                            calculatedVolume = calculatedVolume * mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                            entry.channel.setVolume(calculatedVolume);
                            tempFadeOut += 1;
                        }
                    } else clearSongs();
                }
                if (delay) {
                    delayTime -= 1;
                    if (delayTime <= 0) delay = false;
                }
                if (tickCounter % 10 == 0 && !delay) {
                    if (MusicPicker.player != null && (MusicPicker.player.getMainHandItem().getItem() instanceof MusicTriggersRecord))
                        fromRecord = ((MusicTriggersRecord) MusicPicker.player.getMainHandItem().getItem()).getSound();
                    else fromRecord = new SoundEvent(new ResourceLocation("nonsensicalresourcelocation"));
                    playing = false;
                    if (MusicPicker.player != null) {
                        for (int x = MusicPicker.player.chunkPosition().x - 3; x <= MusicPicker.player.chunkPosition().x + 3; x++) {
                            for (int z = MusicPicker.player.chunkPosition().z - 3; z <= MusicPicker.player.chunkPosition().z + 3; z++) {
                                Set<BlockPos> currentChunkTEPos = MusicPicker.world.getChunk(x, z).getBlockEntitiesPos();
                                for (BlockPos b : currentChunkTEPos) {
                                    if (MusicPicker.world.getChunk(x, z).getBlockEntity(b) instanceof JukeboxBlockEntity te && te.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) playing = true;
                                }
                            }
                        }
                    }
                    holder = MusicPicker.playThese();
                    if (holder != null && !holder.isEmpty() && !playing) {
                        boolean startQuiet = false;
                        for (int i : canPlayTitle.keySet()) {
                            if (!canPlayTitle.get(i) && !new HashSet<>(MusicPicker.playableList).containsAll(ConfigTitleCards.titlecards.get(i).getTriggers()))
                                canPlayTitle.put(i, true);
                        }
                        for (int i : canPlayImage.keySet()) {
                            if (!canPlayImage.get(i) && !new HashSet<>(MusicPicker.playableList).containsAll(ConfigTitleCards.imagecards.get(i).getTriggers()))
                                canPlayImage.put(i, true);
                        }
                        for (String playable : MusicPicker.playableList) {
                            if (!MusicPicker.titleCardEvents.contains(playable)) {
                                if (Boolean.parseBoolean(SoundHandler.TriggerInfoMap.get(playable)[34])) {
                                    SoundHandler.TriggerIdentifierMap.get(playable.split("-")[0]).remove(SoundHandler.TriggerInfoMap.get(playable)[10]);
                                    SoundHandler.TriggerInfoMap.remove(playable);
                                    if (SoundHandler.TriggerIdentifierMap.get(playable.split("-")[0]).isEmpty()) {
                                        SoundHandler.TriggerIdentifierMap.remove(playable.split("-")[0]);
                                        SoundHandler.TriggerInfoMap.remove(playable.split("-")[0]);
                                    }
                                }
                            }
                        }
                        if (curMusic == null) finish = false;
                        if (curTrackList == null && !finish) {
                            curTrackList = holder;
                            if (fadeOutList == null) fadeOutList = holder;
                            if (!nullFromLink) {
                                fadingIn = true;
                                startQuiet = true;
                                tempFadeIn = MusicPicker.curFadeIn;
                                savedFadeOut = MusicPicker.curFadeOut;
                            }
                            nullFromLink = false;
                            for (String command : ConfigCommands.commandMap.keySet()) {
                                if (ConfigCommands.commandMap.get(command).equals(MusicPicker.titleCardEvents)) PacketHandler.sendToServer(new ExecuteCommand(command));
                            }
                        }
                        if (curMusic != null) {
                            if (!sh.isActive(curMusic) || mc.options.getSoundSourceVolume(SoundSource.MUSIC) == 0 || mc.options.getSoundSourceVolume(SoundSource.MASTER) == 0) {
                                finish = false;
                                for (String sound : musicLinker.keySet()) sh.stop(musicLinker.get(sound));
                                if (fadingOut) {
                                    fadingOut = false;
                                    fadingIn = true;
                                    startQuiet = true;
                                }
                                curMusic = null;
                                curMusicSource = null;
                                delay = true;
                                delayTime = MusicPicker.curDelay;
                                removeTrack(trackToDelete, indexToDelete, playedEvents, playedMusic);
                            }
                        }
                        if (!finish) {
                            if (MusicPicker.shouldChange || !curTrackList.equals(holder)) {
                                if (curTrackList.size() != 0) changeTrack();
                                else curTrackList = null;
                            } else if (curMusic == null && mc.options.getSoundSourceVolume(SoundSource.MASTER) > 0 && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0) {
                                triggerLinker = new HashMap<>();
                                if (musicLinker != null)
                                    for (String sound : musicLinker.keySet()) sh.stop(musicLinker.get(sound));
                                musicLinker = new HashMap<>();
                                EventsClient.GuiCounter = 0;
                                curTrackList = curTrackList.stream().filter(track -> !oncePerTrigger.contains(track)).collect(Collectors.toList());
                                curTrackList = curTrackList.stream().filter(track -> !onceUntilEmpty.contains(track)).collect(Collectors.toList());
                                if (curTrackList.size() >= 1) {
                                    int i = ThreadLocalRandom.current().nextInt(0, curTrackList.size());
                                    if (curTrackList.size() > 1 && curTrack != null) {
                                        int total = curTrackList.stream().mapToInt(s -> Integer.parseInt(ConfigToml.otherinfo.get(s)[3])).sum();
                                        int j;
                                        for (j = 0; j < 1000; j++) {
                                            int r = ThreadLocalRandom.current().nextInt(1, total + 1);
                                            MusicTriggers.logger.debug("Random was between 1 and " + (total + 1) + " " + r + " was chosen");
                                            String temp = " ";
                                            for (String s : curTrackList) {
                                                if (r < Integer.parseInt(ConfigToml.otherinfo.get(s)[3])) {
                                                    temp = s;
                                                    break;
                                                }
                                                r -= Integer.parseInt(ConfigToml.otherinfo.get(s)[3]);
                                            }
                                            if (!temp.matches(curTrack) && !temp.matches(" ")) {
                                                curTrack = temp;
                                                break;
                                            }
                                        }
                                        if (j >= 1000)
                                            MusicTriggers.logger.warn("Attempt to get non duplicate song passed 1000 tries! Forcing current song " + ConfigToml.songholder.get(curTrack) + " to play.");
                                    } else {
                                        curTrack = curTrackList.get(i);
                                    }
                                    if (curTrack != null) {
                                        curTrack = curTrack.replaceAll("@", "").replaceAll("#", "");
                                        MusicTriggers.logger.debug(curTrack + " was chosen");
                                        finish = Boolean.parseBoolean(ConfigToml.otherinfo.get(curTrack)[2]);
                                        curTrackHolder = ConfigToml.songholder.get(curTrack);
                                        MusicTriggers.logger.info("Attempting to play track: " + curTrackHolder);
                                        if (ConfigToml.triggerlinking.get(curTrack) != null) {
                                            triggerLinker.put("song-" + 0, ConfigToml.triggerlinking.get(curTrack).get(curTrack));
                                            musicLinker.put("song-" + 0, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundSource.MUSIC, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]), Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
                                            volumeLinker.put("song-" + 0, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]));
                                            saveVolIn = Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]);
                                            fadeInLinker.put("song-" + 0, Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[5]));
                                            fadeInLinkerMax.put("song-" + 0, Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[5]));
                                            fadeOutLinker.put("song-" + 0, Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[6]));
                                            fadeOutLinkerMax.put("song-" + 0, Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[6]));
                                            for (int l : ConfigToml.loopPoints.get(curTrack).keySet()) {
                                                loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                                loopLinker.get("song-" + 0).put(l, ConfigToml.loopPoints.get(curTrack).get(l));
                                                loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                                loopLinkerCounter.get("song-" + 0).put(l, 0);
                                            }
                                            int linkcounter = 1;
                                            for (String song : ConfigToml.triggerlinking.get(curTrack).keySet()) {
                                                if (!song.matches(curTrack)) {
                                                    triggerLinker.put("song-" + linkcounter, ConfigToml.triggerlinking.get(curTrack).get(song));
                                                    musicLinker.put("song-" + linkcounter, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + song), SoundSource.MUSIC, Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[1]),
                                                            Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
                                                    volumeLinker.put("song-" + linkcounter, Float.parseFloat(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[1]));
                                                    fadeInLinker.put("song-" + linkcounter, Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[2]));
                                                    fadeInLinkerMax.put("song-" + linkcounter, Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[2]));
                                                    fadeOutLinker.put("song-" + linkcounter, Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[3]));
                                                    fadeOutLinkerMax.put("song-" + linkcounter, Integer.parseInt(ConfigToml.otherlinkinginfo.get(curTrack).get(song)[3]));
                                                    MusicTriggers.logger.info("Fade out ticks for linked song: " + linkcounter + ": " + ConfigToml.otherlinkinginfo.get(curTrack).get(song)[3]);
                                                    if (ConfigToml.linkingLoopPoints.get(curTrack) != null && ConfigToml.linkingLoopPoints.get(curTrack).get(song) != null) {
                                                        for (int l : ConfigToml.linkingLoopPoints.get(curTrack).get(song).keySet()) {
                                                            loopLinker.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                            loopLinker.get("song-" + linkcounter).put(l, ConfigToml.linkingLoopPoints.get(curTrack).get(song).get(l));
                                                            loopLinkerCounter.putIfAbsent("song-" + linkcounter, new HashMap<>());
                                                            loopLinkerCounter.get("song-" + linkcounter).put(l, 0);
                                                        }
                                                    }
                                                }
                                                linkcounter++;
                                            }
                                        } else {
                                            musicLinker.put("song-" + 0, new SetVolumeSound(new ResourceLocation(MusicTriggers.MODID, "music." + curTrackHolder), SoundSource.MUSIC, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]), Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[0]), false, 1, SoundInstance.Attenuation.NONE, 0F, 0F, 0F));
                                            saveVolIn = Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]);
                                            volumeLinker.put("song-" + 0, Float.parseFloat(ConfigToml.otherinfo.get(curTrack)[4]));
                                            for (int l : ConfigToml.loopPoints.get(curTrack).keySet()) {
                                                loopLinker.putIfAbsent("song-" + 0, new HashMap<>());
                                                loopLinker.get("song-" + 0).put(l, ConfigToml.loopPoints.get(curTrack).get(l));
                                                loopLinkerCounter.putIfAbsent("song-" + 0, new HashMap<>());
                                                loopLinkerCounter.get("song-" + 0).put(l, 0);
                                            }
                                        }
                                        if (MusicPicker.player != null) {
                                            if (!ConfigRegistry.clientSideOnly)
                                                PacketHandler.sendToServer(new CurSong(curTrackHolder, MusicPicker.player.getUUID()));
                                            else curSong.put(MusicPicker.player.getUUID(), curTrackHolder);
                                        }
                                        curMusicSource = null;
                                        if (cards) renderCards();
                                        for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
                                            String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                                            musicLinker.get(checkThis).setVolume(Float.MIN_VALUE * 1000);
                                            if (checkThis.matches("song-0")) {
                                                if (!startQuiet) musicLinker.get(checkThis).setVolume(saveVolIn);
                                                curMusic = musicLinker.get(checkThis);
                                                prevPlayingLink = "song-0";
                                                curLinkNum = "song-" + 0;
                                            }
                                            sh.play(musicLinker.get(checkThis));
                                        }
                                        curMusicSource = sh.soundEngine.instanceToChannel.get(curMusic).channel;
                                        if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1]) == 1)
                                            onceUntilEmpty.add(curTrack);
                                        if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1]) == 2)
                                            oncePerTrigger.add(curTrack);
                                        else if (Integer.parseInt(ConfigToml.otherinfo.get(curTrack)[1]) == 3) {
                                            trackToDelete = curTrack;
                                            indexToDelete = i;
                                            playedEvents = MusicPicker.titleCardEvents;
                                            playedMusic = curMusic;
                                        }
                                    } else curTrackList = null;
                                } else onceUntilEmpty = new ArrayList<>();
                            }
                        }
                    } else if (!finish) {
                        if (!playing) {
                            if (curMusic != null) {
                                if (!fadingOut) {
                                    fadingOut = true;
                                    tempFadeOut = MusicPicker.curFadeOut;
                                    saveVolOut = volumeLinker.get(curLinkNum);
                                }
                            } else if (curTrack != null) {
                                curTrack = null;
                                EventsClient.IMAGE_CARD = null;
                                EventsClient.fadeCount = 1000;
                                EventsClient.timer = 0;
                                EventsClient.activated = false;
                                EventsClient.ismoving = false;
                                cards = true;
                            }
                        } else {
                            EventsClient.IMAGE_CARD = null;
                            EventsClient.fadeCount = 1000;
                            EventsClient.timer = 0;
                            EventsClient.activated = false;
                            EventsClient.ismoving = false;
                            if (curMusic != null) {
                                if (Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(curMusic).channel).getState() != 0x1013) {
                                    for (String is : musicLinker.keySet()) {
                                        Objects.requireNonNull(sh.soundEngine.instanceToChannel.get(musicLinker.get(is)).channel).pause();
                                    }
                                    paused = true;
                                }
                            }
                        }
                    }
                }
            }
            tickCounter++;
            if (tickCounter == 100) tickCounter = 10;
        }
    }

    public static void renderCards() {
        MusicTriggers.logger.debug("Finding cards to render");
        int markForDeletion = -1;
        for (int i : ConfigTitleCards.titlecards.keySet()) {
            boolean pass = false;
            if(new HashSet<>(MusicPicker.titleCardEvents).containsAll(ConfigTitleCards.titlecards.get(i).getTriggers()) && new HashSet<>(ConfigTitleCards.titlecards.get(i).getTriggers()).containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(ConfigTitleCards.titlecards.get(i).getVague() && new HashSet<>(MusicPicker.playableList).containsAll(ConfigTitleCards.titlecards.get(i).getTriggers()) && canPlayTitle.get(i)) {
                pass=true;
                canPlayTitle.put(i, false);
            }
            if (pass && mc.player != null) {
                MusicTriggers.logger.info("displaying title card "+i);
                if(!ConfigTitleCards.titlecards.get(i).getTitles().isEmpty()) mc.gui.setTitle(Component.nullToEmpty(Objects.requireNonNull(ChatFormatting.getByName(ConfigTitleCards.titlecards.get(i).getTitlecolor()))+ ConfigTitleCards.titlecards.get(i).getTitles().get(ThreadLocalRandom.current().nextInt(0, ConfigTitleCards.titlecards.get(i).getTitles().size()))));
                if(!ConfigTitleCards.titlecards.get(i).getSubTitles().isEmpty()) mc.gui.setSubtitle(Component.nullToEmpty(Objects.requireNonNull(ChatFormatting.getByName(ConfigTitleCards.titlecards.get(i).getSubtitlecolor()))+ ConfigTitleCards.titlecards.get(i).getSubTitles().get(ThreadLocalRandom.current().nextInt(0, ConfigTitleCards.titlecards.get(i).getSubTitles().size()))));
                if(ConfigTitleCards.titlecards.get(i).getPlayonce()) {
                    markForDeletion = i;
                }
                break;
            }
        }
        if(markForDeletion!=-1) {
            ConfigTitleCards.titlecards.remove(markForDeletion);
            markForDeletion = -1;
        }
        for (int i : ConfigTitleCards.imagecards.keySet()) {
            boolean pass = false;
            if(new HashSet<>(MusicPicker.titleCardEvents).containsAll(ConfigTitleCards.imagecards.get(i).getTriggers()) && new HashSet<>(ConfigTitleCards.imagecards.get(i).getTriggers()).containsAll(MusicPicker.titleCardEvents)) pass=true;
            else if(ConfigTitleCards.imagecards.get(i).getVague() && new HashSet<>(MusicPicker.playableList).containsAll(ConfigTitleCards.imagecards.get(i).getTriggers()) && canPlayImage.get(i)) {
                pass=true;
                canPlayImage.put(i, false);
            }
            if (pass && mc.player != null) {
                if(ConfigTitleCards.imagecards.get(i).getName()!=null) {
                    MusicTriggers.logger.info("displaying image card " + ConfigTitleCards.imagecards.get(i).getName());
                    if (!ConfigTitleCards.ismoving.get(i)) {
                        EventsClient.IMAGE_CARD = new ResourceLocation(MusicTriggers.MODID, "textures/" + ConfigTitleCards.imagecards.get(i).getName() + ".png");
                    } else {
                        EventsClient.pngs = new ArrayList<>();
                        EventsClient.ismoving = true;
                        EventsClient.movingcounter = 0;
                        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/textures/" + ConfigTitleCards.imagecards.get(i).getName());
                        File[] listOfPNG = folder.listFiles();
                        assert listOfPNG != null;
                        List<String> temp = new ArrayList<>();
                        for (File f : listOfPNG) {
                            temp.add(f.getName().replaceAll(".png", ""));
                        }
                        temp.sort(new Comparator<String>() {
                            public int compare(String o1, String o2) {
                                return extractInt(o1) - extractInt(o2);
                            }

                            int extractInt(String s) {
                                String num = s.replaceAll("\\D", "");
                                return num.isEmpty() ? 0 : Integer.parseInt(num);
                            }
                        });
                        for (int index = 0; index < temp.size(); index++) {
                            EventsClient.pngs.add(index, new ResourceLocation(MusicTriggers.MODID, "textures/" + ConfigTitleCards.imagecards.get(i).getName() + "/" + temp.get(index) + ".png"));
                        }
                    }
                    EventsClient.curImageIndex = i;
                    EventsClient.activated = true;

                    if (ConfigTitleCards.imagecards.get(i).getPlayonce()) {
                        markForDeletion = i;
                    }
                    break;
                }
            }
        }
        if(markForDeletion!=-1) {
            ConfigTitleCards.imagecards.get(markForDeletion).setName(null);
        }
        cards = false;
    }

    public static boolean theDecidingFactor(List<String> all, List<String> titlecard, String[] comparison) {
        List<String> updatedComparison = new ArrayList<>();
        boolean cont = false;
        for(String el : comparison) {
            if(titlecard.contains(el)) {
                updatedComparison = Arrays.stream(comparison)
                        .filter(element -> !element.matches(el))
                        .collect(Collectors.toList());
                if(updatedComparison.size()<=0) {
                    return true;
                }
                cont = true;
                break;
            }
        }
        if(cont) {
            return new HashSet<>(all).containsAll(updatedComparison);
        }
        return false;
    }

    public static String formatSongTime() {
        String ret = "No song playing";
        if(curMusic!=null && sh.soundEngine.instanceToChannel.get(curMusic)!=null) {
            try {
                ret = formattedTimeFromMilliseconds(curMusicTimer*curMusic.getPitch());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static String formattedTimeFromMilliseconds(float milliseconds) {
        if(milliseconds!=Integer.MAX_VALUE) {
            if (milliseconds == -1) milliseconds = 0;
            float seconds = milliseconds / 1000f;
            if (seconds % 60 < 10) return (int) (seconds / 60) + ":0" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
            else return(int) (seconds / 60) + ":" + (int) (seconds % 60) + formatMilliseconds(milliseconds);
        }
        return "No song playing";
    }

    private static String formatMilliseconds(float milliseconds) {
        if(milliseconds%1000<10) return ":00"+(int)(milliseconds%1000);
        else if(milliseconds%1000<100) return ":0"+(int)(milliseconds%1000);
        else return ":"+(int)(milliseconds%1000);
    }

    private static void removeTrack(String track, int index, List<String> events, SoundInstance playing) {
        if(track!=null) {
            curTrack = null;
            sh.stop(playing);
            curMusicSource=null;
            curTrackList.remove(index);
            for (String ev : events) {
                String[] trigger = ev.split("-");
                if (trigger.length==1) trigger = (ev+"-_").split("-");
                SoundHandler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).remove(track);
                if(SoundHandler.TriggerIdentifierMap.get(trigger[0]).get(trigger[1]).isEmpty()) {
                    SoundHandler.TriggerIdentifierMap.get(trigger[0]).remove(trigger[1]);
                    SoundHandler.TriggerInfoMap.remove(trigger[0]+"-"+trigger[1]);
                }
                if(SoundHandler.TriggerIdentifierMap.get(trigger[0]).isEmpty()) {
                    SoundHandler.TriggerIdentifierMap.remove(trigger[0]);
                    SoundHandler.TriggerInfoMap.remove(trigger[0]);
                }
            }
            trackToDelete=null;
            playedEvents = new ArrayList<>();
        }
    }

    public static void changeTrack() {
        curTrackList = null;
        tempTitleCards = MusicPicker.titleCardEvents;
        String songNum = null;
        for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
            String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
            if (triggerLinker.get(checkThis) != null) {
                if (theDecidingFactor(MusicPicker.playableList, tempTitleCards, triggerLinker.get(checkThis)) && mc.player != null) {
                    songNum = checkThis;
                    break;
                }
            }
        }
        if (songNum == null) {
            if(curLinkNum==null) {
                MusicTriggers.logger.warn("Index of current music was null! Falling back to default fade out volume. You should report this");
                curLinkNum = "song-"+0;
            }
            triggerLinker = new HashMap<>();
            if(!fadingOut) {
                fadingOut = true;
                tempFadeOut = MusicPicker.curFadeOut;
                if (curMusic != null) saveVolOut = volumeLinker.get(curLinkNum);
                else tempFadeOut = 0;
            }
        } else {
            curTrackList = null;
            EventsClient.IMAGE_CARD = null;
            EventsClient.fadeCount = 1000;
            EventsClient.timer = 0;
            EventsClient.activated = false;
            EventsClient.ismoving = false;
            cards = true;
            String prev = null;
            for (Map.Entry<String, SetVolumeSound> stringListEntry : musicLinker.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (checkThis.matches(songNum)) {
                    prev = checkThis;
                    linkedFadingIn.put(checkThis,true);
                    curMusic = musicLinker.get(checkThis);
                    curLinkNum = checkThis;
                    curTrackHolder = musicLinker.get(checkThis).getLocation().toString().replaceAll("music.", "").replaceAll("riggers:", "");
                    if (ConfigRegistry.registerDiscs && MusicPicker.player != null) {
                        PacketHandler.sendToServer(new CurSong(curTrack, MusicPicker.player.getUUID()));
                    }
                } else if(checkThis.matches(prevPlayingLink)) {
                    linkedFadingOut.put(checkThis,true);
                }
                else {
                    musicLinker.get(checkThis).setVolume(Float.MIN_VALUE*1000);
                    if (sh.soundEngine.instanceToChannel.get(musicLinker.get(checkThis)) != null) {
                        sh.soundEngine.instanceToChannel.get(musicLinker.get(checkThis)).execute(sound -> sound.setVolume(Float.MIN_VALUE*1000));
                    }
                }
            }
            if(prev!=null) prevPlayingLink = prev;
        }
        MusicPicker.shouldChange = false;
    }

    private static void clearSongs() {
        oncePerTrigger = new ArrayList<>();
        onceUntilEmpty = new ArrayList<>();
        fadeOutList = null;
        removeTrack(trackToDelete, indexToDelete, playedEvents, playedMusic);
        fadingOut = false;
        curTrackList = null;
        reverseTrackList = null;
        for (String sound : musicLinker.keySet()) sh.stop(musicLinker.get(sound));
        musicLinker = new HashMap<>();
        EventsClient.IMAGE_CARD = null;
        EventsClient.fadeCount = 1000;
        EventsClient.timer = 0;
        EventsClient.activated = false;
        EventsClient.ismoving = false;
        curMusic = null;
        curTrack = null;
        curTrackHolder = null;
        cards = true;
        tempFadeIn = MusicPicker.curFadeIn;
    }
}
