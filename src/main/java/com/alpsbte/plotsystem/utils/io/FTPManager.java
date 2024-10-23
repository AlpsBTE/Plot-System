/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Server;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

public class FTPManager {

    private static FileSystemOptions fileOptions;

    private final static String DEFAULT_SCHEMATIC_PATH_LINUX = "/var/lib/Plot-System/schematics";

    static {
        try {
            fileOptions = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fileOptions, "no");
            SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(fileOptions, "password");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);

            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(fileOptions, true);
            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileOptions, false);
        } catch (FileSystemException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Exception found with FileSystemManager!"), ex);
        }
    }

    public static String getFTPUrl(Server server, int cityID) throws SQLException, URISyntaxException {
        String schematicsPath = server.getFTPConfiguration().getSchematicPath();
        return new URI(server.getFTPConfiguration().isSFTP() ? "sftp" : "ftp",
                server.getFTPConfiguration().getUsername() + ":" + server.getFTPConfiguration().getPassword(),
                server.getFTPConfiguration().getAddress(),
                server.getFTPConfiguration().getPort(),
                String.format("/%s/%s/%s/", schematicsPath == null ? DEFAULT_SCHEMATIC_PATH_LINUX : schematicsPath, "finishedSchematics", cityID),
                null,
                null).toString();
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
        } catch (FileSystemException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Exception found with FileSystemManager!"), ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    public static boolean downloadSchematic(String ftpURL, File schematic) {
        boolean fileExists = false;
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();

            // Get local schematic
            FileObject localSchematic = fileManager.toFileObject(schematic);

            // Get remote path
            FileObject remote = fileManager.resolveFile(ftpURL.replaceFirst("finishedSchematics/", ""), fileOptions);

            // Get remote schematic and write it to local file
            FileObject remoteSchematic = remote.resolveFile(schematic.getName());
            if (remoteSchematic.exists()) {
                localSchematic.copyFrom(remoteSchematic, Selectors.SELECT_SELF);
                fileExists = true;
            }

            localSchematic.close();
            remoteSchematic.close();
        } catch (FileSystemException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Exception found with FileSystemManager!"), ex);
        }
        return fileExists;
    }

    public static void deleteSchematic(String ftpURL, String schematicName) throws FileSystemException {
        try (StandardFileSystemManager fileManager = new StandardFileSystemManager()) {
            fileManager.init();
            FileObject remote, remoteSchematic;

            remote = fileManager.resolveFile(ftpURL, fileOptions);
            remoteSchematic = remote.resolveFile(schematicName);
            if (remoteSchematic.exists()) {
                remoteSchematic.delete();
                if (remote.getChildren().length == 0) {
                    remote.delete();
                }
            }
        }
    }
}