/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package keystrokesmod.utility.notebot.decoder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import keystrokesmod.utility.notebot.song.Note;
import keystrokesmod.utility.notebot.song.Song;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

// https://github.com/koca2000/NoteBlockAPI/blob/master/src/main/java/com/xxmicloxx/NoteBlockAPI/utils/NBSDecoder.java

/**
 * Utils for reading Note Block Studio data
 *
 */
public class NBSSongDecoder extends SongDecoder {

    public static final int NOTE_OFFSET = 33; // Magic value (https://opennbs.org/nbs)

    /**
     * Parses a Song from a Note Block Studio project file (.nbs)
     * @see Song
     * @param songFile .nbs file
     * @return Song object representing a Note Block Studio project
     */
    @Override
    @NotNull
    public Song parse(File songFile) throws Exception {
        return parse(new FileInputStream(songFile));
    }

    /**
     * Parses a Song from an InputStream and a Note Block Studio project file (.nbs)
     * @see Song
     * @param inputStream of a .nbs file
     * @return Song object representing the given .nbs file
     */
    @NotNull
    private Song parse(InputStream inputStream) throws Exception {
        Multimap<Integer, Note> notesMap = MultimapBuilder.linkedHashKeys().arrayListValues().build();

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        short length = readShort(dataInputStream);
        int nbsversion = 0;
        if (length == 0) {
            nbsversion = dataInputStream.readByte();
            dataInputStream.readByte(); // first custom instrument
            if (nbsversion >= 3) {
                readShort(dataInputStream);
            }
        }
        readShort(dataInputStream); // Song Height
        String title = readString(dataInputStream);
        String author = readString(dataInputStream);
        readString(dataInputStream); // original author
        readString(dataInputStream); // description
        float speed = readShort(dataInputStream) / 100f;
        dataInputStream.readBoolean(); // auto-save
        dataInputStream.readByte(); // auto-save duration
        dataInputStream.readByte(); // x/4ths, time signature
        readInt(dataInputStream); // minutes spent on project
        readInt(dataInputStream); // left clicks (why?)
        readInt(dataInputStream); // right clicks (why?)
        readInt(dataInputStream); // blocks added
        readInt(dataInputStream); // blocks removed
        readString(dataInputStream); // .mid/.schematic file name
        if (nbsversion >= 4) {
            dataInputStream.readByte(); // loop on/off
            dataInputStream.readByte(); // max loop count
            readShort(dataInputStream); // loop start tick
        }

        double tick = -1;
        while (true) {
            short jumpTicks = readShort(dataInputStream); // jumps till next tick
            //System.out.println("Jumps to next tick: " + jumpTicks);
            if (jumpTicks == 0) {
                break;
            }
            tick += jumpTicks * (20f / speed);
            //System.out.println("Tick: " + tick);
            short layer = -1;
            while (true) {
                short jumpLayers = readShort(dataInputStream); // jumps till next layer
                if (jumpLayers == 0) {
                    break;
                }
                layer += jumpLayers;
                //System.out.println("Layer: " + layer);
                byte instrument = dataInputStream.readByte();

                byte key = dataInputStream.readByte();
                if (nbsversion >= 4) {
                    dataInputStream.readUnsignedByte(); // note block velocity
                    dataInputStream.readUnsignedByte(); // note panning, 0 is right in nbs format
                    readShort(dataInputStream); // note block pitch
                }

                String inst = fromNBSInstrument(instrument);

                // Probably a custom instrument. Ignore this note
                if (inst == null) continue;

                Note note = new Note(inst /* instrument */, key - NOTE_OFFSET /* note */);
                setNote((int) Math.round(tick), note, notesMap);
            }
        }

        return new Song(notesMap, title, author);
    }

    /**
     * Sets a note at a tick in a song
     * @param ticks
     * @param note
     * @param notesMap
     */
    private static void setNote(int ticks, Note note, Multimap<Integer, Note> notesMap) {
        notesMap.put(ticks, note);
    }

    private static short readShort(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        return (short) (byte1 + (byte2 << 8));
    }

    private static int readInt(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        int byte3 = dataInputStream.readUnsignedByte();
        int byte4 = dataInputStream.readUnsignedByte();
        return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
    }

    private static String readString(DataInputStream dataInputStream) throws IOException {
        int length = readInt(dataInputStream);
        if (length < 0) {
            throw new EOFException("Length can't be negative! Length: " + length);
        }
        if (length > dataInputStream.available()) {
            throw new EOFException("Can't read string that is larger than a buffer! Length: " + length + " Readable Bytes Length: " + dataInputStream.available());
        }

        StringBuilder builder = new StringBuilder(length);
        for (; length > 0; --length) {
            char c = (char) dataInputStream.readByte();
            if (c == (char) 0x0D) {
                c = ' ';
            }
            builder.append(c);
        }
        return builder.toString();
    }

    // Magic Values (https://opennbs.org/nbs)
    @Contract(pure = true)
    private static @Nullable String fromNBSInstrument(int instrument) {
        switch (instrument) {
            case 0 : return "harp";
            case 1 : return "bassattack";
            case 2 : return "bd";
            case 3 : return "snare";
            case 4 : return "hat";
            default : return null;
        }
    }

}
