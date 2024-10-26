package keystrokesmod.mixins.impl.gui;


import keystrokesmod.utility.interact.moveable.MoveableManager;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends GuiScreen {

    @Override
    protected void mouseClickMove(int x, int y, int p_mouseClickMove_3_, long p_mouseClickMove_4_) {
        super.mouseClickMove(x, y, p_mouseClickMove_3_, p_mouseClickMove_4_);
        if (p_mouseClickMove_3_ == 0)
            MoveableManager.onMouseDrag(x, y);
    }

    @Override
    protected void mouseReleased(int x, int y, int p_mouseReleased_3_) {
        super.mouseReleased(x, y, p_mouseReleased_3_);
        MoveableManager.onMouseRelease();
    }
}
