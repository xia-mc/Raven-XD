package keystrokesmod.mixins.impl.world;


import net.minecraft.block.BlockNote;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BlockNote.class)
public interface BlockNoteAccessor {

    @Invoker("getInstrument")
    String getInstrument(int type);
}
