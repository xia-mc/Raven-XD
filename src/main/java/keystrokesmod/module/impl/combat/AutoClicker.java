package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class AutoClicker extends Module {
    public ModeSetting mode;
    public SliderSetting minCPS;
    public SliderSetting maxCPS;
    public SliderSetting jitter;
    public static ButtonSetting leftClick;
    public ButtonSetting rightClick;
    public ButtonSetting breakBlocks;
    public ButtonSetting inventoryFill;
    public ButtonSetting weaponOnly;
    public ButtonSetting blocksOnly;
    private final ModeSetting clickSound;
    private Random rand = null;
    private Method gs;
    private long nextReleaseClickTime;
    private long nextClickTime;
    private long k;
    private long l;
    private double m;
    private boolean n;
    private boolean hol;

    public AutoClicker() {
        super("AutoClicker", Module.category.combat, 0);
        this.registerSetting(new DescriptionSetting("Best with delay remover."));
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"CPS", "Record"}, 0));
        final ModeOnly mode0 = new ModeOnly(mode, 0);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9.0, 1.0, 20.0, 0.5, mode0));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12.0, 1.0, 20.0, 0.5, mode0));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 0.1));
        this.registerSetting(leftClick = new ButtonSetting("Left click", true));
        this.registerSetting(rightClick = new ButtonSetting("Right click", false));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", false));
        this.registerSetting(inventoryFill = new ButtonSetting("Inventory fill", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
        this.registerSetting(clickSound = new ModeSetting("Click sound", new String[]{"None", "Standard", "Double", "Alan"}, 0));

        try {
            this.gs = GuiScreen.class.getDeclaredMethod("func_73864_a", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        } catch (Exception var4) {
            try {
                this.gs = GuiScreen.class.getDeclaredMethod("mouseClicked", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (Exception ignored) {
            }
        }

        if (this.gs != null) {
            this.gs.setAccessible(true);
        }

    }

    public void onEnable() {
        if (this.gs == null) {
            this.disable();
        }

        this.rand = new Random();
    }

    public void onDisable() {
        this.nextReleaseClickTime = 0L;
        this.nextClickTime = 0L;
        this.hol = false;
    }

    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
    }

    @SubscribeEvent
    public void onRenderTick(@NotNull RenderTickEvent ev) {
        if (ev.phase != Phase.END && Utils.nullCheck() && !mc.thePlayer.isEating() && mc.objectMouseOver != null && HitSelect.canAttack(mc.objectMouseOver.entityHit)) {
            if (mc.currentScreen == null && mc.inGameHasFocus) {
                if (leftClick.isToggled() && Mouse.isButtonDown(0) && !(weaponOnly.isToggled() && !Utils.holdingWeapon())) {
                    this.dc(mc.gameSettings.keyBindAttack.getKeyCode(), 0);
                } else if (rightClick.isToggled() && Mouse.isButtonDown(1)) {
                    if (blocksOnly.isToggled() && (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock))) {
                        return;
                    }
                    this.dc(mc.gameSettings.keyBindUseItem.getKeyCode(), 1);
                } else {
                    this.nextReleaseClickTime = 0L;
                    this.nextClickTime = 0L;
                }
            } else if (inventoryFill.isToggled() && mc.currentScreen instanceof GuiInventory) {
                if (!Mouse.isButtonDown(0) || !Keyboard.isKeyDown(54) && !Keyboard.isKeyDown(42)) {
                    this.nextReleaseClickTime = 0L;
                    this.nextClickTime = 0L;
                } else if (this.nextReleaseClickTime != 0L && this.nextClickTime != 0L) {
                    if (System.currentTimeMillis() > this.nextClickTime) {
                        this.gd();
                        this.inventoryClick(mc.currentScreen);
                    }
                } else {
                    this.gd();
                }
            }

        }
    }

    public void dc(int key, int mouse) {
        if (breakBlocks.isToggled() && mouse == 0 && mc.objectMouseOver != null) {
            BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null) {
                Block bl = mc.theWorld.getBlockState(p).getBlock();
                if (bl != Blocks.air && !(bl instanceof BlockLiquid)) {
                    if (!this.hol) {
                        KeyBinding.setKeyBindState(key, true);
                        KeyBinding.onTick(key);
                        this.hol = true;
                        if (clickSound.getInput() != 0) {
                            mc.thePlayer.playSound(
                                    "keystrokesmod:click." + clickSound.getOptions()[(int) clickSound.getInput()].toLowerCase()
                                    , 1, 1
                            );
                        }
                    }

                    return;
                }

                if (this.hol) {
                    KeyBinding.setKeyBindState(key, false);
                    this.hol = false;
                }
            }
        }

        if (jitter.getInput() > 0.0D) {
            double a = jitter.getInput() * 0.45D;
            EntityPlayerSP var10000;
            if (this.rand.nextBoolean()) {
                var10000 = mc.thePlayer;
                var10000.rotationYaw = (float) ((double) var10000.rotationYaw + (double) this.rand.nextFloat() * a);
            } else {
                var10000 = mc.thePlayer;
                var10000.rotationYaw = (float) ((double) var10000.rotationYaw - (double) this.rand.nextFloat() * a);
            }

            if (this.rand.nextBoolean()) {
                var10000 = mc.thePlayer;
                var10000.rotationPitch = (float) ((double) var10000.rotationPitch + (double) this.rand.nextFloat() * a * 0.45D);
            } else {
                var10000 = mc.thePlayer;
                var10000.rotationPitch = (float) ((double) var10000.rotationPitch - (double) this.rand.nextFloat() * a * 0.45D);
            }
        }

        if (this.nextClickTime > 0L && this.nextReleaseClickTime > 0L) {
            if (System.currentTimeMillis() > this.nextClickTime && KillAura.target == null && !ModuleManager.killAura.swing) {
                KeyBinding.setKeyBindState(key, true);
                RecordClick.click();
                KeyBinding.onTick(key);
                Reflection.setButton(mouse, true);
                if (clickSound.getInput() != 0) {
                    mc.thePlayer.playSound(
                            "keystrokesmod:click." + clickSound.getOptions()[(int) clickSound.getInput()].toLowerCase()
                            , 1, 1
                    );
                }
                this.gd();
            } else if (System.currentTimeMillis() > this.nextReleaseClickTime) {
                KeyBinding.setKeyBindState(key, false);
                Reflection.setButton(mouse, false);
            }
        } else {
            this.gd();
        }

    }

    // TODO we need a cpsCalculator
    public void gd() {
        switch ((int) mode.getInput()) {
            case 0:
                double c = Utils.getRandomValue(minCPS, maxCPS, this.rand) + 0.4D * this.rand.nextDouble();
                long d = (int) Math.round(1000.0D / c);
                if (System.currentTimeMillis() > this.k) {
                    if (!this.n && this.rand.nextInt(100) >= 85) {
                        this.n = true;
                        this.m = 1.1D + this.rand.nextDouble() * 0.15D;
                    } else {
                        this.n = false;
                    }

                    this.k = System.currentTimeMillis() + 500L + (long) this.rand.nextInt(1500);
                }

                if (this.n) {
                    d = (long) ((double) d * this.m);
                }

                if (System.currentTimeMillis() > this.l) {
                    if (this.rand.nextInt(100) >= 80) {
                        d += 50L + (long) this.rand.nextInt(100);
                    }

                    this.l = System.currentTimeMillis() + 500L + (long) this.rand.nextInt(1500);
                }

                this.nextClickTime = System.currentTimeMillis() + d;
                this.nextReleaseClickTime = System.currentTimeMillis() + d / 2L - (long) this.rand.nextInt(10);
                break;
            case 1:
                this.nextClickTime = RecordClick.getNextClickTime();
                this.nextReleaseClickTime = this.nextClickTime + 1;
                break;
        }
    }

    private void inventoryClick(@NotNull GuiScreen s) {
        int x = Mouse.getX() * s.width / mc.displayWidth;
        int y = s.height - Mouse.getY() * s.height / mc.displayHeight - 1;

        try {
            this.gs.invoke(s, x, y, 0);
            RecordClick.click();
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }

    }
}
