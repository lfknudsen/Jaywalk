package com.falkknudsen.osmunda;

import com.falkknudsen.jaywalk.Tuple;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.apache.commons.compress.compressors.CompressorStreamFactory.BZIP2;

/** Utility class for handling the opening and unpacking of files on the user's machine. */
public class FileHandler {
    /** Unpacks a compressed/archived file as necessary, returning a {@link Tuple}
     with the innermost filetype and buffered input-stream to the contents.
     Supports nested compression, e.g. .tar.gz. */
    public static Tuple<String, InputStream> unpack(File file) throws IOException {
        String filename = file.getPath();
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the file " + filename + ".");
        }
        InputStream stream = new BufferedInputStream(new FileInputStream(file));

        String[] parts = filename.split("\\."); // separate the extension(s).
        for (int i = parts.length - 1; i > 0; i--) {  // iterate backwards, so last extension is unpacked first.
            switch (parts[i]) {
            case "tar", "zip":
                stream = unzip(stream);
                break;
            case "gz", "bz2", "bzip2":
                stream = decompress(stream);
                break;
            default:
                return new Tuple<>(parts[i], wrap(stream));
            };
        }
        return new Tuple<>(filename, wrap(stream));
    }

    public static InputStream wrap(InputStream stream) {
        if (!stream.markSupported()) {
            return new BufferedInputStream(stream);
        }
        return stream;
    }

    /** For .zip, and .tar, which are archive formats.<br> If the input
     {@code stream} isn't already wrapped in a {@link BufferedInputStream},
     this will do so automatically first. Only returns the <em>first</em>
     file in the archive. */
    private static InputStream unzip(InputStream stream) {
        try {
            ArchiveInputStream<? extends ArchiveEntry> input =
                    new ArchiveStreamFactory().createArchiveInputStream(wrap(stream));
            input.getNextEntry();
            return input;
        } catch (ArchiveException | IOException e) {
            throw new RuntimeException("An error occurred while attempting to unzip the file.");
        }
    }

    /** For .bz2 and .gz, which are compression formats.
     If the input {@code stream} isn't already wrapped in a {@link BufferedInputStream}, this will
     do so automatically first. */
    private static InputStream decompress(InputStream stream) {
        try {
            return new CompressorStreamFactory().createCompressorInputStream(wrap(stream));
        } catch (CompressorException e) {
            throw new RuntimeException("An error occurred while attempting to decompress the file.");
        }
    }

    /** Opens the user's operating system's built-in window for selecting a file.<br>
     The user can choose any of the following file-types:<br>
     .osm, .bin, .bz2, .zip, .gz, .tar, .obj. */
    public static Optional<String> pickFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick OSM file to load");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OSM files",
                        "*.osm", "*.bin", "*.bz2", "*.zip", "*.gz", "*.tar", "*.obj")
        );
        fileChooser.setInitialDirectory(new File("data/"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            return Optional.of(file.getPath());
        }
        return Optional.empty();
    }

    /** Compresses an existing file with the name {@code filename}, saving the result to a new file
     that has the same full name and extension(s), appended with {@code extension}.<br>
     The extension is expected to be either "gz", "bz2", or "bzip2". */
    public static void compress(String filename, String extension) throws IOException, CompressorException {
        if (extension.startsWith(".")) extension = extension.substring(1);
        extension = extension.toLowerCase();
        if (!extension.matches("gz|bz2|bzip2"))
            throw new IllegalArgumentException("Invalid extension: " + extension);

        InputStream fIn = Files.newInputStream(Paths.get(filename));
        InputStream in = new BufferedInputStream(fIn);

        OutputStream fOut = Files.newOutputStream(Paths.get(filename + "." + extension));
        OutputStream out = new BufferedOutputStream(fOut);

        // In practise, people tend to use "bz2", but the long-form name,
        // and the one used internally by the library, is "bzip2"
        if (extension.equals("bz2")) extension = BZIP2;
        CompressorStreamFactory factory = new CompressorStreamFactory();
        CompressorOutputStream stream =
                factory.createCompressorOutputStream(extension, out);

        IOUtils.copy(in, stream);

        stream.close();
        in.close();
    }

    /** Archives an existing file with the name {@code filename}, saving the result to a new file
     that has the same full name and extension(s), appended with {@code extension}.<br>
     The extension is expected to be either "zip" or "tar". */
    public static void archive(String filename, String extension) throws IOException, ArchiveException {
        if (extension.startsWith(".")) extension = extension.substring(1);
        extension = extension.toLowerCase();
        if (!extension.matches("zip|tar"))
            throw new IllegalArgumentException("Invalid extension: " + extension);

        File f = new File(filename);
        InputStream fIn = Files.newInputStream(Paths.get(filename));
        InputStream in = new BufferedInputStream(fIn);

        OutputStream fOut = Files.newOutputStream(Paths.get(filename + "." + extension));
        OutputStream out = new BufferedOutputStream(fOut);

        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveOutputStream<ArchiveEntry> stream =
                factory.createArchiveOutputStream(extension, out);

        ArchiveEntry entry = stream.createArchiveEntry(f, filename);
        stream.putArchiveEntry(entry);
        IOUtils.copy(in, stream);
        stream.closeArchiveEntry();
        stream.finish();

        stream.close();
        in.close();
    }
}
