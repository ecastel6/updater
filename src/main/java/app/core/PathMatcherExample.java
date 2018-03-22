package app.core;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.*;

public class PathMatcherExample {
    String [] pathPattern;

    public PathMatcherExample(String[] pathPattern) {
        this.pathPattern = pathPattern;
    }

    static PathMatcher getPathPattern(String[] pathPattern) {
        FileSystem fileSystem = FileSystems.getDefault();
        String p=StringUtils.join(pathPattern,File.separator);
        System.out.println(p);
        return fileSystem.getPathMatcher(
                "glob:/**/"+ p);

    }

    public static void main(String[] args) {
        String[] testString = new String[]{"opt","arcadiaVersions"};
        PathMatcher pathMatcher =getPathPattern(testString);
        System.out.println(pathMatcher.toString());
        Path path = Paths.get("/tmp/opt/arcadiaVersions/ini.conf");
        System.out.println(pathMatcher.matches(path));
        }
    }


