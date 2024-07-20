package keystrokesmod.mixins.impl.client;


import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Invoker("mouseClicked")
    void mouseClicked(int x, int y, int mouse);
}
