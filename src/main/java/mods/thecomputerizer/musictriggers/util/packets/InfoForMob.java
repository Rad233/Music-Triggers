package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForMob {
    private String s;

    public InfoForMob(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForMob(UUID u, String n, String r, String t, String tp, String h, String hp, String v, String vi, String i, String num, int time, int to) {
        this.s = u.toString()+","+n+","+r+","+t+","+tp+","+h+","+hp+","+v+","+vi+","+i+","+num+","+time+","+to;
    }

    public static void encode(InfoForMob packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForMob packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateMobAndSend(packet.getDataUUID(), packet.getMobName(), packet.getDetectionRange(),
                packet.getTargettingBoolean(), packet.getHordeTargettingPercentage(), packet.getHealth(),
                packet.getHealthTargettingPercentage(), packet.getVictoryBoolean(), packet.getVictoryID(), packet.getInfernalID(),
                packet.getMobNumber(), packet.getTime(), packet.getTimeOut());

        ctx.setPacketHandled(true);
    }

    public UUID getDataUUID() {
        if(s==null) {
            return null;
        }
        return UUID.fromString(stringBreaker(s)[0]);
    }
    public String getMobName() {
        return stringBreaker(s)[1];
    }
    public Integer getDetectionRange() {
        return Integer.parseInt(stringBreaker(s)[2]);
    }
    public Boolean getTargettingBoolean() {
        return Boolean.parseBoolean(stringBreaker(s)[3]);
    }
    public Integer getHordeTargettingPercentage() {
        return Integer.parseInt(stringBreaker(s)[4]);
    }
    public Integer getHealth() {
        return Integer.parseInt(stringBreaker(s)[5]);
    }
    public Integer getHealthTargettingPercentage() {
        return Integer.parseInt(stringBreaker(s)[6]);
    }
    public Boolean getVictoryBoolean() {
        return Boolean.parseBoolean(stringBreaker(s)[7]);
    }
    public Integer getVictoryID() {
        return Integer.parseInt(stringBreaker(s)[8]);
    }
    public String getInfernalID() {
        return stringBreaker(s)[9];
    }
    public Integer getMobNumber() {
        return Integer.parseInt(stringBreaker(s)[10]);
    }
    public Integer getTime() {
        return Integer.parseInt(stringBreaker(s)[11]);
    }
    public Integer getTimeOut() {
        return Integer.parseInt(stringBreaker(s)[11]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
