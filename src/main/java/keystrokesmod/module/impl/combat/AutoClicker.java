package keystrokesmod.module.impl.combat;

import akka.actor.Kill;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class AutoClicker extends Module {
    private final DescriptionSetting bestWithDelayRemover;
    public SliderSetting minCPS;
    public SliderSetting maxCPS;
    public SliderSetting jitter;
    public SliderSetting blockHitChance;
    public static ButtonSetting leftClick;
    public ButtonSetting rightClick;
    public ButtonSetting breakBlocks;
    public ButtonSetting inventoryFill;
    public ButtonSetting weaponOnly;
    public ButtonSetting blocksOnly;
    private Random rand = null;
    private Method gs;
    private long i;
    private long j;
    private long k;
    private long l;
    private double m;
    private boolean n;
    private boolean hol;

    public AutoClicker() {
        super("AutoClicker", Module.category.combat, 0);
        this.registerSetting(bestWithDelayRemover = new DescriptionSetting("Best with delay remover."));
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9.0, 1.0, 20.0, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12.0, 1.0, 20.0, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 0.1));
        this.registerSetting(blockHitChance = new SliderSetting("Block hit chance", 0.0, 0.0, 100.0, 1.0, "%"));
        this.registerSetting(leftClick = new ButtonSetting("Left click", true));
        this.registerSetting(rightClick = new ButtonSetting("Right click", false));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", false));
        this.registerSetting(inventoryFill = new ButtonSetting("Inventory fill", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));

        try {
            this.gs = GuiScreen.class.getDeclaredMethod("func_73864_a", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        } catch (Exception var4) {
            try {
                this.gs = GuiScreen.class.getDeclaredMethod("mouseClicked", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (Exception var3) {
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
        this.i = 0L;
        this.j = 0L;
        this.hol = false;
    }

    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != Phase.END && Utils.nullCheck() && !mc.thePlayer.isEating()) {
            if (mc.currentScreen == null && mc.inGameHasFocus) {
                if (weaponOnly.isToggled() && !Utils.holdingWeapon()) {
                    return;
                }

                if (leftClick.isToggled() && Mouse.isButtonDown(0)) {
                    this.dc(mc.gameSettings.keyBindAttack.getKeyCode(), 0);
                } else if (rightClick.isToggled() && Mouse.isButtonDown(1)) {
                    if (blocksOnly.isToggled() && (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock))) {
                        return;
                    }
                    this.dc(mc.gameSettings.keyBindUseItem.getKeyCode(), 1);
                } else {
                    this.i = 0L;
                    this.j = 0L;
                }
            } else if (inventoryFill.isToggled() && mc.currentScreen instanceof GuiInventory) {
                if (!Mouse.isButtonDown(0) || !Keyboard.isKeyDown(54) && !Keyboard.isKeyDown(42)) {
                    this.i = 0L;
                    this.j = 0L;
                } else if (this.i != 0L && this.j != 0L) {
                    if (System.currentTimeMillis() > this.j) {
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

        if (this.j > 0L && this.i > 0L) {
            double c = blockHitChance.getInput();
            if (System.currentTimeMillis() > this.j && KillAura.target == null && !ModuleManager.killAura.swing) {
                KeyBinding.setKeyBindState(key, true);
                KeyBinding.onTick(key);
                Reflection.setButton(mouse, true);
                if (mouse == 0 && c > 0.0 && Mouse.isButtonDown(1) && Math.random() >= (100.0 - c) / 100.0) {
                    final int getKeyCode = mc.gameSettings.keyBindUseItem.getKeyCode();
                    KeyBinding.setKeyBindState(getKeyCode, true);
                    KeyBinding.onTick(getKeyCode);
                    Reflection.setButton(1, true);
                }
                this.gd();
            } else if (System.currentTimeMillis() > this.i) {
                KeyBinding.setKeyBindState(key, false);
                Reflection.setButton(mouse, false);
                if (mouse == 0 && c > 0.0 && Math.random() >= (100.0 - c) / 100.0) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                    Reflection.setButton(1, false);
                }
            }
        } else {
            this.gd();
        }

    }

    public void gd() {
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

        this.j = System.currentTimeMillis() + d;
        this.i = System.currentTimeMillis() + d / 2L - (long) this.rand.nextInt(10);
    }

    private void inventoryClick(GuiScreen s) {
        int x = Mouse.getX() * s.width / mc.displayWidth;
        int y = s.height - Mouse.getY() * s.height / mc.displayHeight - 1;

        try {
            this.gs.invoke(s, x, y, 0);
        } catch (IllegalAccessException | InvocationTargetException var5) {
        }

    }
}
