package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerProvider {
    private static final List<String> cachedServers = new ArrayList<>();

    public ServerProvider() {
        // cache all servers
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT server_name FROM server;")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) cachedServers.add(rs.getString(1));
            }
        } catch (SQLException ex) {Utils.logSqlException(ex);}
    }

    public boolean serverExists(String serverName) {
        return cachedServers.stream().anyMatch(s -> s.equals(serverName));
    }

    public List<String> getServers() {
        return cachedServers;
    }

    public boolean addServer(String name, int buildTeamId) {
        if (serverExists(name)) return true;
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO server (server_name, build_team_id) " +
                        "VALUES (?, ?);")) {
            stmt.setString(1, name);
            stmt.setInt(2, buildTeamId);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedServers.add(name);
            return result;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean removeServer(String name) {
        if (!serverExists(name)) return false;
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM server WHERE server_name = ?;")) {
            stmt.setString(1, name);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedServers.remove(name);
            return result;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }
}
