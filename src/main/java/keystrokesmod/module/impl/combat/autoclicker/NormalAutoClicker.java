package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.impl.combat.HitSelect;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.MovingObjectPosition;
import org.jetbrains.annotations.NotNull;

public class NormalAutoClicker extends SubMode<IAutoClicker> {
    private final SliderSetting minCPS = new SliderSetting("Min CPS", 8, 1, 20, 0.1);
    private final SliderSetting maxCPS = new SliderSetting("Max CPS", 14, 1, 20, 0.1);
    private final ButtonSetting breakBlocks = new ButtonSetting("Break blocks", true);
    private final ButtonSetting butterFly = new ButtonSetting("Butterfly", true);
    private final boolean leftClick;
    private final boolean rightClick;

    private final CoolDown clickStopWatch = new CoolDown(0);
    private int ticksDown;
    private long nextSwing;

    public NormalAutoClicker(String name, @NotNull IAutoClicker parent, boolean left) {
        super(name, parent);
        leftClick = left;
        rightClick = !left;

        this.registerSetting(minCPS, maxCPS, breakBlocks, butterFly);
    }

    @Override
    public void onUpdate() {
        clickStopWatch.setCooldown(nextSwing);
        if (clickStopWatch.hasFinished() && HitSelect.canAttack(mc.objectMouseOver.entityHit) && mc.currentScreen == null) {
            final long clicks = (long) (Utils.randomizeDouble(minCPS.getInput(), maxCPS.getInput()));

            if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            if (this.nextSwing >= 50 * 2 && butterFly.isToggled()) {
                this.nextSwing = (long) (Math.random() * 100);
            } else {
                this.nextSwing = 1000 / clicks;
            }

            if (rightClick && mc.gameSettings.keyBindUseItem.isKeyDown() && !mc.gameSettings.keyBindAttack.isKeyDown()) {
                click(1);

                if (Math.random() > 0.9) {
                    click(1);
                }
            }

            if (leftClick && ticksDown > 1 && !mc.gameSettings.keyBindUseItem.isKeyDown() && (!breakBlocks.isToggled() || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)) {
                click(0);
            } else if (!breakBlocks.isToggled()) {
                ((PlayerControllerMPAccessor) mc.playerController).setCurBlockDamageMP(0);
            }

            this.clickStopWatch.start();
        }
    }

    private void click(int button) {
        if (mc.currentScreen == null)
            Utils.sendClick(button, true);
        else if (mc.currentScreen instanceof GuiContainer && parent.isInventoryFill())
            Utils.inventoryClick(mc.currentScreen);
    }
}
