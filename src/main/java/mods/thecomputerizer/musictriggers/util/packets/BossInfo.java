package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class BossInfo {

    private final String s;

    public BossInfo(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public BossInfo(String name, float health) {
        this.s = name+","+health;
    }

    public static void encode(BossInfo packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final BossInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        CalculateFeatures.bossInfo.put(packet.getBossName(), packet.getDataHealth());
        EventsCommon.bossTimer = 40;

        ctx.setPacketHandled(true);
    }

    public String getBossName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s,",")[0];
    }

    public float getDataHealth() {
        return Float.parseFloat(stringBreaker(s,",")[1]);
    }

}
