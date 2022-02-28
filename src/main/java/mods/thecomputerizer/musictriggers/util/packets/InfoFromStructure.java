package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class InfoFromStructure {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infofromstructure");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(boolean b, String s, String d) {
        String send = b+","+s+","+d;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            String s = decode(buf);
            fromServer.clientSyncStruct(getDataBool(s), getDataStruct(s), getDataCurStruct(s));
        });
    }

    public static String getDataCurStruct(String s) {
        return stringBreaker(s)[2];
    }
    public static String getDataStruct(String s) {
        return stringBreaker(s)[1];
    }
    public static boolean getDataBool(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[0]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
