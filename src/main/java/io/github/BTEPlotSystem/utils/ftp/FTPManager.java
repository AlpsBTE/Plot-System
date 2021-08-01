package github.BTEPlotSystem.utils.ftp;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.logging.Level;

public class FTPManager {

    private static FileSystemManager fileManager;
    private static FileSystemOptions fileOptions;

    static {
        try {
            fileManager = VFS.getManager();

            fileOptions = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileOptions, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
    }

    public static String getFTPUrl(Server server, int cityID) {
        return String.format("%sftp://%s:%s@%s:%d/%s%s/",
                server.getFTPConfiguration().getPort() == 22 ? "s" : "",
                server.getFTPConfiguration().getUsername(),
                server.getFTPConfiguration().getPassword(),
                server.getFTPConfiguration().getAddress(),
                server.getFTPConfiguration().getPort(),
                server.getSchematicPath(),
                cityID
        );
    }

    public static void uploadSchematic(String ftpURL, File schematic) {
        try {
            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path and create missing directories
            FileObject remote = fileManager.resolveFile(ftpURL, fileOptions);
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

    public static void downloadSchematic(String ftpURL, File schematic) {
        try {
            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path
            FileObject remote = fileManager.resolveFile(ftpURL, fileOptions);

            // Get remote schematic and write it to local file
            FileObject remoteSchematic = remote.resolveFile(schematic.getName());
            localSchematic.copyFrom(remoteSchematic, Selectors.SELECT_SELF);

            localSchematic.close();
            remoteSchematic.close();

            Bukkit.getLogger().log(Level.INFO, "File " + schematic.getName() + " downloaded successfully!");
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
    }
}