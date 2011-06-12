package com.webkonsept.bukkit.memorywarning;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MemoryWarning extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");
	private final MemPoller poller = new MemPoller(this);
	
	private File configFile = new File("plugins/MemoryWarning/settings.yml");
	private Configuration config = new Configuration(configFile);
	
	// settings
	boolean verbose = false;
	Double warnPercentage = 95.0;
	Double panicPercentage = 97.0;
	Integer minutesBetweenPolls = 5;
	Boolean pollingMessage = false;
	List<String> killPlugins = new ArrayList<String>();
	

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		log.info("Disabled");
	}

	@Override
	public void onEnable() {
		
		out("Coming on line...");
		
		loadConfig();
		
		poller.setServer(getServer());
		poller.setWarnPercentage(warnPercentage);
		poller.setPanicPercentage(panicPercentage);
		poller.setPollingMessage(pollingMessage);
		poller.setSlayPlugins(killPlugins);
		
		Integer interval = minutesBetweenPolls * 1200; // On a good day it's 20 ticks per second, 1200 ticks per minute.
		
		if (! configFile.exists()){
			configFile.mkdirs();
			if (configFile.canWrite()){
				config.save();
			}
			else {
				crap("Can't write to configuration file, so I can't create it!");
			}
		}
		
		double memMax = Runtime.getRuntime().maxMemory() / 1048576;
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this,poller,200,interval);

		if (pollingMessage){
			out("I ill poll every "+interval+" server ticks, or rougly "+minutesBetweenPolls+" minutes apart, depending on load.");
			out("NOTE:  I'm going to spam your console every time I poll memory usage!");
		}
		babble("You've given Java a memory allowance of "+memMax+"MB I'll warn if the server uses more than "+warnPercentage+"%");
	}
	public void loadConfig() {
		File configFile = new File(this.getDataFolder().toString()+"/settings.yml");
		Configuration config = new Configuration(configFile);
		
		config.load();
		verbose = config.getBoolean("log.verbose", false);
		warnPercentage = config.getDouble("level.warn", 95.0);
		panicPercentage = config.getDouble("level.panic", 97.0);
		minutesBetweenPolls = config.getInt("minutesBetweenPolls",5);
		pollingMessage = config.getBoolean("log.polling", false);
		killPlugins = config.getStringList("killPlugins",killPlugins);
		
		if (!configFile.exists()){
			this.getDataFolder().mkdirs();
			try {
				configFile.createNewFile();
				killPlugins.add("Please edit your");
				killPlugins.add("settings file");
				killPlugins.add("for MemoryWarning");
				config.setProperty("killPlugins",killPlugins);
				out("There was no config file, so I made one in "+getDataFolder().toString()+"/"+configFile.getName());
			} catch (IOException e) {
				e.printStackTrace();
				this.crap("IOError while creating config file: "+e.getMessage());
			}
		}
		config.save();
	}
	public void out(String message) {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void crap(String message){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void babble(String message){
		if (!this.verbose){ return; }
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
	}
}
