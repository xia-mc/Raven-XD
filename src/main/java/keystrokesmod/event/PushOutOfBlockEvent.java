package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Cancelable
@AllArgsConstructor
public class PushOutOfBlockEvent extends Event {
    private @NotNull Direction direction;
    private float pushMotion;

    public enum Direction {
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z;

        @Contract("_ -> new")
        public @NotNull BlockPos offsetWith(@NotNull BlockPos blockPos) {
            switch (this) {
                case POSITIVE_X:
                    return new BlockPos(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ());
                case NEGATIVE_X:
                    return new BlockPos(blockPos.getX() - 1, blockPos.getY(), blockPos.getZ());
                case POSITIVE_Z:
                    return new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1);
                case NEGATIVE_Z:
                    return new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() - 1);
                default:
                    throw new RuntimeException("Invalid direction.");
            }
        }
    }
}
