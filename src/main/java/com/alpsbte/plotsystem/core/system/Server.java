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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class Server {
    private final int ID;
    private int ftpConfigurationID;

    private String name;

    public Server(int ID) throws SQLException {
        this.ID = ID;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT ftp_configuration_id, name FROM plotsystem_servers WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.ftpConfigurationID = rs.getInt(1);
                if (rs.wasNull()) this.ftpConfigurationID = -1;

                this.name = rs.getString(2);
            }

            DatabaseConnection.closeResultSet(rs);
        }
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public FTPConfiguration getFTPConfiguration() throws SQLException {
        return ftpConfigurationID != -1 ? new FTPConfiguration(ftpConfigurationID) : null;
    }

    public static List<Server> getServers() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_servers").executeQuery()) {
            List<Server> servers = new ArrayList<>();
            while (rs.next()) {
                servers.add(new Server(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);

            return servers;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return new ArrayList<>();
    }

    public static Server addServer(String name) throws SQLException {
        int id = DatabaseConnection.getTableID("plotsystem_servers");
        DatabaseConnection.createStatement("INSERT INTO plotsystem_servers (id, name) VALUES (?, ?)")
                .setValue(id)
                .setValue(name).executeUpdate();
        return new Server(id);
    }

    public static void removeServer(int serverID) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_servers WHERE id = ?")
                .setValue(serverID).executeUpdate();
    }

    public static void setFTP(int serverID, int ftpID) throws SQLException {
        if (ftpID != -1) {
            DatabaseConnection.createStatement("UPDATE plotsystem_servers SET ftp_configuration_id = ? WHERE id = ?")
                    .setValue(ftpID)
                    .setValue(serverID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_servers SET ftp_configuration_id = DEFAULT WHERE id = ?")
                    .setValue(serverID).executeUpdate();
        }
    }
}
