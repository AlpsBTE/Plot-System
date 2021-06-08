package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.system.plot.Plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FTPManager {

    public static String getFTPUrl(Utils.Server server, Plot plot) {
        return String.format(
                "%sftp://%s:%s@%s:%d/%s",
                server.ftpConfiguration.secureFTP ? "s" : "",
                server.ftpConfiguration.username,
                server.ftpConfiguration.password,
                server.ftpConfiguration.address,
                server.ftpConfiguration.port,
                server.finishedSchematicPath
                        + plot.getCity().getID()
                        + "/"
                        + plot.getID() + ".schematic"
        );
    }

    private static final int BUFFER_SIZE = 4096;

    public static void sendFileFTP(String ftpURL, File schematic)
    {
        try {
            URL url = new URL(ftpURL);
            URLConnection conn = url.openConnection();
            OutputStream outputStream = conn.getOutputStream();
            FileInputStream inputStream = new FileInputStream(schematic);

            byte[] buffer = new byte[BUFFER_SIZE];
            @SuppressWarnings("UnusedAssignment") int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            System.out.println("File uploaded");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}