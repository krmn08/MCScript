package com.krmnserv321.mcscript;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.InvocationTargetException;

public final class Reflection {
    private Reflection() {
    }

    public static SimpleCommandMap getCommandMap() {
        Server server = Bukkit.getServer();
        try {
            return (SimpleCommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException();
        }
    }
}
