package com.webkonsept.bukkit.memorywarning;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class MemoryWarningPlayerListener extends PlayerListener {
	MemoryWarning plugin;
	
	public MemoryWarningPlayerListener (MemoryWarning instance){
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		if (!plugin.isEnabled()) return;
		if (plugin.poller.isPanicing()){
			if (event.getPlayer().isOp()){
				event.getPlayer().kickPlayer("Sorry mate, the server is in Memory Panic.  Try again later.");
				event.setJoinMessage(ChatColor.RED+event.getPlayer().getName()+" joined, but is rejected due to the current Memory Panic.");
				plugin.crap("Had to kick "+event.getPlayer().getName()+" because we're in a memory panic :(");
			}
			else {
				event.getPlayer().sendMessage(ChatColor.RED+"Memory panic is happening >right now<!");				
			}
		}
	}

}
