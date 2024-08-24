package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelNoGroundNoFall extends SubMode<NoFall> {
    private final SliderSetting minFallDistance;
    private final ButtonSetting autoJump;
    private final ButtonSetting notWhileBreaking;

    private State state = State.NONE;

    public HypixelNoGroundNoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3.0, 0.0, 8.0, 0.1));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
        this.registerSetting(notWhileBreaking = new ButtonSetting("Not while breaking", false));
    }

    @Override
    public void onEnable() {
        state = State.NONE;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance >= minFallDistance.getInput()) {
            state = State.FALL;
        }

        if (notWhileBreaking.isToggled() && ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock())
            state = State.NONE;

        if (state != State.NONE)
            event.setOnGround(false);

        switch (state) {
            case FALL:
                if (mc.thePlayer.onGround) {
                    state = State.RELEASE_PRE;
                    if (autoJump.isToggled() && !Utils.jumpDown())
                        mc.thePlayer.jump();
                }
                break;
            case RELEASE_PRE:
                if (!mc.thePlayer.onGround)
                    state = State.RELEASE_POST;
                break;
            case RELEASE_POST:
                if (mc.thePlayer.onGround)
                    state = State.NONE;
                break;
        }
    }


    enum State {
        NONE,
        FALL,
        RELEASE_PRE,
        RELEASE_POST
    }
}
