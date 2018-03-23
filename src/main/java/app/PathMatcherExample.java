package app;

import java.nio.file.*;

public class PathMatcherExample {

    public static void main(String[] args) {
        String[] driveList = new String[]{"C:", "D:"};
        String[] pathMultifolder = new String[]{"opt", "arcadiaVersions"};

        FileSystem fileSystem = FileSystems.getDefault();
        //PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:D:/**/opt/arcadiaVersions");

        String pattern = "Vorsions";//opt/arcadiaVersions";

        //String regularExpression="([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?" +pattern +"$";
        String regularExpression = "\"[[a-zA-Z]:]?\\\\/(([A-z0-9\\\\-\\\\%]+\\\\/)*[A-z0-9\\\\-\\\\%]+)?\\\\/opt\\\\/arcadiaVersions$\"";
        //String regularExpression="([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?\\/opt.+";
        //String regularExpression="([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?\\/opt.+";

        //PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:?:/**/opt/arcadiaVersions");

        PathMatcher pathMatcher = fileSystem.getPathMatcher("regex:" + regularExpression);
        Path path = Paths.get("C:/cp/opt/arcadiaVersions");
        System.out.println(regularExpression);
        System.out.println(pathMatcher.matches(path));
    }
}
