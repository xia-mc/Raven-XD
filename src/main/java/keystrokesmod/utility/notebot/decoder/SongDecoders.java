/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package keystrokesmod.utility.notebot.decoder;

import keystrokesmod.module.impl.fun.NoteBot;
import keystrokesmod.utility.notebot.NotebotUtils;
import keystrokesmod.utility.notebot.song.Note;
import keystrokesmod.utility.notebot.song.Song;
import lombok.var;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SongDecoders {
    private static final Map<String, SongDecoder> decoders = new HashMap<>(); // file extension -> song decoder

    static {
        registerDecoder("nbs", new NBSSongDecoder());
        registerDecoder("txt", new TextSongDecoder());
        // TODO Maybe a midi decoder in the future
    }

    public static void registerDecoder(String extension, SongDecoder songDecoder) {
        decoders.put(extension, songDecoder);
    }

    public static SongDecoder getDecoder(File file) {
        return decoders.get(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean hasDecoder(File file) {
        return decoders.containsKey(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean hasDecoder(Path path) {
        return hasDecoder(path.toFile());
    }

    /**
     * Parse file to one of {@link SongDecoder}
     *
     * @param file A song file
     * @return A {@link Song} object
     */
    @NotNull
    public static Song parse(File file) throws Exception {
        if (!hasDecoder(file)) throw new IllegalStateException("Decoder for this file does not exists!");
        SongDecoder decoder = getDecoder(file);
        Song song = decoder.parse(file);

        fixSong(song);

        song.finishLoading();

        return song;
    }

    /**
     * This method adapts {@link Song} to settings in Notebot module
     *
     * @param song A song
     */
    private static void fixSong(Song song) {
        NoteBot notebot = NoteBot.getInstance();
        if (notebot == null) return;

        var iterator = song.getNotesMap().entries().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int tick = entry.getKey();
            Note note = entry.getValue();

            int n = note.getNoteLevel();
            if (n < 0 || n > 24) {
                if (notebot.roundOutOfRange.isToggled()) {
                    note.setNoteLevel(n < 0 ? 0 : 24);
                } else {
                    notebot.warning("Note at tick %d out of range.", tick);
                    iterator.remove();
                    continue;
                }
            }

            if (notebot.mode.getInput() == NotebotUtils.NotebotMode.ExactInstruments.ordinal()) {
                String newInstrument = notebot.getMappedInstrument(note.getInstrument());
                if (newInstrument != null) {
                    note.setInstrument(newInstrument);
                }
            } else {
                note.setInstrument(null);
            }
        }
    }
}
