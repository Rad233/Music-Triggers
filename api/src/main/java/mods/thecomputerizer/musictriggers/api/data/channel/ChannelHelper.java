package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.global.Registration;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlHelper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChannelHelper {

    @Getter private static final Map<String,ChannelAPI> channels = new HashMap<>();
    @Getter private static final GlobalData globalData = new GlobalData();
    private static String youtubeEmail = null;
    private static String youtubePassword = null;

    public static @Nullable ChannelAPI findChannel(LoggableAPI logger, String channelName) {
        ChannelAPI channel = channels.get(channelName);
        if(Objects.isNull(channel)) logger.logError("Unable to find channel with name `{}`!",channelName);
        return channel;
    }

    public static @Nullable Debug getDebug() {
        return globalData.getDebug();
    }

    public static boolean getDebugBool(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) && debug.getParameterAsBoolean(name);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDebugList(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) ? (List<String>)debug.getParameterAsList(name) : Collections.emptyList();
    }

    public static Number getDebugNumber(String name) {
        Debug debug = getDebug();
        return Objects.nonNull(debug) ? debug.getParameterAsNumber(name) : 0;
    }

    public static String getDebugString(String name) {
        Debug debug = getDebug();
        if(Objects.isNull(debug)) return "";
        String ret = debug.getParameterAsString(name);
        return Objects.nonNull(ret) ? ret : "";
    }

    public static boolean getRegistrationBool(String name) {
        Registration registration = getRegistration();
        return Objects.nonNull(registration) && registration.getParameterAsBoolean(name);
    }

    public static @Nullable Registration getRegistration() {
        return globalData.getRegistration();
    }

    public static Set<Toggle> getToggles() {
        return globalData.getToggles();
    }

    public static void initChannels(String channelsFile) {
        Holder channels = openToml(channelsFile,globalData);
        if(Objects.isNull(channels)) return;
        for(Table info : channels.getUniqueTables()) {
        }
    }

    /**
     * Assumes the file extension is not present
     */
    public static @Nullable Holder openToml(String path, LoggableAPI logger) {
        path+=".toml";
        try {
            return TomlHelper.readFully(path);
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(logger)) logger.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return null;
        }
    }

    /**
     * Assumes the file extension is not present
     */
    public static List<String> openTxt(String path, @Nullable ChannelAPI channel) {
        path+=".txt";
        File file = FileHelper.get(path,false);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(channel)) channel.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return Collections.emptyList();
        }
    }

    public static void parseChannelData() {
        for(ChannelAPI channel : channels.values()) channel.parseData();
        readToggles();
    }

    private static void readToggles() {

    }

    public static void registerRemoteSources(ChannelAPI channel, AudioPlayerManager manager) {
        registerRemoteSource(channel,manager,"YouTube",() -> new YoutubeAudioSourceManager(true,youtubeEmail,youtubePassword));
        registerRemoteSource(channel,manager,"SoundCloud",SoundCloudAudioSourceManager::createDefault);
        registerRemoteSource(channel,manager,"BandCamp",BandcampAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Vimeo",VimeoAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Twitch",TwitchStreamAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Beam",BeamAudioSourceManager::new);
        registerRemoteSource(channel,manager,"Getyarn",GetyarnAudioSourceManager::new);
        registerRemoteSource(channel,manager,"HTTPAudio",() -> new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
    }

    private static void registerRemoteSource(
            ChannelAPI channel, AudioPlayerManager manager, String sourceName, Supplier<AudioSourceManager> supplier) {
        try {
            manager.registerSourceManager(supplier.get());
        } catch(Exception ex) {
            channel.logError("Failed to register remote source for `{}`!",sourceName,ex);
        }
    }
}