package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.utils.Utils;
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

        ResultSet rs = DatabaseConnection.createStatement("SELECT server_id, name, head_id FROM plotsystem_countries WHERE id = ?")
                .setValue(this.ID).executeQuery();

        if (rs.next()) {
            this.serverID = rs.getInt(1);
            this.name = rs.getString(2);
            this.headID = rs.getString(3);
        }
    }

    public int getID() {
        return ID;
    }

    public Server getServer() throws SQLException {
        return new Server(serverID);
    }

    public String getName() {
        return name;
    }

    public ItemStack getHead() {
        return Utils.TryParseInt(headID) != null ? Utils.getItemHead(new Utils.CustomHead(Integer.parseInt(headID))) : null;
    }

    public static List<Country> getCountries() {
        try {
            ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_countries ORDER BY server_id").executeQuery();

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

    public static void addCountry(String name) {
        try {
            DatabaseConnection.createStatement("INSERT INTO plotsystem_countries (name) VALUES (?)").setValue(name).executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void removeCountry(String name) {
        try {
            DatabaseConnection.createStatement("DELETE FROM plotsystem_countries WHERE name = ?").setValue(name).executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void setHeadID(String name, int id) {
        try {
            DatabaseConnection.createStatement("UPDATE plotsystem_countries SET head_id = ? WHERE name = ?").setValue(id).setValue(name).executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}