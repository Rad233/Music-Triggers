package mods.thecomputerizer.musictriggers.util.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class ExecuteCommand {

    private String s;

    public ExecuteCommand(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public ExecuteCommand(String cmd) {
        this.s = cmd;
    }

    public static void encode(ExecuteCommand packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final ExecuteCommand packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        if(context.get().getDirection().getReceptionSide().isServer()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            server.getCommands().performCommand(server.createCommandSourceStack(), packet.getLiteralCommand());
        }

        ctx.setPacketHandled(true);
    }

    public String getLiteralCommand() {
        return s;
    }
}
