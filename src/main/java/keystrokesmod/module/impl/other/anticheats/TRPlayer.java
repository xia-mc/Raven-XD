package keystrokesmod.module.impl.other.anticheats;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.script.classes.ItemStack;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.minecraft.world.WorldSettings.GameType;

/**
 * 管理玩家信息的类。每个有效玩家都应有一个TRPlayer实例。
 * Classes that manage player information. Each active player should have one instance of TRPlayer.
 */
public class TRPlayer {
    public AbstractClientPlayer fabricPlayer;
    public CheckManager manager;
    public Vec3 currentPos = Vec3.ZERO;
    public Vec3 currentMotion = Vec3.ZERO;
    public Vec3 currentVehicleMotion = Vec3.ZERO;
    public ItemStack currentMainHead = ItemStack.EMPTY;
    public ItemStack currentOffHead = ItemStack.EMPTY;
    public boolean currentSprint = false;
    public boolean currentSwing = false;
    public Vec3 lastPos = Vec3.ZERO;
    public Vec3 lastMotion = Vec3.ZERO;
    public Vec3 lastVehicleMotion = Vec3.ZERO;
    public ItemStack lastMainHead = ItemStack.EMPTY;
    public ItemStack lastOffHead = ItemStack.EMPTY;
    public boolean lastSprint = false;
    public boolean lastSwing = false;
    public Vec2 currentRot = Vec2.ZERO;
    public Vec2 lastRot = Vec2.ZERO;
    @Range(from = 0, to = 19)
    public List<Vec3> posHistory = new ArrayList<>(20);
    @Range(from = 0, to = 19)
    public List<Vec3> motionHistory = new ArrayList<>(20);
    @Range(from = 0, to = 19)
    public List<Vec3> vehicleMotionHistory = new ArrayList<>(20);
    @Range(from = 0, to = 19)
    public List<Boolean> sprintHistory = new ArrayList<>(20);
    public Vec3 lastOnGroundPos = Vec3.ZERO;
    public Vec3 lastOnGroundPos2 = Vec3.ZERO;
    public Vec3 lastInLiquidPos = Vec3.ZERO;
    public Vec3 lastOnLiquidGroundPos = Vec3.ZERO;
    public boolean currentOnGround = true;
    public boolean lastOnGround = true;
    public boolean lastOnGround2 = true;
    public boolean hasSetback = false;
    public boolean jumping = false;
    public boolean lastUsingItem = false;
    public double speedMul = 1;
    public GameType currentGameType = GameType.SURVIVAL;
    public GameType lastGameType = GameType.SURVIVAL;
    public long upTime = 0;
    public int latency = 0;
    public float lastFallDistance = 0;
    public short offGroundTicks = 0;

    public PlayerData compatPlayerData = new PlayerData();

    public @NotNull ScheduledExecutorService timeTask = Executors.newScheduledThreadPool(1);

    public TRPlayer(AbstractClientPlayer player, boolean self) {
        this.fabricPlayer = player;
        this.manager = self ? CheckManager.createSelf((TRSelf) this) : CheckManager.create(this);

        currentPos = new Vec3(fabricPlayer.posX, fabricPlayer.posY, fabricPlayer.posZ);
        currentMotion = new Vec3(fabricPlayer.motionX, fabricPlayer.motionY, fabricPlayer.motionZ);
        currentVehicleMotion = Vec3.ZERO;
        currentMainHead = ItemStack.EMPTY;
        currentOffHead = currentMainHead;
        currentSprint = fabricPlayer.isSprinting();
        currentSwing = fabricPlayer.isSwingInProgress;
        currentRot = new Vec2(fabricPlayer.rotationPitch, fabricPlayer.rotationYaw);
        currentOnGround = lastOnGround = lastOnGround2 = fabricPlayer.onGround;
        currentGameType = lastGameType =
                fabricPlayer.isSpectator() ? GameType.SPECTATOR :
                        GameType.SURVIVAL;
        for (int i = 0; i < 20; i++) {
            posHistory.add(currentPos);
        }
        for (int i = 0; i < 20; i++) {
            motionHistory.add(currentMotion);
        }
        for (int i = 0; i < 20; i++) {
            vehicleMotionHistory.add(currentVehicleMotion);
        }
        for (int i = 0; i < 20; i++) {
            sprintHistory.add(currentSprint);
        }
    }

    @Contract("_ -> new")
    public static @NotNull TRPlayer create(@NotNull AbstractClientPlayer player) {
        return new TRPlayer(player, false);
    }

    public void update(AbstractClientPlayer player) {
        fabricPlayer = player;
        if (fabricPlayer == null) return;

        currentPos = new Vec3(fabricPlayer.posX, fabricPlayer.posY, fabricPlayer.posZ);
        currentMotion = new Vec3(fabricPlayer.motionX, fabricPlayer.motionY, fabricPlayer.motionZ);
        currentVehicleMotion = Vec3.ZERO;
        currentMainHead = ItemStack.convert(fabricPlayer.getHeldItem());
        currentOffHead = currentMainHead;
        currentSprint = fabricPlayer.isSprinting();
        currentSwing = fabricPlayer.isSwingInProgress;
        currentRot = new Vec2(fabricPlayer.rotationPitch, fabricPlayer.rotationYaw);
        currentOnGround = fabricPlayer.onGround;
        if (currentOnGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }
        speedMul = 1 * fabricPlayer.getAIMoveSpeed() * 10;
        fabricPlayer.getActivePotionEffects().stream()
                .filter(effect -> effect.getEffectName().toLowerCase().contains("speed"))
                .findAny()
                .ifPresent(effect -> speedMul = (effect.getAmplifier() * 0.2 + 1) * fabricPlayer.getAIMoveSpeed() * 10);
        // IDK why, but it just works!

        updateHistory();
        try {
//            final PlayerInfo playerInfo = Objects.requireNonNull(Objects.requireNonNull(CLIENT).getPlayerInfo(fabricPlayer.getUUID()));
            // bro idk how to coding with 1.8
            currentGameType = fabricPlayer.isSpectator() ? GameType.SPECTATOR :
                    GameType.SURVIVAL;
            latency = (int) Anticheat.getLatency().getInput();
        } catch (NullPointerException ignored) {
        }
        if (currentOnGround) {
            lastOnGroundPos2 = lastOnGroundPos;
            lastOnGroundPos = currentPos;
            jumping = false;
            if (fabricPlayer.isInWater())
                lastOnLiquidGroundPos = currentPos;
        }
        if (fabricPlayer.isInWater() || fabricPlayer.isInLava()) {
            lastInLiquidPos = currentPos;
        }

        compatPlayerData.update(fabricPlayer);

        manager.update();

        lastPos = currentPos;
        lastMotion = currentMotion;
        lastVehicleMotion = currentVehicleMotion;
        lastMainHead = currentMainHead;
        lastOffHead = currentOffHead;
        lastSprint = currentSprint;
        lastSwing = currentSwing;
        lastRot = currentRot;
        lastOnGround2 = lastOnGround;
        lastOnGround = currentOnGround;
        lastUsingItem = fabricPlayer.isUsingItem();
        lastGameType = currentGameType;
        lastFallDistance = fabricPlayer.fallDistance;
        upTime++;
        tryToClearVL();
    }

    private void updateHistory() {
        if (posHistory.size() >= 20) {
            posHistory.remove(posHistory.size() - 1);
        }
        posHistory.add(0, currentPos);

        if (motionHistory.size() >= 20) {
            motionHistory.remove(motionHistory.size() - 1);
        }
        motionHistory.add(0, currentMotion);

        if (vehicleMotionHistory.size() >= 20) {
            vehicleMotionHistory.remove(vehicleMotionHistory.size() - 1);
        }
        vehicleMotionHistory.add(0, currentVehicleMotion);

        if (sprintHistory.size() >= 20) {
            sprintHistory.remove(sprintHistory.size() - 1);
        }
        sprintHistory.add(0, currentSprint);
    }

    public void tryToClearVL() {
        if (upTime % Anticheat.getVlClearTime().getInput() == 0) {
            manager.onCustomAction(check -> check.violations = 0);
        }
    }
}
