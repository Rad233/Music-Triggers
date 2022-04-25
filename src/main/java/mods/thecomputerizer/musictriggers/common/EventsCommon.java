package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import mods.thecomputerizer.musictriggers.util.packets.CurSong;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EventsCommon {

    public static HashMap<BlockPos, Integer> tickCounter = new HashMap<>();
    public static HashMap<BlockPos, ItemStack> recordHolder = new HashMap<>();
    public static HashMap<BlockPos, UUID> recordUUID = new HashMap<>();
    public static HashMap<BlockPos, Level> recordWorld = new HashMap<>();
    public static HashMap<UUID, List<String>> recordMenu = new HashMap<>();

    public static int bossTimer = 0;

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if (bossTimer > 1) bossTimer -= 1;
        else if(bossTimer==1) {
            CalculateFeatures.bossInfo = new HashMap<>();
            bossTimer-=1;
        }
        for (String trigger : CalculateFeatures.victoryMobs.keySet()) {
            if(!CalculateFeatures.allTriggers.contains(trigger)) {
                Map<LivingEntity, Integer> tempMap = CalculateFeatures.victoryMobs.get(trigger);
                for (LivingEntity en : tempMap.keySet()) {
                    int temp = tempMap.get(en);
                    if (temp > 0) CalculateFeatures.victoryMobs.get(trigger).put(en, temp - 1);
                    else CalculateFeatures.victoryMobs.put(trigger, new HashMap<>());
                }
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
        for (Map.Entry<BlockPos, ItemStack> blockPosItemStackEntry : recordHolder.entrySet()) {
            BlockPos blockPos = blockPosItemStackEntry.getKey();
            if (recordHolder.get(blockPos) != null && !recordHolder.get(blockPos).isEmpty() && (recordHolder.get(blockPos).getItem() instanceof BlankRecord || (recordHolder.get(blockPos).getItem() instanceof MusicTriggersRecord && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)))) {
                tickCounter.put(blockPos, tickCounter.get(blockPos) + 1);
                if (randomNum + tickCounter.get(blockPos) >= 6000) {
                    recordWorld.get(blockPos).playSound(null, blockPos, new SoundEvent(new ResourceLocation("minecraft", "item.trident.thunder")), SoundSource.MASTER, 1F, 1F);
                    tickCounter.put(blockPos, 0);
                    String randomMenuSong = "theSongWill_NOTbetHisVALUE";
                    if(recordMenu.get(recordUUID.get(blockPos))!=null) randomMenuSong = recordMenu.get(recordUUID.get(blockPos)).get(new Random().nextInt(recordMenu.get(recordUUID.get(blockPos)).size()));
                    for (SoundEvent s : SoundHandler.allSoundEvents) {
                        if (recordHolder.get(blockPos).getItem() instanceof BlankRecord) {
                            String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:", "");
                            if (songName.matches(CurSong.curSong.get(recordUUID.get(blockPos)))) {
                                recordHolder.put(blockPos, Objects.requireNonNull(MusicTriggersRecord.getBySound(s)).getDefaultInstance());
                            }
                        } else if (recordMenu.get(recordUUID.get(blockPos)) != null && !recordMenu.get(recordUUID.get(blockPos)).isEmpty() && recordWorld.get(blockPos).getBlockState(blockPos).getValue(MusicRecorder.HAS_DISC)) {
                            String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:", "");
                            if (songName.matches(randomMenuSong))
                                recordHolder.put(blockPos, Objects.requireNonNull(MusicTriggersRecord.getBySound(s)).getDefaultInstance());

                        }
                    }
                }
            } else tickCounter.put(blockPos, 0);
        }
    }
}
