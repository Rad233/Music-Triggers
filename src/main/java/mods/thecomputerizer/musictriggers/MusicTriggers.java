package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.config.*;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.Json;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Mod(MusicTriggers.MODID)
public class MusicTriggers {
    public static final String MODID = "musictriggers";

    public static File songsDir;
    public static File texturesDir;

    private static RepositorySource source;

    public static final Logger logger = LogManager.getLogger();

    public MusicTriggers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonsetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addPack);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        File configDir = new File("config", "MusicTriggers");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            songsDir = new File(configDir.getPath(), "songs");
            if (!songsDir.exists()) {
                songsDir.mkdir();
            }
            File assetsDir = new File(songsDir.getPath(), "assets");
            if (!assetsDir.exists()) {
                assetsDir.mkdir();
            }
            File musictriggersDir = new File(assetsDir.getPath(), "musictriggers");
            if (!musictriggersDir.exists()) {
                musictriggersDir.mkdir();
            }
            File soundsDir = new File(musictriggersDir.getPath(), "sounds");
            if (!soundsDir.exists()) {
                soundsDir.mkdir();
            }
            File musicDir = new File(soundsDir.getPath(), "music");
            if (!musicDir.exists()) {
                musicDir.mkdir();
            }
            texturesDir = new File(musictriggersDir.getPath(), "textures");
            if (!texturesDir.exists()) {
                texturesDir.mkdir();
            }
            File mcmeta = new File(songsDir.getPath() + "/pack.mcmeta");
            if (!mcmeta.exists()) {
                try {
                    mcmeta.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                List<String> lines = Arrays.asList("{", "\t\"pack\": {", "\t\t\"pack_format\": 8,", "\t\t\"description\": \"Can you believe this was generated automatically?\"", "\t}", "}");
                Path file = Paths.get(mcmeta.getPath());
                try {
                    Files.write(file, lines, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            File jjson = new File(musictriggersDir.getPath() + "/sounds.json");
            if (!jjson.exists() && Json.allSongs != null) {
                try {
                    jjson.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            File langDir = new File(musictriggersDir.getPath(), "lang");
            if (!langDir.exists()) {
                langDir.mkdir();
            }
            File lang = new File(langDir.getPath() + "/en_us.json");
            if (!lang.exists()) {
                try {
                    lang.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            Mappings.init();
            makeSoundsJson();
            makeDiscLang();
        }
            ConfigToml.parse(FMLEnvironment.dist == Dist.CLIENT);
        if(FMLEnvironment.dist == Dist.CLIENT) {
            ConfigCommands.parse();
            ConfigTitleCards.parse();
            if(Json.allSongs!=null) {
                File pack = new File("config/MusicTriggers/");
                if (pack.isDirectory()) {
                    MusicTriggers.logger.info("found pack :)");
                    source = new PackFolder(pack, PackSource.DEFAULT);
                }
            }
        }
        File debugConfig = new File("config/MusicTriggers/debug.toml");
        if(!debugConfig.exists()) {
            try {
                debugConfig.createNewFile();
                ConfigDebug.create(debugConfig);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        ConfigDebug.parse(debugConfig);
        File registrationConfig = new File("config/MusicTriggers/registration.toml");
        if(!registrationConfig.exists()) {
            try {
                registrationConfig.createNewFile();
                ConfigRegistry.create(registrationConfig);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        ConfigRegistry.parse(registrationConfig);
        RegistryHandler.init(eventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(MusicPlayer.class);
            MinecraftForge.EVENT_BUS.register(EventsClient.class);
        }
        MinecraftForge.EVENT_BUS.register(EventsCommon.class);
        CustomTick.setUp();
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(MusicPlayer.RELOAD);
    }

    public void commonsetup(FMLCommonSetupEvent ev) {
        if(!ConfigRegistry.clientSideOnly) PacketHandler.register();
    }

    public void addPack(AddPackFindersEvent ev) {
        if(source!=null) ev.addRepositorySource(source);
    }

    public static void makeSoundsJson() {
        File sj = new File("config/MusicTriggers/songs/assets/musictriggers/sounds.json");
        if (sj.exists()) {
            sj.delete();
        }
        List<String> writeThis = Json.create();
        if (writeThis != null) {
            try {
                sj.createNewFile();
                FileWriter writer = new FileWriter(sj);
                for (String str : writeThis) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void makeDiscLang() {
        if(ConfigRegistry.registerDiscs) {
            File sj = new File("config/MusicTriggers/songs/assets/musictriggers/lang/en_us.json");
            if (sj.exists()) {
                sj.delete();
            }
            List<String> writeThis = Json.create();
            assert writeThis != null;
            writeThis.clear();
            writeThis = Json.lang();
            if (writeThis != null) {
                try {
                    sj.createNewFile();
                    FileWriter writer = new FileWriter(sj);
                    for (String str : writeThis) {
                        writer.write(str + System.lineSeparator());
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }
}
