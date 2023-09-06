package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.network.PacketJukeBoxCustom;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.object.ItemUtil;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MusicTriggersRecord extends EpicItem {

    public MusicTriggersRecord() {
        addPropertyOverride(Constants.res("trigger"),
                (stack, worldIn, entityIn) -> {
                    String triggerID = tagString(stack,"triggerID");
                    return Objects.nonNull(triggerID) ? mapTriggerToFloat(triggerID) : 0f;
                });
    }

    protected String tagString(ItemStack stack,String key) {
        String ret = ItemUtil.getOrCreateTag(stack).getString(key);
        return ret.matches("") ? null : ret;
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos,
                                      @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock()==BlockRegistry.MUSIC_RECORDER && !state.getValue(MusicRecorder.HAS_RECORD)
                && !state.getValue(MusicRecorder.HAS_DISC)) {
            if (!world.isRemote) {
                MusicRecorder mr = (MusicRecorder) state.getBlock();
                ItemStack stack = player.getHeldItem(hand);
                mr.insertRecord(world, pos, stack, player);
                stack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        } else if (state.getBlock()==Blocks.JUKEBOX && !state.getValue(BlockJukebox.HAS_RECORD)) {
            if (!world.isRemote) {
                ItemStack stack = player.getHeldItem(hand);
                String channel = tagString(stack,"channelFrom");
                String trackID = tagString(stack,"trackID");
                if (Objects.nonNull(channel) && Objects.nonNull(trackID)) {
                    ((BlockJukebox) state.getBlock()).insertRecord(world, pos, state, stack);
                    new PacketJukeBoxCustom(pos,channel,trackID).addPlayers((EntityPlayerMP) player).send();
                    stack.shrink(1);
                    player.addStat(StatList.RECORD_PLAYED);
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @SideOnly(Side.CLIENT)
    protected String getLang(String ... elements) {
        if(Objects.isNull(elements) || elements.length==0) return Constants.MODID;
        if(elements.length==1) return elements[0]+"."+Constants.MODID;
        StringBuilder builder = new StringBuilder(elements[0]+"."+Constants.MODID+".");
        for(int i=1;i<elements.length;i++) {
            builder.append(elements[i]);
            if(i<elements.length-1) builder.append(".");
        }
        return AssetUtil.customLang(builder.toString(),false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip,
                               @Nonnull ITooltipFlag flag) {
        String trackID = tagString(stack,"trackID");
        if(Objects.nonNull(trackID))
            tooltip.add(getLang("item","music_triggers_record","description")+": "+trackID);
        else tooltip.add(getLang("item","music_triggers_record","blank_description"));
    }

    private float mapTriggerToFloat(String trigger) {
        switch (trigger) {
            case "acidrain":
                return 0.01f;
            case "biome":
                return 0.02f;
            case "blizzard":
                return 0.03f;
            case "bloodmoon":
                return 0.04f;
            case "bluemoon":
                return 0.05f;
            case "cloudy":
                return 0.06f;
            case "command":
                return 0.07f;
            case "creative":
                return 0.08f;
            case "time":
                return 0.09f;
            case "dead":
                return 0.1f;
            case "difficulty":
                return 0.11f;
            case "dimension":
                return 0.12f;
            case "effect":
                return 0.13f;
            case "elytra":
                return 0.14f;
            case "fallingstars":
                return 0.15f;
            case "fishing":
                return 0.16f;
            case "gamestages":
                return 0.17f;
            case "generic":
                return 0.18f;
            case "gui":
                return 0.19f;
            case "harvestmoon":
                return 0.2f;
            case "height":
                return 0.21f;
            case "hurricane":
                return 0.22f;
            case "light":
                return 0.23f;
            case "loading":
                return 0.24f;
            case "lowhp":
                return 0.25f;
            case "menu":
                return 0.26f;
            case "mob":
                return 0.27f;
            case "pet":
                return 0.28f;
            case "pvp":
                return 0.29f;
            case "raining":
                return 0.3f;
            case "rainintensity":
                return 0.31f;
            case "riding":
                return 0.32f;
            case "sandstorm":
                return 0.33f;
            case "season":
                return 0.34f;
            case "snowing":
                return 0.35f;
            case "spectator":
                return 0.36f;
            case "storming":
                return 0.37f;
            case "structure":
                return 0.38f;
            case "tornado":
                return 0.39f;
            case "underwater":
                return 0.4f;
            case "victory":
                return 0.41f;
            case "zones":
                return 0.42f;
            default:
                return 0f;
        }
    }
}