package com.krmnserv321.mcscript.script.java;

import com.krmnserv321.mcscript.MCScript;
import com.krmnserv321.mcscript.script.eval.ScriptRunnable;
import org.bukkit.plugin.Plugin;

public class RunnableObject {
    private static final Plugin PLUGIN = MCScript.getInstance();
    private ScriptRunnable runnable;

    public RunnableObject(ScriptRunnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        runnable.runTask(PLUGIN);
    }

    public void run(long delay) {
        runnable.runTaskLater(PLUGIN, delay);
    }

    public void run(long delay, long period) {
        runnable.runTaskTimer(PLUGIN, delay, period);
    }
}
