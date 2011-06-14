package com.webkonsept.bukkit.memorywarning;


import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public class MemPoller implements Runnable {
	
	private MemoryWarning plugin;
	private Server server;
	private Double warnPercentage = 95.0;
	private Double panicPercentage = 97.0;
	private Double calmPercentage = 94.0;
	private Boolean spamMe = false;
	private Boolean PANIC = false;
	private List<Plugin> slayPlugins = new ArrayList<Plugin>();
	private List<Plugin> disabledPlugins = new ArrayList<Plugin>();
	private PluginManager pm;
	private double memUsed = ( Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory() ) / 1048576;
	private double memMax = Runtime.getRuntime().maxMemory() / 1048576;
	private double percentageUsed = ( 100 / memMax) * memUsed;
	
	MemPoller (MemoryWarning instance){
		plugin = instance;
	}
	public void setServer(Server use){
		server = use;
		if (use != null){
			pm = use.getPluginManager();
		}
	}
	public double getMemUsed(){
		return memUsed;
	}
	public double getMemMax(){
		return memMax;
	}
	public double getPercentageUsed(){
		return percentageUsed;
	}
	public boolean isPanicing(){
		return PANIC;
	}
	
	public String getSummary(){
		updateNumbers();
		String msg = "Server is using "+(int)percentageUsed+"% of it's memory allowance ("+memUsed+"MB/"+memMax+"MB)";
		return msg;
	}
	
	public void setWarnPercentage(Double percent){
		warnPercentage = percent;
		plugin.babble("Will warn at "+warnPercentage+"%");
	}
	public void setPanicPercentage(Double percent){
		panicPercentage = percent;
		plugin.babble("Will panic at "+panicPercentage+"%");
	}
	public void setCalmPercentage(Double percent){
		calmPercentage = percent;
		plugin.babble("Will calm down at "+calmPercentage+"%");
	}
	
	
	public void setLogPolling (Boolean spam){
		spamMe = spam;
		if (spam){
			plugin.babble("Polling spam is enabled.");
		}
		else {
			plugin.babble("Polling spam is disabled.");
		}
	}
	public void setSlayPlugins (List<String> pluginList){
		slayPlugins.clear();
		for (String pluginName : pluginList){
			if (pluginName != null){
				this.addToSlayList(pluginName);
			}
		}
	}
	public void addToSlayList(String pluginName){
		if (pm == null){
			plugin.crap("Plugin Manager is not available yet!  Can't add "+pluginName+" to list of plugins to kill in a panic!");
			return;
		}
		Plugin thisPlugin = pm.getPlugin(pluginName);
		if (thisPlugin != null && ! pluginName.equalsIgnoreCase("MemoryWarning")){
			slayPlugins.add(thisPlugin);
			plugin.babble(pluginName+" will be killed in panic");
		}
		else if (pluginName.equalsIgnoreCase("MemoryWarning")){
			plugin.crap("MemoryWarning listed for killing. I refuse to disable myself.");
		}
		else {
			plugin.crap("A plugin in the killPlugins list is NOT available: "+pluginName);
		}
	}
	private void updateNumbers() {
		memUsed = ( Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory() ) / 1048576;
		memMax = Runtime.getRuntime().maxMemory() / 1048576;
		percentageUsed = ( 100 / memMax) * memUsed;
	}
	public void run() {
		
		String msg = getSummary();
		
		if ( percentageUsed > panicPercentage){
			if (PANIC){
				panicActions();
			}
			else {
				PANIC = true;
			}
		}
		else if (PANIC){
			if (percentageUsed < calmPercentage){
				unPanicActions();
				server.broadcastMessage(ChatColor.GREEN+"Server is no longer in memory panic mode, and has resumed normal operations.");
				PANIC = false;
			}
		}
		
		if ( percentageUsed > warnPercentage){
			
			if (PANIC){
				plugin.crap("SERVER IS IN MEMORY PANIC MODE! "+percentageUsed+"% RAM USED!");
				server.savePlayers();
				for (World world : server.getWorlds()){
					world.save();
				}
				server.dispatchCommand((CommandSender)new ConsoleCommandSender(server),"save-off");
				
				server.broadcastMessage(ChatColor.RED+"ATTENTION PLEASE!");
				server.broadcastMessage(ChatColor.RED+"The server is about to run out of memory.  All players and worlds have been saved.");
				server.broadcastMessage(ChatColor.RED+"If the situation does not improve shortly, a lot of plugins will be disabled, and players kicked.");
				server.broadcastMessage(ChatColor.RED+"Sorry for the inconveniance, and you are of course welcome back later.");
			}
			else {
				plugin.crap(msg);
				for (Player player : server.getOnlinePlayers()){
					if (player.isOp()){
						player.sendMessage(ChatColor.RED+"[MemoryWarning] "+msg);
					}
				}
			}
		}
		else if (spamMe){
			plugin.out(msg);
		}
	}

	private void panicActions() {
		plugin.crap("Taking MEMORY PANIC actions!");
		for (Plugin plugin : slayPlugins){
			if (pm.isPluginEnabled(plugin)){
				pm.disablePlugin(plugin);
				disabledPlugins.add(plugin);
			}
		}
		for (Player player : server.getOnlinePlayers()){
			if (!player.isOp()){
				player.kickPlayer("Memory panic, so I had to let you go.  Come back in a few minutes.");
				plugin.crap("Had to give "+player.getName()+" the boot :(");
			}
		}
	}
	private void unPanicActions() {
		plugin.out("Standing down from MEMORY PANIC");
		for (Plugin plugin : disabledPlugins){
			pm.enablePlugin(plugin);
		}
		disabledPlugins.clear();
		server.dispatchCommand((CommandSender)new ConsoleCommandSender(server),"save-on");
	}
}
