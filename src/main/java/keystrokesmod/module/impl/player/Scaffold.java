package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Scaffold extends Module { // from b4 :)
    private final SliderSetting forward;
    private final SliderSetting diagonal;
    private final SliderSetting rotation;
    private final SliderSetting fastScaffold;
    private final ButtonSetting autoSwap;
    private final ButtonSetting fastOnRMB;
    private final ButtonSetting highlightBlocks;
    public ButtonSetting safeWalk;
    private final ButtonSetting showBlockCount;
    private final ButtonSetting accelerationCoolDown;
    private final ButtonSetting silentSwing;
    private MovingObjectPosition placeBlock;
    private int lastSlot;
    private final String[] rotationModes = new String[]{"None", "Backwards", "Strict"};
    private final String[] fastScaffoldModes = new String[]{"Disabled", "Sprint"};
    public float placeYaw;
    public float placePitch;
    public int at;
    public int index;
    private boolean slow;
    private int slowTicks;
    public boolean rmbDown;
    private int ticksAccelerated;
    private final Map<BlockPos, Timer> highlight = new HashMap<>();
    public Scaffold() {
        super("Scaffold", category.player);
        this.registerSetting(forward = new SliderSetting("Forward motion", 1.0, 0.5, 1.2, 0.01));
        this.registerSetting(diagonal = new SliderSetting("Diagonal motion", 1.0, 0.5, 1.2, 0.01));
        this.registerSetting(rotation = new SliderSetting("Rotation", rotationModes, 1));
        this.registerSetting(fastScaffold = new SliderSetting("Fast scaffold", fastScaffoldModes, 0));
        this.registerSetting(accelerationCoolDown = new ButtonSetting("Acceleration cooldown", true));
        this.registerSetting(autoSwap = new ButtonSetting("AutoSwap", true));
        this.registerSetting(fastOnRMB = new ButtonSetting("Fast on RMB", false));
        this.registerSetting(highlightBlocks = new ButtonSetting("Highlight blocks", true));
        this.registerSetting(safeWalk = new ButtonSetting("Safewalk", true));
        this.registerSetting(showBlockCount = new ButtonSetting("Show block count", true));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
    }

    public void onDisable() {
        placeBlock = null;
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
            lastSlot = -1;
        }
        at = index = slowTicks = ticksAccelerated = 0;
        slow = false;
        highlight.clear();
    }

    public void onEnable() {
        lastSlot = -1;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        double roundedSpeed = Utils.rnd(Utils.getHorizontalSpeed(), 2);
        if (accelerationCoolDown.isToggled() && (roundedSpeed > 0.26 || (ModuleManager.bHop != null && ModuleManager.bHop.isEnabled() && ModuleManager.bHop.hopping) || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()))) {
            slow = true;
            slowTicks = 0;
            ticksAccelerated++;
        }
        else if (accelerationCoolDown.isToggled() && ticksAccelerated >= 5 && slow && (roundedSpeed <= 0.26 || (ModuleManager.bHop == null || !ModuleManager.bHop.isEnabled() || !ModuleManager.bHop.hopping) || !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) && mc.thePlayer.onGround) {
            slowTicks++;
            if (slowTicks <= 20) {
                mc.thePlayer.motionX *= 0.65;
                mc.thePlayer.motionZ *= 0.65;
            }
            else {
                slow = false;
                slowTicks = 0;
                ticksAccelerated = 0;
            }
        }
        if (rotation.getInput() > 0) {
            if (rotation.getInput() == 2 && placeBlock != null) {
                event.setYaw(placeYaw);
                event.setPitch(placePitch);
            } else {
                event.setYaw(getYaw());
                event.setPitch(85);
            }
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) { // place here
        final ItemStack getHeldItem = mc.thePlayer.getHeldItem();
        if (!autoSwap.isToggled() || getSlot() == -1) {
            if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
                return;
            }
        }
        final HashMap<BlockPos, EnumFacing> possiblePositions = new HashMap<>();
        final int n = mc.thePlayer.onGround ? -1 : -2;
        final int n2 = 3;
        for (int i = n; i < 0; ++i) {
            for (int j = -n2; j <= n2; ++j) {
                for (int k = -n2; k <= n2; ++k) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                    if (!BlockUtils.replaceable(blockPos)) {
                        EnumFacing enumFacing = null;
                        double lastDistance = 0.0;
                        for (EnumFacing enumFacing2 : EnumFacing.VALUES) {
                            enumLoop: {
                                if (enumFacing2 != EnumFacing.DOWN) {
                                    if (enumFacing2 == EnumFacing.UP) {
                                        if (mc.thePlayer.onGround) {
                                            break enumLoop;
                                        }
                                    }
                                    final BlockPos offset = blockPos.offset(enumFacing2);
                                    if (BlockUtils.replaceable(offset)) {
                                        final double distanceSqToCenter = offset.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                                        if (enumFacing == null || distanceSqToCenter < lastDistance) {
                                            enumFacing = enumFacing2;
                                            lastDistance = distanceSqToCenter;
                                        }
                                    }
                                }
                            }
                        }
                        if (enumFacing != null) {
                            possiblePositions.put(blockPos, enumFacing);
                        }
                    }
                }
            }
        }
        if (possiblePositions.isEmpty()) {
            return;
        }
        if (mc.thePlayer.onGround && Utils.isMoving()) {
            if (forward.getInput() != 1.0 && !diagonal()) {
                Utils.setSpeed(Utils.getHorizontalSpeed() * forward.getInput());
            }
            else if (diagonal.getInput() != 1 && diagonal()) {
                Utils.setSpeed(Utils.getHorizontalSpeed() * diagonal.getInput());
            }
        }
        int slot = getSlot();
        if (slot == -1) {
            return;
        }
        if (lastSlot == -1) {
            lastSlot = mc.thePlayer.inventory.currentItem;
        }
        mc.thePlayer.inventory.currentItem = slot;
        if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
            return;
        }
        MovingObjectPosition m = null;
        double n5 = -1.0;
        for (float n6 = -25.0f; n6 < 25.0f; ++n6) {
            final float n7 = (float)(getYaw() - n6 + f());
            for (float n8 = 0.0f; n8 < 23.0f; ++n8) {
                final float m2 = RotationUtils.clamp((float)(70 + n8 + f()));
                final MovingObjectPosition raycast = RotationUtils.rayCast(mc.playerController.getBlockReachDistance(), n7, m2);
                if (raycast != null && Utils.overPlaceable()) {
                    if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        final EnumFacing enumFacing3 = possiblePositions.get(raycast.getBlockPos());
                        if (enumFacing3 != null) {
                            if (enumFacing3 == raycast.sideHit) {
                                if (m == null || !BlockUtils.isSamePos(raycast.getBlockPos(), m.getBlockPos())) {
                                    if (((ItemBlock)getHeldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, getHeldItem)) {
                                        final double squareDistanceTo = mc.thePlayer.getPositionVector().squareDistanceTo(raycast.hitVec);
                                        if (m == null || squareDistanceTo < n5) {
                                            m = raycast;
                                            n5 = squareDistanceTo;
                                            placeYaw = n7;
                                            placePitch = m2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (m != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            placeBlock = m;
            place();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck() || !showBlockCount.isToggled()) {
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                return;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            int blocks = totalBlocks();
            String color = "§";
            if (blocks <= 5) {
                color += "c";
            }
            else if (blocks <= 15) {
                color += "6";
            }
            else if (blocks <= 25) {
                color += "e";
            }
            else {
                color = "";
            }
            mc.fontRendererObj.drawStringWithShadow(color + blocks + " §rblock" + (blocks == 1 ? "" : "s"), scaledResolution.getScaledWidth()/2 + 8, scaledResolution.getScaledHeight()/2 + 4, -1);
        }
    }

    @SubscribeEvent
    public void onMouse(final MouseEvent mouseEvent) {
        if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (placeBlock != null && rmbDown) {
                mouseEvent.setCanceled(true);
            }
        }
    }

    public boolean stopFastPlace() {
        return this.isEnabled() && placeBlock != null;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.nullCheck() || !highlightBlocks.isToggled() || highlight.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, Timer>> iterator = highlight.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Timer> entry = iterator.next();
            if (entry.getValue() == null) {
                entry.setValue(new Timer(750));
                entry.getValue().start();
            }
            int alpha = entry.getValue() == null ? 210 : 210 - entry.getValue().getValueInt(0, 210, 1);
            if (alpha == 0) {
                iterator.remove();
                continue;
            }
            RenderUtils.renderBlock(entry.getKey(), Utils.merge(Theme.getGradient((int) HUD.theme.getInput(), 0), alpha), true, false);
        }
    }

    public boolean sprint() {
        return this.isEnabled() && (fastScaffold.getInput() == 1 && (!fastOnRMB.isToggled() || rmbDown)) && placeBlock != null;
    }

    public boolean safewalk() {
        return this.isEnabled() && safeWalk.isToggled();
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getInput() <= 1 || (rotation.getInput() == 2 && placeBlock != null));
    }

    public static double f() {
        return Utils.randomizeInt(5, 25) / 100.0;
    }

    public static float getYaw() {
        float n = 0.0f;
        final double n2 = mc.thePlayer.movementInput.moveForward;
        final double n3 = mc.thePlayer.movementInput.moveStrafe;
        if (n2 == 0.0) {
            if (n3 == 0.0) {
                n = 180.0f;
            }
            else if (n3 > 0.0) {
                n = 90.0f;
            }
            else if (n3 < 0.0) {
                n = -90.0f;
            }
        }
        else if (n2 > 0.0) {
            if (n3 == 0.0) {
                n = 180.0f;
            }
            else if (n3 > 0.0) {
                n = 135.0f;
            }
            else if (n3 < 0.0) {
                n = -135.0f;
            }
        }
        else if (n2 < 0.0) {
            if (n3 == 0.0) {
                n = 0.0f;
            }
            else if (n3 > 0.0) {
                n = 45.0f;
            }
            else if (n3 < 0.0) {
                n = -45.0f;
            }
        }
        return mc.thePlayer.rotationYaw + n;
    }

    private void place() {
        final ItemStack getHeldItem = mc.thePlayer.getHeldItem();
        if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
            placeBlock = null;
            return;
        }
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getHeldItem, placeBlock.getBlockPos(), placeBlock.sideHit, placeBlock.hitVec)) {
            if (silentSwing.isToggled()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
            else {
                mc.thePlayer.swingItem();
                mc.getItemRenderer().resetEquippedProgress();
            }
            highlight.put(placeBlock.getBlockPos().offset(placeBlock.sideHit), null);
        }
    }

    private int getSlot() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && InvManager.canBePlaced((ItemBlock) itemStack.getItem()) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public int totalBlocks() {
        int totalBlocks = 0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemBlock && InvManager.canBePlaced((ItemBlock) stack.getItem()) && stack.stackSize > 0) {
                totalBlocks += stack.stackSize;
            }
        }
        return totalBlocks;
    }

    private boolean diagonal() {
        return (Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05);
    }
}
