package keystrokesmod.module.impl.other.anticheats.checks.simulation;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.utils.phys.PredictEngine;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Simulation extends Check {
    public static final Set<MovementInput> MOVEMENTS = new ObjectArraySet<>();
    public static final boolean[] BOOLEANS = {true, false};

    static {
        for (float forward = -1; forward <= 1; forward++) {
            for (float strafe = -1; strafe <= 1; strafe++) {
                for (boolean jump : BOOLEANS) {
                    for (boolean sneak : BOOLEANS) {
                        MovementInput input = new MovementInput();
                        input.moveForward = forward;
                        input.moveStrafe = strafe;
                        input.jump = jump;
                        input.sneak = sneak;

                        if (input.sneak) {
                            input.moveStrafe = (float) ((double) input.moveStrafe * 0.3);
                            input.moveForward = (float) ((double) input.moveForward * 0.3);
                        }

                        MOVEMENTS.add(input);
                    }
                }
            }
        }
    }

    private final PredictEngine engine;
    private final Queue<Vec3> nextPosition = new ConcurrentLinkedQueue<>();

    public Simulation(@NotNull TRPlayer player) {
        super("Simulation", player);
        this.engine = new PredictEngine(player);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (!nextPosition.isEmpty()) {
                Vec3 nearestVec = nextPosition.poll();
                double nearestDist = nearestVec.distanceTo(player.fabricPlayer);

                while (!nextPosition.isEmpty()) {
                    final Vec3 curVec = nextPosition.poll();
                    final double curDist = curVec.distanceTo(player.fabricPlayer);

                    if (curDist < nearestDist) {
                        nearestVec = curVec;
                        nearestDist = curDist;
                    }
                }

                if (nearestVec.distanceTo(player.fabricPlayer) > Anticheat.getThreshold().getInput()) {
                    flag(String.format(
                            "x:%.4f y:%.4f z:%.4f",
                            player.fabricPlayer.posX - nearestVec.x,
                            player.fabricPlayer.posY - nearestVec.y,
                            player.fabricPlayer.posZ - nearestVec.z
                    ));
                } else {
                    customMsg(String.format(
                            "x:%.4f y:%.4f z:%.4f",
                            player.fabricPlayer.posX - nearestVec.x,
                            player.fabricPlayer.posY - nearestVec.y,
                            player.fabricPlayer.posZ - nearestVec.z
                    ));
                }
            }

            // prediction
            engine.sync();
            nextPosition.clear();
            for (MovementInput movement : MOVEMENTS) {
                for (boolean sprint : BOOLEANS) {
                    PredictEngine predict = engine.clone();

                    predict.setMovementInput(movement);
                    predict.setSprinting(sprint);

                    predict.onUpdate();

                    nextPosition.add(new Vec3(predict));
                }
            }
        } catch (Throwable e) {
            customMsg(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public int getAlertBuffer() {
        return 10;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getSimulationCheck().isToggled();
    }
}
