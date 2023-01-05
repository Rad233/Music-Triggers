package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;

public class GuiLogVisualizer extends GuiSuperType {

    private int spacing;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
    }

    @Override
    public void init() {
        super.init();
        this.spacing = (int)(this.font.lineHeight*1.5);
    }

    @Override
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int y = this.height-20;
        int index = MusicTriggers.savedMessages.size()-1;
        while(y>20 && index>=0) {
            drawString(matrix,this.font,MusicTriggers.savedMessages.get(index),20,y,14737632);
            index--;
            y-=this.spacing;
        }
    }

    @Override
    protected void save() {

    }
}
