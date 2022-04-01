package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configDebug;
import mods.thecomputerizer.musictriggers.config.configTitleCards;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.client.MinecraftClient;

import java.io.File;

public class reload {

    public static void readAndReload() {
        MusicPlayer.curMusicTimer=0;
        MusicPicker.emptyMapsAndLists();
        configToml.emptyMaps();
        configTitleCards.emptyMaps();
        SoundHandler.emptyListsAndMaps();
        configToml.parse();
        configTitleCards.parse();
        SoundHandler.registerSounds();
        ModSounds.reload();
        MinecraftClient.getInstance().reloadResources();
        refreshDebug();
    }

    public static void refreshDebug() {
        configDebug.parse(new File("config/MusicTriggers/debug.toml"));
    }
}
