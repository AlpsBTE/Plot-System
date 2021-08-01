package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.system.plot.Plot;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.logging.Level;

public class FTPManager {

    public static String getFTPUrl(Utils.Server server, Plot plot) {
        return String.format("%sftp://%s:%s@%s:%d/%s%s/",
                server.ftpConfiguration.secureFTP ? "s" : "",
                server.ftpConfiguration.username,
                server.ftpConfiguration.password,
                server.ftpConfiguration.address,
                server.ftpConfiguration.port,
                server.finishedSchematicPath,
                plot.getCity().getID()
        );
    }

    public static void sendFileFTP(String ftpURL, File schematic) {
        try {
            FileSystemManager fileManager = VFS.getManager();

            FileSystemOptions options = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(options, false);

            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path and create missing directories
            FileObject remote = fileManager.resolveFile(ftpURL, options);
            remote.createFolder();

            // Create remote schematic and write to it
            FileObject remoteSchematic = remote.resolveFile(schematic.getName());
            remoteSchematic.copyFrom(localSchematic, Selectors.SELECT_SELF);

            localSchematic.close();
            remoteSchematic.close();

            Bukkit.getLogger().log(Level.INFO, "File " + schematic.getName() + " uploaded successfully!");
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
    }
}