package github.BTEPlotSystem.utils.ftp;

import github.BTEPlotSystem.core.system.Server;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class FTPManager {

    private static FileSystemOptions fileOptions;

    private final static String DEFAULT_SCHEMATIC_PATH_LINUX = "/var/lib/Plot-System/schematics";

    static {
        try {
            fileOptions = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileOptions, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
    }

    public static String getFTPUrl(Server server, int cityID) throws SQLException {
        String schematicsPath = server.getFTPConfiguration().getSchematicPath();

        return String.format("%sftp://%s:%s@%s:%d/%s/%s/%s/",
                server.getFTPConfiguration().getPort() == 22 ? "s" : "",
                server.getFTPConfiguration().getUsername(),
                server.getFTPConfiguration().getPassword(),
                server.getFTPConfiguration().getAddress(),
                server.getFTPConfiguration().getPort(),
                schematicsPath == null ? DEFAULT_SCHEMATIC_PATH_LINUX : schematicsPath,
                "finishedSchematics",
                cityID
        );
    }

    public static CompletableFuture<Void> uploadSchematic(String ftpURL, File schematic) {
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();

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
        return CompletableFuture.completedFuture(null);
    }

    public static CompletableFuture<Void> downloadSchematic(String ftpURL, File schematic) {
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();

            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path
            FileObject remote = fileManager.resolveFile(ftpURL.replaceFirst("finishedSchematics/",""), fileOptions);

            // Get remote schematic and write it to local file
            FileObject remoteSchematic = remote.resolveFile(schematic.getName());
            localSchematic.copyFrom(remoteSchematic, Selectors.SELECT_SELF);

            localSchematic.close();
            remoteSchematic.close();

            Bukkit.getLogger().log(Level.INFO, "File " + schematic.getName() + " downloaded successfully!");
        } catch (FileSystemException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception found with FileSystemManager!", ex);
        }
        return CompletableFuture.completedFuture(null);
    }
}