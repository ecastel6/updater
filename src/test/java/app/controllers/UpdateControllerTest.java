package app.controllers;

import app.models.ReturnValues;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateControllerTest {

    @Test
    void relativePercentageTest() {
        BackupsController backupsController = BackupsController.getInstance();
        assertEquals(
                backupsController.differencePercentage(1050L, 1000L),
                backupsController.differencePercentage(1000L, 1050L));
    }

    @Test
    void updateAppTest() {
    /*    ArcadiaController arcadiaController=ArcadiaController.getInstance();
        UpdateController updateController=new UpdateController("oc");
        updateController.stopAppServer(ArcadiaApp.OPENCARD);
        ArcadiaAppData arcadiaAppData=arcadiaController.getInstalledApps().get("oc");
        //ArcadiaAppData arcadiaAppData=new ArcadiaAppData(ArcadiaApp.OPENCARD,new File("D:/opt/tomcat_oc"),null,null);
        updateController.reinstallServices(arcadiaAppData);*/
        ServiceController serviceController = ServiceController.getInstance();
        String serviceName = "tomcat_oc";
        File sourceServiceScript = FileUtils.getFile("d:/opt/arcadiaVersions/base/windows/" + serviceName + ".bat");
        String drive = FilenameUtils.getPrefix(sourceServiceScript.toString().substring(0, 2));
        System.out.println(FilenameUtils.getPrefix(String.valueOf(sourceServiceScript)));
        File targetServiceScript = FileUtils.getFile("D:/opt/tomcat_oc/", "bin", serviceName + ".bat");
        ReturnValues retDestroy = serviceController.runCommand(new String[]{"cmd.exe", "/c", drive + " && " + "cd " + targetServiceScript.getParent() + " && " + targetServiceScript.toString(), "remove", serviceName});
        ReturnValues retCreate = serviceController.runCommand(new String[]{"cmd.exe", "/c", drive + " && " + "cd " + targetServiceScript.getParent() + " && " + targetServiceScript.toString(), "install", serviceName});
        System.out.println(String.format("Destroy retValue=%s retMsg=%s", retDestroy.t, retDestroy.u));
        System.out.println(String.format("Create retValue=%s retMsg=%s", retCreate.t, retCreate.u));
    }

    @Test
    void rollbackApplicationTest() {
        Stack stack = new Stack();
        stack.push("rollbackArcadiaResources");
        UpdateController updateController = new UpdateController();
        updateController.rollbackApplication(stack);
    }

    @Test
    void rollbackArcadiaResourcesTest() {
        UpdateController updateController = new UpdateController();
        updateController.setInstalledAppDir(new File("/home/ecastel/opt/tomcat_cbos"));
        updateController.setLatestUpdatesVersionDir(new File("/home/ecastel/opt/arcadiaVersions/cbos/3.12R2"));
        /*System.out.println(updateController.rollbackArcadiaResources());
        System.out.println(updateController.rollbackLogBack());
        System.out.println(updateController.rollbackSharedlib());
        System.out.println(updateController.rollbackCustom());*/
        updateController.rollbackWars();
    }
}