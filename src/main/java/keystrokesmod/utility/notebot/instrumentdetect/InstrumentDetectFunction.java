/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package keystrokesmod.utility.notebot.instrumentdetect;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public interface InstrumentDetectFunction {
    /**
     * Detects an instrument for noteblock
     *
     * @param noteBlock Noteblock state
     * @param blockPos Noteblock position
     * @return Detected instrument
     */
    String detectInstrument(IBlockState noteBlock, BlockPos blockPos);
}
