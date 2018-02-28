package app;

import java.nio.file.Path;
import java.nio.file.Paths;

// todo rename dir
// todo move dir
// todo backup directory
// todo create directory
// todo delete directory
// todo copy file
// todo find directory
// todo directory size

public class ArcadiaUpdater
{

    public static void main(String[] args) {
        /*try {
            DbController dbController = DbController.getInstance();
            System.out.printf("DB dir : %s\n", dbController.getServerDir());
        } catch (IOException e) {
            e.printStackTrace()
        }*/
        Path p = Paths.get("C:\\Users\\minisergio\\Documents\\CAMPUSDELASALUD\\opt\\pgsql\\data\\postgresql.conf");
        System.out.println(p.getNameCount());
        //DbController db=DbController.getInstance();
        //db.getFilenameDepth(p);
        /*String username = "postgres";
        String database = "template1";
        //String executeCmd = "pg_dump -U " + username + " -w -c -f " + database + ".sql " + database;
        String[] executeCmd = new String[]{"psql", "-U", "postgres", "-l"};
        //String executeCmd = "psql -U " + username + " -l";
        //final String cmd = "/home/ecastel/opt/pgsql/bin/psql --username \"postgres\" -l >/tmp/pp";
        System.out.println(executeCmd);
        Process runtimeProcess;
        try {
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
                System.out.println("Command executed successfully");
            } else {
                System.out.println("Command error");
            }
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null)
                System.out.println(line);

        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        /*
        DbController dbc = DbController.getInstance();
        System.out.println(dbc.getServerConfFilename());

        try {
            FileBasedConfigurationHandler fbch = new FileBasedConfigurationHandler("C:/prop/rabbitmq_old.properties");
            if (fbch.isKeyPresent("rabbitmq.addresses"))
                System.out.printf("valor es %s", fbch.getKeyValue("rabbitmq.addresses"));
            else
                System.out.println("No está");
            *//*if (fbch.getArrayList().contains("rabbitmq.addresses")) {
                System.out.printf("Clave presente: Valor %s \n",fbch.getConfig().getString("rabbitmq.addresses"));
            } else
                System.out.println("Clave no encontrada");*//*
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

*/


        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

    }
}
