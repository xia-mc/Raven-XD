package keystrokesmod.module.impl.world;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.tower.*;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import static keystrokesmod.module.ModuleManager.scaffold;

public class Tower extends Module {
    private final ButtonSetting disableWhileCollided;
    private final ButtonSetting disableWhileHurt;
    private final ButtonSetting sprintJumpForward;
    private final ButtonSetting stopMotion;

    private boolean lastTowering = false;

    public Tower() {
        super("Tower", category.world);
        this.registerSetting(new DescriptionSetting("Works with SafeWalk & Scaffold"));
        final ModeValue mode;
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new VanillaTower("Vanilla", this))
                .add(new JumpSprintTower("JumpSprint", this))
                .add(new HypixelTower("Hypixel", this))
                .add(new BlocksMCTower("BlocksMC", this))
                .add(new ConstantMotionTower("ConstantMotion", this))
                .add(new VulcanTower("Vulcan", this))
        );

        this.registerSetting(disableWhileCollided = new ButtonSetting("Disable while collided", false));
        this.registerSetting(disableWhileHurt = new ButtonSetting("Disable while hurt", false));
        this.registerSetting(sprintJumpForward = new ButtonSetting("Sprint jump forward", true));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
        this.canBeEnabled = false;

        mode.enable();
        FMLCommonHandler.instance().bus().register(new Object(){
            @SubscribeEvent
            public void onUpdate(TickEvent.ClientTickEvent event) {
                final boolean curCanTower = canTower();
                if (!curCanTower && lastTowering && stopMotion.isToggled())
                    MoveUtil.stop();
                lastTowering = curCanTower;
            }
        });
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
        return ((ModuleManager.safeWalk.isEnabled() && ModuleManager.safeWalk.tower.isToggled() && SafeWalk.canSafeWalk()) || (scaffold.isEnabled() && scaffold.tower.isToggled()));
    }

    public boolean canSprint() {
        return canTower() && this.sprintJumpForward.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && Utils.jumpDown();
    }
}
