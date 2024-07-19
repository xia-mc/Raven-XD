package keystrokesmod.module.impl.world.tower;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static keystrokesmod.module.ModuleManager.tower;

/**
 * Skidded from Rise (com.alan.clients.module.impl.player.scaffold.tower.WatchdogTower)
 * <p>
 * Counter-confused by xia__mc
 * @see hackclient.rise.rh
 * @author Alan34
 */
public class HypixelBTower extends SubMode<Tower> {
    public static final HashSet<EnumFacing> LIMIT_FACING = new HashSet<>(Collections.singleton(EnumFacing.SOUTH));
    private int vr;
    private int er;
    boolean Im = false;
    private int In;
    private float Io;
    double Ip = 0.0;
    private int Iq;
    
    private boolean blockPlaceRequest = false;
    private int offGroundTicks = 0;
    private BlockPos deltaPlace = BlockPos.ORIGIN;

    private final ButtonSetting onlyWhileMoving;

    public HypixelBTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(onlyWhileMoving = new ButtonSetting("Only while moving", true));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;

        if (blockPlaceRequest && !Utils.isMoving() && !onlyWhileMoving.isToggled()) {
            MovingObjectPosition lastScaffoldPlace = ModuleManager.scaffold.placeBlock;
            if (lastScaffoldPlace == null)
                return;
            Optional<Triple<BlockPos, EnumFacing, Vec3>> optionalPlaceSide = RotationUtils.getPlaceSide(
                    lastScaffoldPlace.getBlockPos().add(deltaPlace),
                    LIMIT_FACING
            );
            if (!optionalPlaceSide.isPresent())
                return;

            Triple<BlockPos, EnumFacing, Vec3> placeSide = optionalPlaceSide.get();

            Raven.getExecutor().schedule(() -> ModuleManager.scaffold.place(
                    new MovingObjectPosition(placeSide.getRight().toVec3(), placeSide.getMiddle(), placeSide.getLeft()),
                    false
            ), 50, TimeUnit.MILLISECONDS);
//            ModuleManager.scaffold.tower$noBlockPlace = true;
            blockPlaceRequest = false;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!tower.canTower() || (!MoveUtil.isMoving() && onlyWhileMoving.isToggled())) {
            this.Io = mc.thePlayer.rotationYaw;
            this.er = 100;
        } else {
            ++this.vr;
            ++this.er;
            if (this.vr >= 23) {
                this.vr = 1;
                this.Io = mc.thePlayer.rotationYaw;
                this.er = 100;
            }

            if (mc.thePlayer.onGround) {
                this.er = 0;
            }

            blockPlaceRequest = true;  // place a block on x+1
            if (!MoveUtil.isMoving()) {
                if (!this.Im) {
                    this.Ip = Math.floor(mc.thePlayer.posZ) + 0.99999999999998;
                    this.Im = true;
                }

                ++this.In;
                if (Math.abs((double)this.Iq - mc.thePlayer.posY) >= 1.0) {
                    if (this.In == 1) {
                        MoveUtil.stop();
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, (mc.thePlayer.posZ + this.Ip) / 2.0);
                    } else if (this.In == 2) {
                        MoveUtil.stop();
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, this.Ip);
                        this.et();
                        this.In = 0;
                        this.Im = false;
                    }
                    MoveUtil.stop();
                } else {
                    this.In = 0;
                    this.Im = false;
                }
            }

            float var2 = this.er == 1 ? 90.0F : 0.0F;
            if (RotationUtils.normalize(mc.thePlayer.rotationYaw - this.Io) < var2) {
                this.Io = mc.thePlayer.rotationYaw;
            } else if (RotationUtils.normalize(mc.thePlayer.rotationYaw - this.Io) < 0.0F) {
                this.Io -= var2;
            } else if (RotationUtils.normalize(mc.thePlayer.rotationYaw - this.Io) > 0.0F) {
                this.Io += var2;
            }

//            mc.thePlayer.bIJ = this.Io;  // set the movement yaw...
            if (this.vr < 20) {
                if (MoveUtil.isMoving())
                    MoveUtil.strafe(0.26, this.Io);
                if (Utils.jumpDown()) {
                    switch (this.er) {
                        case 0:
                            if (mc.thePlayer.posY % 1.0 == 0.0) {
                                event.setOnGround(true);
                            }

                            mc.thePlayer.motionY = 0.41999998688697815;
                            break;
                        case 1:
                            mc.thePlayer.motionY = 0.33;
                            break;
                        case 2:
                            mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                    }
                }
            } else if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.4196000099182129;
            } else if (offGroundTicks == 3) {
                mc.thePlayer.motionY = 0.0;
            }

            if (this.er == 2) {
                this.er = -1;
            }

        }
    }

    public void onEnable() {
        this.Ip = mc.thePlayer.posZ;
        this.vr = 0;
        this.Io = mc.thePlayer.rotationYaw;
        if (!mc.thePlayer.onGround) {
            this.er = 100;
        }
        offGroundTicks = 0;
    }

    public void et() {
        this.Iq = (int)Math.floor(mc.thePlayer.posY);
        deltaPlace = new BlockPos(0, 1, 1);
    }
}
