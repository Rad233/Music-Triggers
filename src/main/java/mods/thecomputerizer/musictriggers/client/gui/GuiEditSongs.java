package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiEditSongs extends Screen {
    public List<String> songs;
    public List<String> codes;
    public Screen parentScreen;
    public GuiScrollingSong scrollingSongs;
    public configObject holder;
    private final Identifier background;

    public GuiEditSongs(Screen parentScreen, configObject holder) {
        super(new TranslatableText("screen.musictriggers.edit_songs"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.songs = this.holder.getAllSongs();
        this.codes = this.holder.getAllCodes();
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        drawCenteredText(matrix, this.textRenderer, new TranslatableText("screen.musictriggers.text.edit_song"), width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addSongs();
        eventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingSong(this.client, this.width, this.height,32,this.height-32, this.songs, this.codes, this, this.holder, null);
        this.scrollingSongs.setRenderBackground(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    ((GuiMain)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    @Override
    public void renderBackgroundTexture(int i) {}

    protected void renderBorders(int startY, int endY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.background);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferbuilder.vertex(0, endY, 0.0D).texture(0.0f, (float)endY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(this.width, endY, 0.0D).texture((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(this.width, startY, 0.0D).texture((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(0, startY, 0.0D).texture(0.0f, (float)startY / 32.0F).color(64, 64, 64, 255).next();
        tessellator.draw();
    }

    @Override
    public void onClose() {
        eventsClient.renderDebug = true;
        super.onClose();
    }
}
