
package com.angelfx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AngelFXLyrics {

    private static final Pattern TIMESTAMP =
            Pattern.compile("\\[(\\d+):(\\d{2})(?:[.:](\\d{1,3}))?\\]");

    private static final Pattern METADATA =
            Pattern.compile("\\[([a-zA-Z]+):([^\\]]*)\\]");

    private static Path lyricsDirectory;
    private static Path loadedFile;

    private static List<LyricLine> lines = List.of();

    private static String title = "Sin canción";
    private static String artist = "";

    private static boolean playing = false;
    private static long playbackPositionMs = 0;
    private static long startTimeNanos = 0;

    private AngelFXLyrics() {
    }

    /**
     * Creates config/angelfx/lyrics and its instructions file.
     */
    public static void initialize(Path configDirectory) throws IOException {
        lyricsDirectory = configDirectory
                .resolve("angelfx")
                .resolve("lyrics");

        Files.createDirectories(lyricsDirectory);

        Path instructions = lyricsDirectory.resolve("LEEME.txt");

        if (!Files.exists(instructions)) {
            String guide = """
                    ANGELFX LYRICS

                    Coloca tus canciones .lrc en esta carpeta.

                    Ejemplo de formato:

                    [ti:Mi canción]
                    [ar:Artista]
                    [00:12.50]Primera línea de la canción
                    [00:16.00]Segunda línea de la canción
                    [00:20.25]Tercera línea de la canción

                    Las marcas de tiempo indican cuándo aparece cada frase.
                    """;

            Files.writeString(instructions, guide, StandardCharsets.UTF_8);
        }
    }

    public static Path getLyricsDirectory() {
        return lyricsDirectory;
    }

    /**
     * Returns all .lrc files found in the lyrics directory.
     */
    public static List<Path> getAvailableFiles() throws IOException {
        if (lyricsDirectory == null || !Files.exists(lyricsDirectory)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(lyricsDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".lrc"))
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString(),
                            String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
    }

    /**
     * Loads the first .lrc file alphabetically.
     */
    public static boolean loadFirstAvailable() throws IOException {
        List<Path> files = getAvailableFiles();

        if (files.isEmpty()) {
            return false;
        }

        load(files.get(0));
        return true;
    }

    /**
     * Loads and parses an LRC file.
     */
    public static void load(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);

        List<LyricLine> parsedLines = new ArrayList<>();

        String newTitle = "Sin canción";
        String newArtist = "";
        long offsetMs = 0;

        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            Matcher metadata = METADATA.matcher(line);

            while (metadata.find()) {
                String key = metadata.group(1).toLowerCase(Locale.ROOT);
                String value = metadata.group(2).trim();

                switch (key) {
                    case "ti" -> newTitle = value;
                    case "ar" -> newArtist = value;
                    case "offset" -> {
                        try {
                            offsetMs = Long.parseLong(value);
                        } catch (NumberFormatException ignored) {
                            offsetMs = 0;
                        }
                    }
                    default -> {
                        // Other LRC metadata is ignored.
                    }
                }
            }

            Matcher timestamps = TIMESTAMP.matcher(line);
            List<Long> times = new ArrayList<>();
            int lastTimestampEnd = 0;

            while (timestamps.find()) {
                long minutes = Long.parseLong(timestamps.group(1));
                long seconds = Long.parseLong(timestamps.group(2));

                String fraction = timestamps.group(3);
                long milliseconds = 0;

                if (fraction != null) {
                    milliseconds = Long.parseLong(fraction);

                    if (fraction.length() == 1) {
                        milliseconds *= 100;
                    } else if (fraction.length() == 2) {
                        milliseconds *= 10;
                    }
                }

                long time = minutes * 60_000L
                        + seconds * 1_000L
                        + milliseconds;

                times.add(time);
                lastTimestampEnd = timestamps.end();
            }

            if (times.isEmpty()) {
                continue;
            }

            String lyricText = line.substring(lastTimestampEnd).trim();

            if (lyricText.isEmpty()) {
                continue;
            }

            for (long time : times) {
                parsedLines.add(new LyricLine(
                        Math.max(0, time + offsetMs),
                        lyricText
                ));
            }
        }

        parsedLines.sort(Comparator.comparingLong(LyricLine::timeMs));

        lines = List.copyOf(parsedLines);
        title = newTitle;
        artist = newArtist;
        loadedFile = file.toAbsolutePath().normalize();

        stopPlayback();
    }

    public static void reload() throws IOException {
        if (loadedFile != null && Files.exists(loadedFile)) {
            load(loadedFile);
        }
    }

    public static boolean hasLyrics() {
        return !lines.isEmpty();
    }

    public static List<LyricLine> getLines() {
        return lines;
    }

    public static String getTitle() {
        return title;
    }

    public static String getArtist() {
        return artist;
    }

    public static Path getLoadedFile() {
        return loadedFile;
    }

    /**
     * Starts the LRC playback clock.
     * It is not connected to system audio yet.
     */
    public static void startPlayback() {
        if (lines.isEmpty()) {
            return;
        }

        if (playbackPositionMs >= getDurationMs()) {
            playbackPositionMs = 0;
        }

        startTimeNanos = System.nanoTime()
                - playbackPositionMs * 1_000_000L;

        playing = true;
    }

    public static void pausePlayback() {
        if (playing) {
            playbackPositionMs = getPlaybackPositionMs();
            playing = false;
        }
    }

    public static void stopPlayback() {
        playing = false;
        playbackPositionMs = 0;
        startTimeNanos = 0;
    }

    public static void togglePlayback() {
        if (playing) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static long getPlaybackPositionMs() {
        if (!playing) {
            return playbackPositionMs;
        }

        return Math.max(0,
                (System.nanoTime() - startTimeNanos) / 1_000_000L);
    }

    public static void setPlaybackPositionMs(long positionMs) {
        long duration = getDurationMs();

        playbackPositionMs = Math.max(0,
                Math.min(positionMs, duration));

        if (playing) {
            startTimeNanos = System.nanoTime()
                    - playbackPositionMs * 1_000_000L;
        }
    }

    public static long getDurationMs() {
        if (lines.isEmpty()) {
            return 0;
        }

        return lines.get(lines.size() - 1).timeMs();
    }

    /**
     * Returns the lyric currently active at the playback position.
     */
    public static LyricLine getCurrentLine() {
        long position = getPlaybackPositionMs();
        LyricLine current = null;

        for (LyricLine line : lines) {
            if (line.timeMs() > position) {
                break;
            }

            current = line;
        }

        return current;
    }

    public static LyricLine getNextLine() {
        long position = getPlaybackPositionMs();

        for (LyricLine line : lines) {
            if (line.timeMs() > position) {
                return line;
            }
        }

        return null;
    }

    public record LyricLine(long timeMs, String text) {
    }
}
