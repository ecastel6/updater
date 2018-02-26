package app;

import app.controllers.DbController;
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

        DbController dbc = DbController.getInstance();
        System.out.println(dbc.getServerConfFilename());

        try {
            FileBasedConfigurationHandler fbch = new FileBasedConfigurationHandler("C:/prop/rabbitmq_old.properties");
            if (fbch.isKeyPresent("rabbitmq.addresses"))
                System.out.printf("valor es %s", fbch.getKeyValue("rabbitmq.addresses"));
            else
                System.out.println("No está");
            /*if (fbch.getArrayList().contains("rabbitmq.addresses")) {
                System.out.printf("Clave presente: Valor %s \n",fbch.getConfig().getString("rabbitmq.addresses"));
            } else
                System.out.println("Clave no encontrada");*/
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
