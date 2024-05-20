package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class FastMine extends Module { // from b4 src
    private DescriptionSetting description;
    private SliderSetting delay;
    public SliderSetting multiplier;
    private SliderSetting mode;
    private ButtonSetting creativeDisable;
    private float lastCurBlockDamageMP;
    private String[] modes = new String[]{"Pre", "Post", "Increment"};

    public FastMine() {
        super("FastMine", category.player);
        this.registerSetting(description = new DescriptionSetting("Default is 5 delay & 1x speed."));
        this.registerSetting(delay = new SliderSetting("Break delay ticks", 5.0, 0.0, 5.0, 1.0));
        this.registerSetting(multiplier = new SliderSetting("Break speed multiplier", 1.0, 1.0, 2.0, 0.02, "x"));
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(creativeDisable = new ButtonSetting("Disable in creative", true));
    }

    @Override
    public String getInfo() {
        return ((int) multiplier.getInput() == multiplier.getInput() ? (int) multiplier.getInput() + "" : multiplier.getInput()) + multiplier.getInfo();
    }

    @SubscribeEvent
    public void a(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !mc.inGameHasFocus || !Utils.nullCheck()) {
            return;
        }
        if (creativeDisable.isToggled() && mc.thePlayer.capabilities.isCreativeMode) {
            return;
        }
        final int delay = (int) this.delay.getInput();
        if (delay < 5.0) {
            try {
                if (delay == 0.0) {
                    Reflection.blockHitDelay.set(mc.playerController, 0);
                } else if (Reflection.blockHitDelay.getInt(mc.playerController) > delay) {
                    Reflection.blockHitDelay.set(mc.playerController, delay);
                }
            } catch (IllegalAccessException ex) {
            } catch (IndexOutOfBoundsException ex2) {
            }
        }
        final double c = multiplier.getInput();
        if (c > 1.0) {
            if (!mc.thePlayer.capabilities.isCreativeMode && Mouse.isButtonDown(0)) {
                try {
                    final float float1 = Reflection.curBlockDamageMP.getFloat(mc.playerController);
                    switch ((int) mode.getInput()) {
                        case 0: {
                            float n = (float) (1.0 - 1.0 / c);
                            if (float1 > 0.0f && float1 < n) {
                                Reflection.curBlockDamageMP.set(mc.playerController, n);
                                break;
                            }
                            break;
                        }
                        case 1: {
                            final double n2 = 1.0 / c;
                            if (float1 < 1.0f && float1 >= n2) {
                                Reflection.curBlockDamageMP.set(mc.playerController, 1);
                                break;
                            }
                            break;
                        }
                        case 2: {
                            float n3 = -1.0f;
                            if (float1 < 1.0f) {
                                if (mc.objectMouseOver != null && float1 > this.lastCurBlockDamageMP) {
                                    n3 = (float) (this.lastCurBlockDamageMP + BlockUtils.getBlockHardness(mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock(), mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem), false, false) * (c - 0.2152857 * (c - 1.0)));
                                }
                                if (n3 != -1.0f && float1 > 0.0f) {
                                    Reflection.curBlockDamageMP.set(mc.playerController, n3);
                                }
                            }
                            this.lastCurBlockDamageMP = float1;
                            break;
                        }
                    }
                } catch (IllegalAccessException | IndexOutOfBoundsException ex3) {
                }
            } else if (mode.getInput() == 2) {
                this.lastCurBlockDamageMP = 0.0f;
            }
        }
    }
}
