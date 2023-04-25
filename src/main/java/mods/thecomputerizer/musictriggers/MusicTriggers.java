package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.RegistryHandler;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import mods.thecomputerizer.theimpossiblelibrary.util.CustomTick;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod(Constants.MODID)
public class MusicTriggers {
    private static LogUtil.ModLogger MOD_LOG;
    private static final LinkedHashMap<Integer, Tuple<String, Integer>> ORDERED_LOG_MESSAGES = new LinkedHashMap<>();
    private static int MESSAGE_COUNTER;
    private static Random RANDOM;

    public MusicTriggers() throws IOException {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        MOD_LOG = LogUtil.create(Constants.MODID);
        MESSAGE_COUNTER = 0;
        RANDOM = new Random();
        if(!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdirs())
                throw new FileExistsException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),FMLEnvironment.dist == Dist.CLIENT);
        if(FMLEnvironment.dist == Dist.CLIENT) {
            ChannelManager.initialize(configFile("channels", "toml"), true);
            ChannelManager.reloading = false;
        }
        if(!ConfigRegistry.CLIENT_SIDE_ONLY)
            if(ConfigRegistry.REGISTER_DISCS)
                RegistryHandler.init(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(Channel.GUI);
        CustomTick.addCustomTickEvent(20);
        //music disc texture overrides
        ev.enqueueWork(() -> ItemModelsProperties.register(ItemRegistry.MUSIC_TRIGGERS_RECORD.get(), new ResourceLocation(Constants.MODID, "trigger"),
                (stack, worldIn, entityIn) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
        ev.enqueueWork(() -> ItemModelsProperties.register(ItemRegistry.CUSTOM_RECORD.get(),
                new ResourceLocation(Constants.MODID, "custom_record"),
                (stack, worldIn, entityIn) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("channelFrom"),
                                stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
    }

    public void commonSetup(FMLCommonSetupEvent ev) {
        if (ConfigRegistry.CLIENT_SIDE_ONLY)
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        else NetworkHandler.register();
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        if(FMLEnvironment.dist == Dist.CLIENT)
            ChannelManager.readResourceLocations();
    }

    public static ResourceLocation getIcon(String type, String name) {
        return Objects.nonNull(type) ? new ResourceLocation(Constants.MODID,"textures/"+type+"/"+name+".png") :
                new ResourceLocation(Constants.MODID,"textures/"+name);
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static int randomInt(int max) {
        return RANDOM.nextInt(max);
    }

    /*
     * Uses a fallback in case someone decides to add something that is not a number to a number parameter
     */
    public static int randomInt(String parameter, String toConvert, int fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        int min = parse(parameter, broken[0], fallback);
        int max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+ RANDOM.nextInt(max-min);
    }

    private static int parse(String parameter, String element, int fallback) {
        try {
            return Integer.parseInt(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static float randomFloat(String parameter, String toConvert, float fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        float min = parse(parameter, broken[0], fallback);
        float max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            float temp = max;
            max = min;
            min = temp;
        }
        float h = max-min;
        if(h<=0) return min;
        return min+(RANDOM.nextFloat()*h);
    }

    private static float parse(String parameter, String element, float fallback) {
        try {
            return Float.parseFloat(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static void logExternally(Level level, String message, Object ... parameters) {
        MOD_LOG.log(level, message, parameters);
        ORDERED_LOG_MESSAGES.put(MESSAGE_COUNTER,new Tuple<>("["+String.format("%-5s",level.toString())+"] "+LogUtil.injectParameters(message, parameters),colorizeLogLevel(level)));
        MESSAGE_COUNTER++;
    }

    private static int colorizeLogLevel(Level level) {
        if(level==Level.DEBUG) return GuiUtil.makeRGBAInt(200,200,200,255);
        else if(level==Level.INFO) return GuiUtil.WHITE;
        else if(level==Level.WARN) return GuiUtil.makeRGBAInt(255,215,0,255);
        else if(level==Level.ERROR) return GuiUtil.makeRGBAInt(255,0,0,255);
        return GuiUtil.makeRGBAInt(100,0,0,255);
    }

    public static Set<Map.Entry<Integer,Tuple<String,Integer>>> getLogEntries() {
        return ORDERED_LOG_MESSAGES.entrySet();
    }

    public static void clearLog() {
        ORDERED_LOG_MESSAGES.clear();
        MESSAGE_COUNTER = 0;
    }

    public static <T> T clone(final T o) {
        return Cloner.standard().deepClone(o);
    }

    public static File configFile(String path, String extension) {
        return FileUtil.generateNestedFile(new File(Constants.CONFIG_DIR,path+"."+extension),false);
    }
}
