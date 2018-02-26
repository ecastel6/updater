package app;

import app.controllers.FileBasedConfigurationHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

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
            e.printStackTrace();
        }*/
        try {
            FileBasedConfigurationHandler fbch = new FileBasedConfigurationHandler("C:/prop/rabbitmq_old.properties");
            Object value = fbch.getConfig().getString("rabbitmq.addresses");
            System.out.println(value.toString());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }




        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

    }
}
