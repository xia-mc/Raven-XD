package keystrokesmod.mixins.impl.client;


import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PostPlayerInputEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {

    @Shadow @Final private GameSettings gameSettings;

    @Unique private double raven_XD$sneakMultiplier = 0.3;

    @Inject(method = "updatePlayerMoveState", at = @At(value = "FIELD", target = "net/minecraft/util/MovementInputFromOptions.sneak:Z", shift = At.Shift.AFTER))
    public void onPrePlayerMove(CallbackInfo ci) {
        final MoveInputEvent moveInputEvent = new MoveInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);

        MinecraftForge.EVENT_BUS.post(moveInputEvent);

        this.raven_XD$sneakMultiplier = moveInputEvent.getSneakSlowDownMultiplier();
        this.moveForward = moveInputEvent.getForward();
        this.moveStrafe = moveInputEvent.getStrafe();
        this.jump = moveInputEvent.isJump();
        this.sneak = moveInputEvent.isSneak();
    }

    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    public void onPostPlayerMove(CallbackInfo ci) {
        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * raven_XD$sneakMultiplier);
            this.moveForward = (float) ((double) this.moveForward * raven_XD$sneakMultiplier);
        }

        MinecraftForge.EVENT_BUS.post(new PostPlayerInputEvent());
    }
}
