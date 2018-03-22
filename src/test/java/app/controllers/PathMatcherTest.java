package app.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class PathMatcherTest implements PathMatcher {
    private ArrayList<String> driveList;
    private String[] multilevelPath;


    public PathMatcherTest(ArrayList<String> driveList, String[] multilevelPath) {
        this.driveList = driveList;
        this.multilevelPath = multilevelPath;
    }

    public static PathMatcher getPathMatcher(String[] pathMultiFolder) {
        String strPathMultiFolder = "";
        if (pathMultiFolder[0].contains(":"))
            strPathMultiFolder = pathMultiFolder[0];


        for (int i = 1; i < pathMultiFolder.length; i++)
            strPathMultiFolder += '/' + pathMultiFolder[i];

        System.out.printf("Path compilado: %s\n", strPathMultiFolder);
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:/**" + strPathMultiFolder);
        System.out.printf("PathMatchertoString %s\n", "glob:D:/**" + strPathMultiFolder);
        return pathMatcher;
    }

    public ArrayList<String> getDriveList() {
        if (driveList != null) return driveList;
        //ArrayList<String> driveList = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            if (Files.isWritable(root)) {
                try {
                    FileStore fileStore = Files.getFileStore(root);
                    if ((!fileStore.isReadOnly()) && (!fileStore.getAttribute("volume:isRemovable").equals(true))) {
                        driveList.add(root.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return driveList;
    }

    public String[] getMultilevelPath() {
        return multilevelPath;
    }


    public static void main(String[] args) {
        String[] pathMultifold = new String[]{"D:", "opt", "arcadiaVersions"};

//        FileSystem fileSystem = FileSystems.getDefault();
//        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:D:/**/opt/arcadiaVersions");
        Path path = Paths.get("D:/cp/opt/arcadiaVersions");
        //System.out.println(pathMatcher.matches(path));
        System.out.println(getPathMatcher(pathMultifold).matches(path));
    }

    @Override
    public boolean matches(Path path) {
        return false;
    }
}
