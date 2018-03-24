package app;

import java.nio.file.*;

public class PathMatcherExample {

    public static void main(String[] args) {
        String[] driveList = new String[]{"C:", "D:"};
        String[] pathMultifolder = new String[]{"opt", "arcadiaVersions"};

        FileSystem fileSystem = FileSystems.getDefault();
        String pattern = "opt/arcadiaVersions/reg.properties";
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:?:/**/" + pattern);


        //String regularExpression="([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?" +pattern +"$";
        //String regularExpression="^([a-zA-Z]:\\/[a-zA-Z]*)";
        //String regularExpression="([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?\\/opt.+";

        //PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:?:/**/opt/arcadiaVersions");
        //Pattern pattern=Pattern.compile(regularExpression);
        Path samplepath = Paths.get("D:/cp/opt/arcadiaVersions/reg.properties");
        if (pathMatcher.matches(samplepath)) System.out.println("Match");

        //PathMatcher pathMatcher = fileSystem.getPathMatcher("regex:" + regularExpression);


    }
}
