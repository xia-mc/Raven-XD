/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package keystrokesmod.utility.notebot.song;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class Note {

    private String instrument;
    private int noteLevel;

    public Note(String instrument, int noteLevel) {
        this.instrument = instrument;
        this.noteLevel = noteLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(instrument, note.instrument) && noteLevel == note.noteLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, noteLevel);
    }

    @Override
    public String toString() {
        return "Note{" +
            "instrument=" + getInstrument() +
            ", noteLevel=" + getNoteLevel() +
            '}';
    }
}
