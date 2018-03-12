package app.controllers;
//
// DEPRECATED
// Decided to go with apache commons-io
//
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileCopyController extends SimpleFileVisitor<Path> {

    private Path sourceDir;
    private Path targetDir;

    public FileCopyController(Path sourceDir, Path targetDir) {

        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attributes) {

        try {
            Path targetFile = targetDir.resolve(sourceDir.relativize(file));
            Files.copy(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attributes) {
        try {
            Path newDir = targetDir.resolve(sourceDir.relativize(dir));
            Files.createDirectory(newDir);
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return FileVisitResult.CONTINUE;
    }

    public static void copyDirectory(Path sourceDir, Path targetDir) {
        try {
            Files.walkFileTree(sourceDir, new FileCopyController(sourceDir, targetDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileOrDirectory(Path sourceDir, Path targetDir) {
        // todo check file or dir check source & target
        try {
            Files.walkFileTree(sourceDir, new FileCopyController(sourceDir, targetDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path move(Path source, Path target, StandardCopyOption mode) {
        // StandardCopyOption REPLACE_EXISTING or ATOMIC_MOVE
        Path newTarget;
        try {
            newTarget = Files.move(source, target, mode);
            return newTarget;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void delete(Path deletePath) throws IOException {
        Files.walkFileTree(deletePath,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path dir, IOException exc) throws IOException {
                        //System.out.printf("Delete dir: %s\n", dir.toString());
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs)
                            throws IOException {
                        //System.out.printf("Delete file: %s\n", file.toString());
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}