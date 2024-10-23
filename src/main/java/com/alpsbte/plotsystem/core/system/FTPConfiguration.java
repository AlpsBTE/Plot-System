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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class FTPConfiguration {
    private final int ID;

    private String schematicPath;
    private String address;
    private int port;
    private boolean isSFTP;
    private String username;
    private String password;

    public FTPConfiguration(int ID) throws SQLException {
        this.ID = ID;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT schematics_path, address, port, isSFTP, username, password FROM plotsystem_ftp_configurations WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.schematicPath = rs.getString(1);
                this.address = rs.getString(2);
                this.port = rs.getInt(3);
                this.isSFTP = rs.getBoolean(4);
                this.username = rs.getString(5);
                this.password = rs.getString(6);
            }

            DatabaseConnection.closeResultSet(rs);
        }
    }

    public int getID() {
        return ID;
    }

    public String getSchematicPath() {
        if (schematicPath != null) {
            schematicPath = !schematicPath.startsWith("/") ? File.separator + schematicPath : schematicPath;
            schematicPath = schematicPath.endsWith("/") ? schematicPath.substring(0, schematicPath.length() - 1) : schematicPath;
        }
        return schematicPath;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isSFTP() {
        return isSFTP;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static List<FTPConfiguration> getFTPConfigurations() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_ftp_configurations").executeQuery()) {
            List<FTPConfiguration> ftpConfigurations = new ArrayList<>();
            while (rs.next()) {
                ftpConfigurations.add(new FTPConfiguration(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);

            return ftpConfigurations;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return new ArrayList<>();
    }

    public static void addFTPConfiguration(String address, int port, boolean isSFTP, String username, String password) throws SQLException {
        DatabaseConnection.createStatement("INSERT INTO plotsystem_ftp_configurations (id, address, port, isSFTP, username, password) VALUES (?, ?, ?, ?, ?, ?)")
                .setValue(DatabaseConnection.getTableID("plotsystem_ftp_configurations"))
                .setValue(address).setValue(port).setValue(isSFTP ? 1 : 0).setValue(username).setValue(password).executeUpdate();
    }

    public static void removeFTPConfiguration(int ID) throws SQLException {
        if (getFTPConfigurations().stream().anyMatch(ftp -> ftp.getID() == ID)) {
            DatabaseConnection.createStatement("DELETE FROM plotsystem_ftp_configurations WHERE id = ?")
                    .setValue(ID).executeUpdate();
        }
    }

    public static void setSchematicPath(int ID, String path) throws SQLException {
        if (getFTPConfigurations().stream().anyMatch(ftp -> ftp.getID() == ID)) {
            DatabaseConnection.createStatement("UPDATE plotsystem_ftp_configurations SET schematics_path = ? WHERE id = ?")
                    .setValue(path).setValue(ID).executeUpdate();
        }
    }
}
