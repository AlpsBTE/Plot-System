package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServerProvider {
    protected static final List<String> SERVERS = new ArrayList<>();

    public ServerProvider() {
        String qAll = "SELECT server_name FROM server;";

        Utils.handleSqlException(() -> SqlHelper.runQuery(qAll, ps -> {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SERVERS.add(rs.getString(1)); // cache all servers
            }
        }));
    }

    public boolean serverExists(String serverName) {
        return SERVERS.stream().anyMatch(s -> s.equals(serverName));
    }

    public List<String> getServers() {
        return SERVERS;
    }

    public boolean addServer(String name, int buildTeamId) {
        if (serverExists(name)) return true;

        String qInsert = "INSERT INTO server (server_name, build_team_id) VALUES (?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsert, ps -> {
            ps.setString(1, name);
            ps.setInt(2, buildTeamId);
            boolean result = ps.executeUpdate() > 0;
            if (result) SERVERS.add(name);
            return result;
        })));
    }

    public boolean removeServer(String name) {
        if (!serverExists(name)) return false;

        String qDelete = "DELETE FROM server WHERE server_name = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDelete, ps -> {
            ps.setString(1, name);
            boolean result = ps.executeUpdate() > 0;
            if (result) SERVERS.remove(name);
            return result;
        })));
    }
}
