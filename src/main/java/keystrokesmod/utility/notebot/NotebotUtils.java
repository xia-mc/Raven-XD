/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package keystrokesmod.utility.notebot;

import keystrokesmod.utility.notebot.instrumentdetect.InstrumentDetectFunction;
import keystrokesmod.utility.notebot.song.Note;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NotebotUtils {

    @Contract("_, _, _, _ -> new")
    public static @NotNull Note getNoteFromNoteBlock(@NotNull IBlockState noteBlock, BlockPos blockPos, NotebotMode mode, InstrumentDetectFunction instrumentDetectFunction) {
        String instrument = null;
        int level = noteBlock.getBlock().getMetaFromState(noteBlock) & 15;
        if (mode == NotebotMode.ExactInstruments) {
            instrument = instrumentDetectFunction.detectInstrument(noteBlock, blockPos);
        }

        return new Note(instrument, level);
    }

    public enum NotebotMode {
        AnyInstrument, ExactInstruments
    }

    public enum OptionalInstrument {
        None(null),
        Harp("harp"),
        Basedrum("bd"),
        Snare("snare"),
        Hat("hat"),
        Bass("bassattack");

        public static final Map<String, OptionalInstrument> BY_MINECRAFT_INSTRUMENT = new HashMap<>();

        static {
            for (OptionalInstrument optionalInstrument : values()) {
                BY_MINECRAFT_INSTRUMENT.put(optionalInstrument.minecraftInstrument, optionalInstrument);
            }
        }

        private final String minecraftInstrument;

        OptionalInstrument(@Nullable String minecraftInstrument) {
            this.minecraftInstrument = minecraftInstrument;
        }

        public String toMinecraftInstrument() {
            return minecraftInstrument;
        }

        public static OptionalInstrument fromMinecraftInstrument(String instrument) {
            if (instrument != null) {
                return BY_MINECRAFT_INSTRUMENT.get(instrument);
            } else {
                return null;
            }
        }
    }
}
