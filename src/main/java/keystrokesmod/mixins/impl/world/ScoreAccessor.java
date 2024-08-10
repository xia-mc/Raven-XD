package keystrokesmod.mixins.impl.world;


import net.minecraft.scoreboard.Score;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Score.class)
public interface ScoreAccessor {

    @Accessor("scorePlayerName")
    void setScorePlayerName(String playerName);
}
