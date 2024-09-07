package keystrokesmod.module.impl.combat;

import keystrokesmod.mixins.impl.entity.EntityPlayerSPAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.gapple.GappleUtlis;
import keystrokesmod.utility.gapple.GappleUtlis2;
import keystrokesmod.utility.render.RRectUtils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class AutoGapple extends Module {
    public SliderSetting duringSendTicks = new SliderSetting("DuringSendTicks", 0, 0, 10, 1);
    public SliderSetting delay = new SliderSetting("Delay", 9, 0, 10, 1);
    public ButtonSetting auto = new ButtonSetting("Auto", false);

    public AutoGapple() {
        super("AutoGapple", category.experimental, "Made for QuickMacro.");
        this.registerSetting(duringSendTicks, delay, auto);
    }

    public static boolean eating = false;
    boolean noCancelC02 = false;
    boolean canStart = false;
    boolean pulsing = false;
    boolean restart = false;
    boolean noC02 = false;
    int slot = -1;
    int c03s = 0;
    int c02s = 0;

    @Override
    public void onEnable() {
        this.c03s = 0;
        this.slot = findItem(36, 45, Items.golden_apple);
        if (this.slot != -1) {
            this.slot -= 36;
        }
    }

    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; ++i) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        eating = false;
        if (this.canStart) {
            pulsing = false;
            GappleUtlis2.Method8();
        }
        GappleUtlis.Method7();
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent event) {
        if  (!mc.isSingleplayer()) {
            if (mc.thePlayer == null || mc.thePlayer.isDead) {
                GappleUtlis2.Method8();
                return;
            }
            if (this.slot == -1) {
                return;
            }
            if (eating) {
                GappleUtlis.Method6();
                if (!GappleUtlis2.storing) {
                    GappleUtlis2.Method2(C09PacketHeldItemChange.class);
                    GappleUtlis2.Method4(C07PacketPlayerDigging.class, it -> ((C07PacketPlayerDigging) it).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM);
                    GappleUtlis2.Method4(C08PacketPlayerBlockPlacement.class, it -> ((C08PacketPlayerBlockPlacement) it).getPosition().getY() == -1);
                    if (!((EntityPlayerSPAccessor) mc.thePlayer).isServerSprint()) {
                        PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        mc.thePlayer.setSprinting(true);
                        ((EntityPlayerSPAccessor) mc.thePlayer).setServerSprint(true);
                    }
                    GappleUtlis2.Method4(C02PacketUseEntity.class, it -> this.noCancelC02);
                    GappleUtlis2.Method4(C0APacketAnimation.class, it -> this.noCancelC02);
                    GappleUtlis2.Method3(C03PacketPlayer.class, packet -> ++this.c03s);
                    GappleUtlis2.setReleaseAction(C03PacketPlayer.class, packet -> --this.c03s);
                    GappleUtlis2.Method6(C02PacketUseEntity.class, packet -> !eating && this.noC02);
                    GappleUtlis2.Method3(C02PacketUseEntity.class, packet -> ++this.c02s);
                    GappleUtlis2.setReleaseAction(C02PacketUseEntity.class, packet -> --this.c02s);
                    this.canStart = true;
                }
            } else {
                eating = true;
            }
            if (this.c03s >= 32) {
                eating = false;
                pulsing = true;
                GappleUtlis2.Method5();
                GappleUtlis2.Method1(new C09PacketHeldItemChange(this.slot), true);
                GappleUtlis2.Method1(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(this.slot + 36).getStack()), true);
                GappleUtlis2.Method8();
                GappleUtlis2.Method1(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem), true);
                pulsing = false;
                Utils.sendMessage("Eat");
                if (this.auto.isToggled()) {
                    if (KillAura.target.getName() != null) {
                        Utils.sendMessage("Stop");
                        restart = false;
                        Utils.sendMessage("Restart");
                    }
                } else {
                    restart = false;
                }
                return;
            }
            if (this.delay.getInput() == 0.0) {
                int i = 0;
                while ((double) i < this.duringSendTicks.getInput()) {
                    GappleUtlis2.Method7(true);
                    ++i;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender2DEvent(TickEvent.RenderTickEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float target = (float) (120.0 * ((double) this.c03s / 32.0));
        int startX = sr.getScaledWidth() / 2 - 68;
        int startY = sr.getScaledHeight() / 2 + 30;
        float width = animateIDK(target, 120, 1.2);
        GlStateManager.disableAlpha();
        RenderUtils.drawRoundedRectangle(startX + 10, (float) ((double) startY + 7.5), 120.0f, 4.8f, 2.0f, new Color(0, 0, 0, 120).getRGB());
        RRectUtils.drawGradientRoundCorner(startX + 10, (float) ((double) startY + 7.5), width, 4.8f, 2.0f);
        GlStateManager.disableAlpha();
    }

    private static float animateIDK(double target, double current, double speed) {
        boolean larger = (target > current);
        if (speed < 0.0F) speed = 0.0F;
        else if (speed > 1.0F) speed = 1.0F;
        double dif = Math.abs(current - target);
        double factor = dif * speed;
//        if (factor < 0.1f) factor = 0.1F;
        if (larger) current += factor;
        else current -= factor;
        return (float) current;
    }

}