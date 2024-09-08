package keystrokesmod.module.impl.player;

import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.*;

public class AntiVoid extends Module {
    private static final String[] MODES = new String[]{"Hypixel", "AirStuck", "Remiaft", "Vulcan"};
    private final ModeSetting mode;
    private final SliderSetting distance;
    private final ButtonSetting toggleScaffold;

    private Vec3 position, motion;
    private boolean wasVoid, setBack;
    private int overVoidTicks;
    private boolean disabledForLongJump = false;

    private boolean airStuck = false;
    private double airStuck$posX, airStuck$posY, airStuck$posZ;
    private float airStuck$yaw, airStuck$pitch;

    private int lastSlot = -1;
    private int delayed = -1;

    private boolean fallDistanced = false;

    public AntiVoid() {
        super("AntiVoid", category.player);
        this.registerSetting(new DescriptionSetting("Prevent you from falling into the void."));
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0));
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
        this.registerSetting(toggleScaffold = new ButtonSetting("Toggle scaffold", false));
    }

    @Override
    public void onDisable() {
        if (mode.getInput() == 0)
            blink.disable();
        airStuck = false;
    }

    @SubscribeEvent
    public void onAABB(BlockAABBEvent event) {
        if (!Utils.nullCheck()) return;
        if (mc.thePlayer.fallDistance > distance.getInput())
            fallDistanced = true;
        if (mode.getInput() == 3) {
            if (fallDistanced && event.getBlockPos().getY() < mc.thePlayer.posY) {
                if (BlockUtils.getBlock(event.getBlockPos()) instanceof BlockAir) {
                    final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                } else {
                    fallDistanced = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        switch ((int) mode.getInput()) {
            case 0:
                if (mc.thePlayer.capabilities.allowFlying) return;
                if (mc.thePlayer.ticksExisted <= 50) return;

                if (disabledForLongJump && mc.thePlayer.onGround)
                    disabledForLongJump = false;

                if (longJump.isEnabled())
                    disabledForLongJump = true;
                if (scaffold.isEnabled() || fly.isEnabled() || disabledForLongJump) {
                    blink.disable();
                    return;
                }

                boolean overVoid = !mc.thePlayer.onGround && Utils.overVoid();

                if (overVoid) {
                    overVoidTicks++;
                } else if (mc.thePlayer.onGround) {
                    overVoidTicks = 0;
                }

                if (overVoid && position != null && motion != null && overVoidTicks < 30 + distance.getInput() * 20) {
                    if (!setBack) {
                        wasVoid = true;

                        blink.enable();

                        if (mc.thePlayer.fallDistance > distance.getInput() || setBack) {
                            sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(position.xCoord, position.yCoord - 0.1 - Math.random(), position.zCoord, false));
                            if (this.toggleScaffold.isToggled()) {
                                scaffold.enable();
                            }

                            ((Blink) blink).blinkedPackets.clear();

                            mc.thePlayer.fallDistance = 0;

                            setBack = true;
                        }
                    } else {
                        blink.disable();
                    }
                } else {

                    setBack = false;

                    if (wasVoid) {
                        blink.disable();
                        wasVoid = false;
                    }

                    motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
                    position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                }
                break;
            case 2:
                if (airStuck) {
                    if (delayed == -1) {
                        delayed = 5;
                    }
                    if (delayed > 0) delayed--;

                    if (delayed == 0) {
                        int slot;
                        if ((slot = ContainerUtils.getSlot(ItemSword.class)) != -1) {
                            lastSlot = mc.thePlayer.inventory.currentItem;
                            mc.thePlayer.inventory.currentItem = slot;
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
                        }
                    }
                } else {
                    if (lastSlot != -1) {
                        mc.thePlayer.inventory.currentItem = lastSlot;
                        lastSlot = -1;
                    }
                    delayed = -1;
                }
            case 1:
                if (!mc.thePlayer.onGround && noBlockUnder() && mc.thePlayer.fallDistance > distance.getInput()) {
                    if (!airStuck) {
                        airStuck$posX = mc.thePlayer.posX;
                        airStuck$posY = mc.thePlayer.posY;
                        airStuck$posZ = mc.thePlayer.posZ;
                        airStuck$yaw = mc.thePlayer.rotationYaw;
                        airStuck$pitch = mc.thePlayer.rotationPitch;
                    }
                    airStuck = true;
                } else {
                    airStuck = false;
                }
                break;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if ((mode.getInput() == 1 || mode.getInput() == 2) && airStuck) {
            mc.thePlayer.setPosition(airStuck$posX, airStuck$posY, airStuck$posZ);
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
            event.setYaw(airStuck$yaw);
            event.setPitch(airStuck$pitch);
        }
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (!(event.getPacket() instanceof C03PacketPlayer)) return;

        if ((mode.getInput() == 1 || mode.getInput() == 2) && airStuck) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && airStuck) {
            final S08PacketPlayerPosLook p = (S08PacketPlayerPosLook) event.getPacket();
            airStuck$posX = p.getX();
            airStuck$posY = p.getY();
            airStuck$posZ = p.getZ();
            airStuck$yaw = p.getYaw();
            airStuck$pitch = p.getPitch();
        }
    }

    private boolean noBlockUnder() {
        for (int offset = 0; offset < (double) 30; offset += 2) {
            final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void sendNoEvent(final Packet<?> packet) {
        PacketUtils.sendPacketNoEvent(packet);
    }
}