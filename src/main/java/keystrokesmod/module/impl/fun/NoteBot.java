package keystrokesmod.module.impl.fun;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.notebot.NotebotUtils;
import keystrokesmod.utility.notebot.decoder.SongDecoder;
import keystrokesmod.utility.notebot.decoder.SongDecoders;
import keystrokesmod.utility.notebot.instrumentdetect.InstrumentDetectMode;
import keystrokesmod.utility.notebot.song.Note;
import keystrokesmod.utility.notebot.song.Song;
import lombok.Getter;
import lombok.var;
import net.minecraft.block.BlockNote;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NoteBot extends Module {
    public static String fileName = "";
    @Getter
    @Nullable
    private static NoteBot instance = null;
    private static File directory;
    public final ModeSetting mode;
    public final ButtonSetting roundOutOfRange;
    public final SliderSetting checkDelay;
    public final SliderSetting tickDelay;
    public final ModeSetting rotation;
    public final ButtonSetting lookView;
    public final SliderSetting concurrentTuneBlocks;
    public final ButtonSetting polyphonic;
    private final ButtonSetting debug;
    private final Map<Note, BlockPos> noteBlockPositions = new HashMap<>(); // Currently used noteblocks by the song
    private final Multimap<Note, BlockPos> scannedNoteblocks = MultimapBuilder.linkedHashKeys().arrayListValues().build(); // Found noteblocks
    private final List<BlockPos> clickedBlocks = new ArrayList<>();
    private final Map<BlockPos, Integer> tuneHits = new HashMap<>(); // noteblock -> target hits number
    private CompletableFuture<Song> loadingSongFuture = null;
    private Song song; // Loaded song
    private Stage stage = Stage.None;
    private PlayingMode playingMode = PlayingMode.None;
    private boolean isPlaying = false;
    private int currentTick = 0;
    private int ticks = 0;
    private boolean anyNoteblockTuned = false;
    private int waitTicks = -1;

    public NoteBot() {
        super("NoteBot", category.experimental);
        instance = this;

        this.registerSetting(new DescriptionSetting("Command: notebot <fileName>"));
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"AnyInstruments", "ExactInstruments"}, 1));
        this.registerSetting(roundOutOfRange = new ButtonSetting("Round out of range", false));
        this.registerSetting(checkDelay = new SliderSetting("Check delay", 10, 1, 20, 1, "tick"));
        this.registerSetting(tickDelay = new SliderSetting("Tick delay", 1, 1, 20, 1, "tick"));
        this.registerSetting(rotation = new ModeSetting("Rotation", new String[]{"None", "Block"}, 1));
        this.registerSetting(lookView = new ButtonSetting("Look view", false, new ModeOnly(rotation, 0).reserve()));
        this.registerSetting(concurrentTuneBlocks = new SliderSetting("Concurrent tune blocks", 1, 1, 20, 1));
        this.registerSetting(polyphonic = new ButtonSetting("Polyphonic", true));
        this.registerSetting(debug = new ButtonSetting("Debug", false));

        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "noteBot");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating clickPatterns directory.");
            }
        }
    }

    private static int calcNumberOfHits(int from, int to) {
        if (from > to) {
            return (25 - from) + to;
        } else {
            return to - from;
        }
    }

    @Override
    public void onEnable() {
        try {
            loadSong(new File(directory, fileName));
//                tune();
        } catch (NullPointerException ignored) {
        } catch (InvalidPathException | IndexOutOfBoundsException e) {
            error("Invalid file: " + fileName);
            stop();
        }
    }

    @Override
    public void onDisable() {
        stop();
    }

    @Override
    public void onUpdate() {
        ticks++;
        clickedBlocks.clear();

        if (stage == Stage.WaitingToCheckNoteblocks) {
            waitTicks--;
            if (waitTicks == 0) {
                waitTicks = -1;
                info("Checking noteblocks again...");

                setupTuneHitsMap();
                stage = Stage.Tune;
            }
        } else if (stage == Stage.SetUp) {
            scanForNoteblocks();
            if (scannedNoteblocks.isEmpty()) {
                error("Can't find any nearby noteblock!");
                stop();
                return;
            }

            setupNoteblocksMap();
            if (noteBlockPositions.isEmpty()) {
                error("Can't find any valid noteblock to play song.");
                stop();
                return;
            }
            setupTuneHitsMap();
            stage = Stage.Tune;
        } else if (stage == Stage.Tune) {
            tune();
        } else if (stage == Stage.Playing) {
            if (!isPlaying) return;

            if (mc.thePlayer == null || currentTick > song.getLastTick()) {
                // Stop the song after it is finished
                onSongEnd();
                return;
            }

            if (song.getNotesMap().containsKey(currentTick)) {
                if (mc.thePlayer.capabilities.isCreativeMode) {
                    error("You need to be in survival mode.");
                    stop();
                    return;
                } else onTickPlay();
            }

            currentTick++;
        }
    }

    /**
     * 为了兼容meteor代码
     */
    public void info(String msg1) {
        Notifications.sendNotification(Notifications.NotificationTypes.INFO, msg1);
    }

    public void info(String msg1, String msg2) {
        info(String.format(msg1, msg2));
    }

    /**
     * 为了兼容meteor代码
     */
    public void warning(String msg1, int msg2) {
        if (!debug.isToggled()) return;
        warning(String.format(msg1, msg2));
    }

    public void warning(String msg1) {
        if (!debug.isToggled()) return;
        Notifications.sendNotification(Notifications.NotificationTypes.WARN, msg1);
    }

    /**
     * 为了兼容meteor代码
     */
    public void error(String msg1) {
        Notifications.sendNotification(Notifications.NotificationTypes.ERROR, msg1);
    }

    /**
     * Gets an Instrument from Note Map
     *
     * @param inst An instrument
     * @return A new instrument mapped by instrument given in parameters
     */
    @Nullable
    public String getMappedInstrument(@NotNull String inst) {
        if ((int) mode.getInput() == NotebotUtils.NotebotMode.ExactInstruments.ordinal()) {
            NotebotUtils.OptionalInstrument optionalInstrument = NotebotUtils.OptionalInstrument.None;  // TODO 尚未实现
            return optionalInstrument.toMinecraftInstrument();
        } else {
            return inst;
        }
    }

    /**
     * Loads and plays song
     *
     * @param file Song supported by one of {@link SongDecoder}
     */
    public void loadSong(File file) {
        resetVariables();

        this.playingMode = PlayingMode.Noteblocks;
        if (!loadFileToMap(file, () -> stage = Stage.SetUp)) {
            onSongEnd();
        }
    }

    /**
     * Tunes noteblocks. This method is called per tick.
     */
    private void tune() {
        if (tuneHits.isEmpty()) {
            if (anyNoteblockTuned) {
                anyNoteblockTuned = false;
                waitTicks = (int) checkDelay.getInput();
                stage = Stage.WaitingToCheckNoteblocks;

                info("Delaying check for noteblocks");
            } else {
                stage = Stage.Playing;
                info("Loading done.");
                play();
            }
            return;
        }

        if (ticks < (int) tickDelay.getInput()) {
            return;
        }

        tuneBlocks();
        ticks = 0;
    }

    private void tuneBlocks() {
        if (!Utils.nullCheck()) {
            disable();
        }

        mc.thePlayer.swingItem();

        int iterations = 0;
        var iterator = tuneHits.entrySet().iterator();

        // Concurrent tuning :o
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockPos pos = entry.getKey();
            int hitsNumber = entry.getValue();

            if (rotation.getInput() != 0)
                RotationHandler.setRotationYaw(PlayerRotation.getYaw(pos));
            RotationHandler.setRotationPitch(PlayerRotation.getPitch(pos));
            if (lookView.isToggled()) {
                mc.thePlayer.rotationYaw = PlayerRotation.getYaw(pos);
                mc.thePlayer.rotationPitch = PlayerRotation.getPitch(pos);
            }
            this.tuneNoteblockWithPackets(pos);

            clickedBlocks.add(pos);

            hitsNumber--;
            entry.setValue(hitsNumber);

            if (hitsNumber == 0) {
                iterator.remove();
            }

            iterations++;

            if (iterations == (int) concurrentTuneBlocks.getInput()) return;
        }
    }

    private void tuneNoteblockWithPackets(BlockPos pos) {
        // We don't need to raycast here. Server handles this packet fine
        mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld,
                SlotHandler.getHeldItem(),
                pos, EnumFacing.DOWN, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
        );

        anyNoteblockTuned = true;
    }

    /**
     * Plays a song after loading and tuning
     */
    public void play() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.capabilities.isCreativeMode && playingMode != PlayingMode.Preview) {
            error("You need to be in survival mode.");
        } else if (stage == Stage.Playing) {
            isPlaying = true;
            info("Playing.");
        } else {
            error("No song loaded.");
        }
    }

    /**
     * Loads and plays song directly
     *
     * @param file     Song supported by one of {@link SongDecoder}
     * @param callback Callback that is run when song has been loaded
     * @return Success
     */
    public boolean loadFileToMap(File file, Runnable callback) {
        if (!file.exists() || !file.isFile()) {
            error("File not found");
            return false;
        }

        if (!SongDecoders.hasDecoder(file)) {
            error("File is in wrong format. Decoder not found.");
            return false;
        }

        info("Loading song \"%s\".", FilenameUtils.getBaseName(file.getName()));

        // Start loading song
        long time1 = System.currentTimeMillis();
        Raven.getExecutor().execute(() -> {
            try {
                this.song = SongDecoders.parse(file);
                long time2 = System.currentTimeMillis();
                long diff = time2 - time1;

                info("Song '" + FilenameUtils.getBaseName(file.getName()) + "' has been loaded to the memory! Took " + diff + "ms");
                callback.run();
            } catch (Exception e) {
                if (e instanceof CancellationException) {
                    error("Loading song '" + FilenameUtils.getBaseName(file.getName()) + "' was cancelled.");
                } else {
                    error("An error occurred while loading song '" + FilenameUtils.getBaseName(file.getName()) + "'. See the logs for more details");
                    onSongEnd();
                }
            }
        });

        stage = Stage.LoadingSong;
        return true;
    }

    public void onSongEnd() {
        stop();
    }

    public void stop() {
        if (isEnabled()) info("Stopping.");
        disable();
    }

    private void resetVariables() {
        if (loadingSongFuture != null) {
            loadingSongFuture.cancel(true);
            loadingSongFuture = null;
        }
        clickedBlocks.clear();
        tuneHits.clear();
        anyNoteblockTuned = false;
        currentTick = 0;
        playingMode = PlayingMode.None;
        isPlaying = false;
        stage = Stage.None;
        song = null;
        noteBlockPositions.clear();
    }

    /**
     * Set up a tune hits map which tells how many times player needs to
     * hit noteblock to obtain desired note level
     */
    private void setupTuneHitsMap() {
        if (!Utils.nullCheck()) return;
        tuneHits.clear();

        for (var entry : noteBlockPositions.entrySet()) {
            int targetLevel = entry.getKey().getNoteLevel();
            BlockPos blockPos = entry.getValue();

            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            int currentLevel = blockState.getBlock().getMetaFromState(blockState) & 15;

            if (targetLevel != currentLevel) {
                tuneHits.put(blockPos, calcNumberOfHits(currentLevel, targetLevel));
            }
        }
    }

    /**
     * Scans noteblocks nearby and adds them to the map
     */
    private void scanForNoteblocks() {
        if (mc.playerController == null || mc.theWorld == null || mc.thePlayer == null) return;
        scannedNoteblocks.clear();
        int min = (int) (-mc.playerController.getBlockReachDistance()) - 2;
        int max = (int) mc.playerController.getBlockReachDistance() + 2;

        // Scan for noteblocks horizontally
        // 6^3 kek
        for (int y = min; y < max; y++) {
            for (int x = min; x < max; x++) {
                for (int z = min; z < max; z++) {
                    BlockPos pos = new BlockPos(mc.thePlayer).add(x, y + 1, z);

                    IBlockState blockState = mc.theWorld.getBlockState(pos);
                    if (blockState.getBlock() != Blocks.noteblock) continue;

                    // Copied from ServerPlayNetworkHandler#onPlayerInteractBlock
                    Vec3 vec3d2 = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    double sqDist = Utils.getEyePos().distanceToSq(new keystrokesmod.script.classes.Vec3(vec3d2));
                    if (sqDist > MathHelper.sqrt_double(6.0)) continue;

                    if (!isValidScanSpot(pos)) continue;

                    Note note = NotebotUtils.getNoteFromNoteBlock(blockState, pos, NotebotUtils.NotebotMode.values()[(int) mode.getInput()], InstrumentDetectMode.BlockState.getInstrumentDetectFunction());
                    scannedNoteblocks.put(note, pos);
                }
            }

        }
    }

    private void onTickPlay() {
        Collection<Note> notes = song.getNotesMap().get(this.currentTick);
        if (!notes.isEmpty()) {

            // Rotate player's head
            if (rotation.getInput() != 0) {
                Optional<Note> firstNote = notes.stream().findFirst();
                if (!firstNote.isPresent()) return;
                BlockPos firstPos = noteBlockPositions.get(firstNote.get());

                if (firstPos != null) {
                    if (rotation.getInput() == 1) {
                        RotationHandler.setRotationYaw(PlayerRotation.getYaw(firstPos));
                        RotationHandler.setRotationPitch(PlayerRotation.getPitch(firstPos));
                    } else {
                        mc.thePlayer.rotationYaw = PlayerRotation.getYaw(firstPos);
                        mc.thePlayer.rotationPitch = PlayerRotation.getPitch(firstPos);
                    }
                }
            }

            // Swing arm
            mc.thePlayer.swingItem();

            // Play notes
            for (Note note : notes) {
                BlockPos pos = noteBlockPositions.get(note);
                if (pos == null) {
                    return;
                }

                playRotate(pos);
            }
        }
    }

    private void playRotate(BlockPos pos) {
        try {
            mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN);
        } catch (NullPointerException ignored) {
        }
    }

    private boolean isValidScanSpot(BlockPos pos) {
        if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockNote)) return false;
        IBlockState state = mc.theWorld.getBlockState(pos.up());
        return state.getBlock() == Blocks.air;
    }

    /**
     * Set up a map of noteblocks positions
     */
    private void setupNoteblocksMap() {
        noteBlockPositions.clear();

        // Modifiable list of unique notes
        List<Note> uniqueNotesToUse = new ArrayList<>(song.getRequirements());
        // A map with noteblocks that have incorrect note level
        Map<String, List<BlockPos>> incorrectNoteBlocks = new HashMap<>();

        // Check if there are already tuned noteblocks
        for (var entry : scannedNoteblocks.asMap().entrySet()) {
            Note note = entry.getKey();
            List<BlockPos> noteblocks = new ArrayList<>(entry.getValue());

            if (uniqueNotesToUse.contains(note)) {
                // Add correct noteblock position to a noteBlockPositions
                noteBlockPositions.put(note, noteblocks.remove(0));
                uniqueNotesToUse.remove(note);
            }

            if (!noteblocks.isEmpty()) {
                // Add excess noteblocks for mapping process [note -> block pos]

                if (!incorrectNoteBlocks.containsKey(note.getInstrument())) {
                    incorrectNoteBlocks.put(note.getInstrument(), new ArrayList<>());
                }

                incorrectNoteBlocks.get(note.getInstrument()).addAll(noteblocks);
            }
        }

        // Map [note -> block pos]
        for (var entry : incorrectNoteBlocks.entrySet()) {
            List<BlockPos> positions = entry.getValue();

            if ((int) mode.getInput() == NotebotUtils.NotebotMode.ExactInstruments.ordinal()) {
                String inst = entry.getKey();

                List<Note> foundNotes = uniqueNotesToUse.stream()
                        .filter(note -> Objects.equals(note.getInstrument(), inst))
                        .collect(Collectors.toList());

                if (foundNotes.isEmpty()) continue;

                for (BlockPos pos : positions) {
                    if (foundNotes.isEmpty()) break;

                    Note note = foundNotes.remove(0);
                    noteBlockPositions.put(note, pos);

                    uniqueNotesToUse.remove(note);
                }
            } else {
                for (BlockPos pos : positions) {
                    if (uniqueNotesToUse.isEmpty()) break;

                    Note note = uniqueNotesToUse.remove(0);
                    noteBlockPositions.put(note, pos);
                }
            }
        }

        if (!uniqueNotesToUse.isEmpty()) {
            for (Note note : uniqueNotesToUse) {
                warning("Missing note: " + note.getInstrument() + ", " + note.getNoteLevel());
            }
            warning(uniqueNotesToUse.size() + " missing notes!");
        }
    }

    public enum Stage {
        None,
        LoadingSong,
        SetUp,
        Tune,
        WaitingToCheckNoteblocks,
        Playing
    }

    public enum PlayingMode {
        None,
        Preview,
        Noteblocks
    }
}
