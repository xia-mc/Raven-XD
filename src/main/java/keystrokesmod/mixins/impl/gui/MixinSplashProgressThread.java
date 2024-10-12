package keystrokesmod.mixins.impl.gui;


import keystrokesmod.utility.Reflection;
import net.minecraftforge.fml.client.SplashProgress;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraftforge/fml/client/SplashProgress$3", remap = false)
public abstract class MixinSplashProgressThread {

    /**
     * To make a custom loading logo rendering fine.
     * <p>
     * Codes:
     * <p>
     * SplashProgress.logoTexture.texCoord(0, 0.0F, 0.0F);
     * <p>
     * GL11.glVertex2f(64.0F, -16.0F);
     * <p>
     * SplashProgress.logoTexture.texCoord(0, 0.0F, 1.0F);
     * <p>
     * GL11.glVertex2f(64.0F, 496.0F);
     * <p>
     * SplashProgress.logoTexture.texCoord(0, 1.0F, 1.0F);
     * <p>
     * GL11.glVertex2f(576.0F, 496.0F);
     * <p>
     * SplashProgress.logoTexture.texCoord(0, 1.0F, 0.0F);
     * <p>
     * GL11.glVertex2f(576.0F, -16.0F);
     * @see "Lnet/minecraftforge/fml/client/SplashProgress$3;run()V"
     */
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glVertex2f(FF)V"))
    private void onGlVertex2f(float x, float y) {
        final short type;
        if (x == 64.0F && y == -16.0F) {
            type = 0;
        } else if (x == 64.0F && y == 496.0F) {
            type = 1;
        } else if (x == 576.0F && y == 496.0F) {
            type = 2;
        } else if (x == 576.0F && y == -16.0F) {
            type = 3;
        } else {
            GL11.glVertex2f(x, y);
            return;
        }

        final Object texture = Reflection.getDeclared(SplashProgress.class, "logoTexture");
        final float width = (float) Reflection.call(texture, "getWidth") / 2.0F / 2.0F;
        final float height = (float) Reflection.call(texture, "getHeight") / 2.0F / 2.0F;

        switch (type) {
            case 0:
                GL11.glVertex2f(-width, -height);
                break;
            case 1:
                GL11.glVertex2f(-width, height);
                break;
            case 2:
                GL11.glVertex2f(width, height);
                break;
            case 3:
                GL11.glVertex2f(width, -height);
                break;
        }
    }
}
