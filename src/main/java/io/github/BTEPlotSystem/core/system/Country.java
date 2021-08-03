package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.database.DatabaseConnection;
import github.BTEPlotSystem.core.database.builder.StatementBuilder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Country {

    private final int ID;
    private int serverID;

    private String name;
    private String headID;

    public Country(int ID) throws SQLException {
        this.ID = ID;

        String sql = "SELECT * FROM plotsystem_countries WHERE id = ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setInt(this.ID).build());

        if (!rs.wasNull()) {
            this.serverID = rs.getInt("server_id");
            this.name = rs.getString("name");
            this.headID = rs.getString("head_id");
        }
    }

    public int getID() {
        return ID;
    }

    public int getServer_id() {
        return serverID;
    }

    public String getName() {
        return name;
    }

    public ItemStack getHead() {
        return Utils.getItemHead(headID);
    }

    public static List<Country> getCountries() {
        try {
            String sql = "SELECT id FROM plotsystem_countries ORDER BY server_id";
            ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql).build());

            List<Country> countries = new ArrayList<>();
            while (rs.next()) {
                countries.add(new Country(rs.getInt(1)));
            }
            return countries;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }
}