package app.controllers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileCopyController extends SimpleFileVisitor<Path>
{

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
            Files.copy(file, targetFile);
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
}