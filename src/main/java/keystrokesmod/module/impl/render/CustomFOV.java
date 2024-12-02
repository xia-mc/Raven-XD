package keystrokesmod.module.impl.render;


//import keystrokesmod.event.FOVUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.client.event.FOVUpdateEvent;


public class CustomFOV extends Module {

    private static final SliderSetting baseFOV = new SliderSetting("FOV: ", 70, 1, 179, 1);
    public final ButtonSetting forceStatic = new ButtonSetting("Static", false);


    private float savedFOVSetting = 70.0F;
    public CustomFOV() {
        super("CustomFOV", category.render, "Allows you to change FOV beyond Minecraft defaults");
        //this.registerSetting(new DescriptionSetting("Currently very broken"));
        this.registerSetting(baseFOV);
        this.registerSetting(forceStatic);
        // line wrapping in the future, maybe?
        this.registerSetting(new DescriptionSetting("Note that \"static\" also"));
        this.registerSetting(new DescriptionSetting("affects the viewmodel and"));
        this.registerSetting(new DescriptionSetting("anything like zoom."));
    }

    public static float getDesiredFOV() {
        return (float) baseFOV.getInput();
    }

    public void onEnable() {

        savedFOVSetting = mc.gameSettings.fovSetting;
        Utils.sendMessage("Saved FOV as: " + savedFOVSetting);
    }

    public void onDisable() {
        mc.gameSettings.fovSetting = savedFOVSetting;
        Utils.sendMessage("Loaded FOV as: " + savedFOVSetting);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate() {

        mc.gameSettings.fovSetting = (float) baseFOV.getInput();

    }

    public void onFOVChange(@NotNull FOVUpdateEvent event) {
        Utils.sendMessage("FOV Change");
        if(forceStatic.isToggled()){
            Utils.sendMessage("FOV Change");
        }
    }


   /* public static class DesiredFOV {
        public final float DesiredFOV = (float) baseFOV.getInput();

    }


    */
}
