package io.github.yannici.bedwarsreloaded;

import org.bukkit.ChatColor;

public class ChatWriter {

    public ChatWriter() {
        super();
    }

    public static String pluginMessage(String str) {
        return ChatColor.GOLD + "[" + Main.getInstance().getName() + "] " + ChatColor.WHITE + str;
    }

}
