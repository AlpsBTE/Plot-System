/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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
