package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class ChestESP extends Module {
    private final SliderSetting red;
    private final SliderSetting green;
    private final SliderSetting blue;
    private final ButtonSetting rainbow;
    private final ButtonSetting outline;
    private final ButtonSetting shade;
    private final ButtonSetting disableIfOpened;

    public ChestESP() {
        super("ChestESP", Module.category.render, 0);
        this.registerSetting(red = new SliderSetting("Red", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(green = new SliderSetting("Green", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(blue = new SliderSetting("Blue", 255.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
        this.registerSetting(outline = new ButtonSetting("Outline", false));
        this.registerSetting(shade = new ButtonSetting("Shade", false));
        this.registerSetting(disableIfOpened = new ButtonSetting("Disable if opened", false));
    }

    @SubscribeEvent
    public void o(RenderWorldLastEvent ev) {
        if (!Utils.nullCheck()) {
            return;
        }
        int rgb = rainbow.isToggled() ? Utils.getChroma(2L, 0L) : (new Color((int) red.getInput(), (int) green.getInput(), (int) blue.getInput())).getRGB();
        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest) {
                if (disableIfOpened.isToggled() && ((TileEntityChest) tileEntity).lidAngle > 0.0f) {
                    continue;
                }
                RenderUtils.renderBlock(tileEntity.getPos(), rgb, outline.isToggled(), shade.isToggled());
            } else {
                if (!(tileEntity instanceof TileEntityEnderChest)) {
                    continue;
                }
                if (disableIfOpened.isToggled() && ((TileEntityEnderChest) tileEntity).lidAngle > 0.0f) {
                    continue;
                }
                RenderUtils.renderBlock(tileEntity.getPos(), rgb, outline.isToggled(), shade.isToggled());
            }
        }
    }
}
