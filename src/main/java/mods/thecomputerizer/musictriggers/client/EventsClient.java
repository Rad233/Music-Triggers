package mods.thecomputerizer.musictriggers.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.GuiSuperType;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketBossInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

public class EventsClient {
    public static ResourceLocation IMAGE_CARD = null;
    public static boolean isWorldRendered;
    public static float fadeCount = 1000;
    public static Boolean activated = false;
    public static long timer=0;
    public static int GuiCounter = 0;
    public static int reloadCounter = 0;
    public static boolean ismoving;
    public static String lastAdvancement;
    public static boolean advancement;
    public static PlayerEntity PVPTracker;
    public static boolean renderDebug = true;
    public static boolean zone = false;
    public static boolean firstPass = false;
    public static int x1 = 0;
    public static int y1 = 0;
    public static int z1 = 0;
    public static int x2 = 0;
    public static int y2 = 0;
    public static int z2 = 0;
    private static int bossBarCounter = 0;
    public static final HashMap<String, Boolean> commandMap = new HashMap<>();

    @SubscribeEvent
    public static void playSound(PlaySoundEvent e) {
        SimpleSound silenced = new SimpleSound(e.getSound().getLocation(), SoundCategory.MUSIC, Float.MIN_VALUE*1000, 1F, false, 0, ISound.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);
        for(String s : ConfigDebug.BLOCKED_MOD_MUSIC) {
            if(e.getSound().getLocation().toString().contains(s) && e.getSound().getSource()==SoundCategory.MUSIC) {
                if(!(!ChannelManager.canAnyChannelOverrideMusic() && ConfigDebug.PLAY_NORMAL_MUSIC)) e.setResultSound(silenced);
            }
        }
        for(String s : ConfigDebug.BLOCKED_MOD_RECORDS) {
            if(e.getSound().getLocation().toString().contains(s) && e.getSound().getSource()==SoundCategory.RECORDS) {
                if(!(!ChannelManager.canAnyChannelOverrideMusic() && ConfigDebug.PLAY_NORMAL_MUSIC)) e.setResultSound(silenced);
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent e) {
        lastAdvancement = e.getAdvancement().getId().toString();
        advancement = true;
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent e) {
        boolean first = true;
        boolean wasCorrect = false;
        for(String arg : e.getParseResults().getContext().getArguments().keySet()) {
            if(first) {
                if(arg.matches("triggercommand")) wasCorrect = true;
                first = false;
            } else {
                if (wasCorrect && !arg.matches("any")) commandMap.put(arg, true);
                break;
             }
        }
    }

    public static boolean commandHelper(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        return commandMap.containsKey(id) && commandMap.get(id);
    }

    public static void commandFinish(Trigger trigger) {
        String id = trigger.getParameter("identifier");
        commandMap.put(id,false);
    }

    @SubscribeEvent
    public static void worldRender(RenderWorldLastEvent e) {
        isWorldRendered=true;
    }

    @SubscribeEvent
    public static void clientDisconnected(PlayerEvent.PlayerLoggedOutEvent e) {
        isWorldRendered=false;
    }

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre e) {
        if(e.getType()==RenderGameOverlayEvent.ElementType.ALL && !renderDebug) e.setCanceled(true);
    }

    public static void initReload() {
        IFormattableTextComponent reload = AssetUtil.genericLang(Constants.MODID,"misc","reload_start").withStyle(TextFormatting.RED)
                        .withStyle(TextFormatting.ITALIC);
        if(Objects.nonNull(Minecraft.getInstance().player))
            Minecraft.getInstance().player.sendMessage(reload,Minecraft.getInstance().player.getUUID());
        reloadCounter = 5;
        ChannelManager.reloading = true;
        MusicTriggers.savedMessages.clear();
        MusicTriggers.logExternally(Level.INFO,"Reloading Music...");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent e) {
        if(Channel.GUI.isDown() && Minecraft.getInstance().player!=null) {
            //BlockPos pos = MusicPicker.roundedPos(Minecraft.getMinecraft().player);
            if(!zone) {
                Minecraft.getInstance().setScreen(Instance.createTestGui());
                //Minecraft.getMinecraft().player.sendMessage(new TextComponentString("\u00A74\u00A7o"+I18n.translateToLocal("misc.musictriggers.reload_start")));
                //reloadCounter = 5;
                //ChannelManager.reloading = true;
            }
            /*
            else if(!firstPass) {
                x1 = pos.getX();
                y1 = pos.getY();
                z1 = pos.getZ();
                firstPass = true;
                Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MUSIC, 1f, 1f, pos));
            } else {
                x2 = pos.getX();
                y2 = pos.getY();
                z2 = pos.getZ();
                int temp;
                if(x1>x2) {
                    temp=x1;
                    x1=x2;
                    x2=temp;
                }
                if(y1>y2) {
                    temp=y1;
                    y1=y2;
                    y2=temp;
                }
                if(z1>z2) {
                    temp=z1;
                    z1=z2;
                    z2=temp;
                }
                firstPass = false;
                zone = false;
                Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.MUSIC, 1f, 1f, pos));
                String compiledZoneCoords = x1+","+y1+","+z1+","+x2+","+y2+","+z2;
                parentScreen.holder.editTriggerInfoParameter(parentScreen.songCode, parentScreen.trigger, parentScreen.scrollingSongs.index, compiledZoneCoords);
                Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            }
             */
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(!Minecraft.getInstance().isPaused() && !(Minecraft.getInstance().screen instanceof GuiSuperType) && !renderDebug) renderDebug = true;
        if(reloadCounter>0) {
            reloadCounter-=1;
            if(reloadCounter==1) {
                ChannelManager.reloadAllChannels();
                IFormattableTextComponent reload = AssetUtil.genericLang(Constants.MODID,"misc","reload_finished")
                        .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.ITALIC);
                if(Objects.nonNull(Minecraft.getInstance().player))
                    Minecraft.getInstance().player.sendMessage(reload,Minecraft.getInstance().player.getUUID());
                IMAGE_CARD = null;
                fadeCount = 1000;
                timer = 0;
                activated = false;
                ismoving = false;
                ChannelManager.reloading = false;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void debugInfo(RenderGameOverlayEvent.Text e) {
        if(ConfigDebug.SHOW_DEBUG && isWorldRendered && renderDebug) {
            e.getLeft().add("Music Triggers Debug Information");
            for(Channel channel : ChannelManager.getAllChannels())
                if(channel.curPlayingName()!=null)
                    e.getLeft().add("Channel["+channel.getChannelName()+"] Current Song: "+channel.curPlayingName());
            if(!ConfigDebug.CURRENT_SONG_ONLY) {
                int displayCount = 0;
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if(!channel.formatSongTime().matches("No song playing")) e.getLeft().add("Channel["+channel.getChannelName()+"] Current Song Time: " + channel.formatSongTime());
                    if(channel.formattedFadeOutTime()!=null) e.getLeft().add("Channel["+channel.getChannelName()+"] Fading Out: "+channel.formattedFadeOutTime());
                    if(channel.formattedFadeInTime()!=null) e.getLeft().add("Channel["+channel.getChannelName()+"] Fading In: "+channel.formattedFadeInTime());
                }
                for(Channel channel : ChannelManager.getAllChannels()) {
                    if (!channel.getPlayableTriggers().isEmpty()) {
                        StringBuilder s = new StringBuilder();
                        for (Trigger trigger : channel.getPlayableTriggers()) {
                            String name = trigger.getNameWithID();
                            if (Minecraft.getInstance().font.width(s + " " + name) > 0.75f *
                                    Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                                if (displayCount == 0) {
                                    e.getLeft().add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                                    displayCount++;
                                } else e.getLeft().add(s.toString());
                                s = new StringBuilder();
                            }
                            s.append(" ").append(name);
                        }
                        if (displayCount == 0) e.getLeft().add("Channel["+channel.getChannelName()+"] Playable Events: " + s);
                        else e.getLeft().add(s.toString());
                    }
                    displayCount = 0;
                }
                StringBuilder sm = new StringBuilder();
                sm.append("minecraft");
                for (String ev : ConfigDebug.BLOCKED_MOD_MUSIC) {
                    if(Minecraft.getInstance().font.width(sm+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                        if(displayCount==0) {
                            e.getLeft().add("Blocked Mods: " + sm);
                            displayCount++;
                        } else e.getLeft().add(sm.toString());
                        sm = new StringBuilder();
                    }
                    sm.append(" ").append(ev);
                }
                if(displayCount==0) e.getLeft().add("Blocked Mods: " + sm);
                else e.getLeft().add(sm.toString());
                displayCount=0;
                Minecraft mc = Minecraft.getInstance();
                PlayerEntity player = mc.player;
                World world = player.level;
                if(player!=null && world!=null) {
                    e.getLeft().add("Current Biome: " + world.getBiome(player.blockPosition()).getRegistryName());
                    e.getLeft().add("Current Dimension: " + world.dimension().location());
                    e.getLeft().add("Current Total Light: " + world.getRawBrightness(roundedPos(player), 0));
                    e.getLeft().add("Current Block Light: " + world.getBrightness(LightType.BLOCK, roundedPos(player)));
                    if (MusicPicker.effectList != null && !MusicPicker.effectList.isEmpty()) {
                        StringBuilder se = new StringBuilder();
                        for (String ev : MusicPicker.effectList) {
                            if(Minecraft.getInstance().font.width(se+" "+ev)>0.75f*Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                                if(displayCount==0) {
                                    e.getLeft().add("Effect List: " + se);
                                    displayCount++;
                                } else e.getLeft().add(se.toString());
                                se = new StringBuilder();
                            }
                            se.append(" ").append(ev);
                        }
                        if(displayCount==0) e.getLeft().add("Effect List: " + se);
                        else e.getLeft().add(se.toString());
                    }
                    if(Minecraft.getInstance().crosshairPickEntity != null) {
                        if (getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity) != null)
                            e.getLeft().add("Current Entity Name: " + getLivingFromEntity(Minecraft.getInstance()
                                    .crosshairPickEntity).getName());
                        if (infernalChecker(getLivingFromEntity(Minecraft.getInstance().crosshairPickEntity)) != null)
                            e.getLeft().add("Infernal Mob Mod Name: " + infernalChecker(getLivingFromEntity(
                                    Minecraft.getInstance().crosshairPickEntity)));
                    }
                }
            }
        }
    }

    private static BlockPos roundedPos(PlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }

    @SubscribeEvent
    public static void renderBoss(RenderGameOverlayEvent.BossInfo e) {
        if (bossBarCounter % 11 == 0) {
            PacketHandler.sendToServer(new PacketBossInfo(e.getBossInfo().getName().getString(), e.getBossInfo().getPercent()));
            bossBarCounter = 0;
        }
        bossBarCounter++;
    }

    private static String infernalChecker(@Nullable LivingEntity m) {
        if (ModList.get().isLoaded("infernalmobs") && m != null)
            return InfernalMobsCore.getMobModifiers(m) == null ? null : InfernalMobsCore.getMobModifiers(m).getModName();
        return null;
    }

    private static LivingEntity getLivingFromEntity(Entity e) {
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }
}
