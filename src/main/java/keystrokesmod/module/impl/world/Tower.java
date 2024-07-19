package keystrokesmod.module.impl.world;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.tower.BlocksMCTower;
import keystrokesmod.module.impl.world.tower.HypixelATower;
import keystrokesmod.module.impl.world.tower.HypixelBTower;
import keystrokesmod.module.impl.world.tower.VanillaTower;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.Utils;
import org.lwjgl.input.Keyboard;

import static keystrokesmod.module.ModuleManager.scaffold;

public class Tower extends Module {
    private final ButtonSetting disableWhileCollided;
    private final ButtonSetting disableWhileHurt;
    private final ButtonSetting sprintJumpForward;

    public Tower() {
        super("Tower", category.world);
        this.registerSetting(new DescriptionSetting("Works with SafeWalk & Scaffold"));
        final ModeValue mode;
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new VanillaTower("Vanilla", this))
                .add(new HypixelATower("Hypixel A", this))
                .add(new BlocksMCTower("BlocksMC", this))
                .add(new HypixelBTower("Hypixel B", this))
                .setDefaultValue("Vanilla")
        );

        this.registerSetting(disableWhileCollided = new ButtonSetting("Disable while collided", false));
        this.registerSetting(disableWhileHurt = new ButtonSetting("Disable while hurt", false));
        this.registerSetting(sprintJumpForward = new ButtonSetting("Sprint jump forward", true));
        this.canBeEnabled = false;

        mode.enable();
    }

    public boolean canTower() {
        if (scaffold.totalBlocks() == 0) return false;
        if (mc.currentScreen != null) return false;
        if (!Utils.nullCheck() || !Utils.jumpDown()) {
            return false;
        }
        else if (disableWhileHurt.isToggled() && mc.thePlayer.hurtTime >= 9) {
            return false;
        }
        else if (disableWhileCollided.isToggled() && mc.thePlayer.isCollidedHorizontally) {
            return false;
        }
        else return modulesEnabled();
    }

    public boolean modulesEnabled() {
        return  ((ModuleManager.safeWalk.isEnabled() && ModuleManager.safeWalk.tower.isToggled() && SafeWalk.canSafeWalk()) || (scaffold.isEnabled() && scaffold.tower.isToggled()));
    }

    public boolean canSprint() {
        return canTower() && this.sprintJumpForward.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && Utils.jumpDown();
    }

    public boolean diagonal() {
        return (Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05);
    }
}
