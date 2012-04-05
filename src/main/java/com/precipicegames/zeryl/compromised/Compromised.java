package com.precipicegames.zeryl.compromised;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;

public class Compromised extends JavaPlugin implements Listener {

    private FileConfiguration config;
    protected Connection db;
    protected String uri;
    protected String user;
    protected String password;
    protected String dbDriver;

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = getConfig();
        this.uri = "mysql://" + config.getString("host") + ":" + config.getString("port") + "/" + config.getString("dbname");
        this.user = config.getString("user");
        this.password = config.getString("pass");
        this.connect();

        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " is now enabled.");
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " is now disabled.");        
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        if (checkName(event.getName())) {
            event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("Please contact us on the forums, your account has shown to be compromised.");
        }
    }

    private boolean checkName(String name) {
        try {
            if(!this.db.isValid(0))
                this.connect();
        } catch (Exception ex) {
            Logger.getLogger(Compromised.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            PreparedStatement check = this.db.prepareStatement("select 1 from players where name = ?");
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if(rs.next() != false) {
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Compromised.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    protected final void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.db = DriverManager.getConnection("jdbc:" + uri, user, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}