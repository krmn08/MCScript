package com.krmnserv321.mcscript.script.eval;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ScriptRunnable extends BukkitRunnable {
    private final Function function;
    private int count;

    ScriptRunnable(Function function, int count) {
        this.function = function;
        Environment environment = EvalUtils.newEnclosedEnvironment(function.getEnvironment());
        try {
            environment.putConstant("cancel", new CallableMethod(this, getClass().getMethod("cancel")));
            environment.putConstant("isCancelled", new CallableMethod(this, getClass().getMethod("isCancelled")));
            environment.putConstant("getTaskId", new CallableMethod(this, getClass().getMethod("getTaskId")));
            environment.putConstant("setCount", new CallableMethod(this, getClass().getMethod("setCount", int.class)));
            environment.putConstant("getCount", new CallableMethod(this, getClass().getMethod("getCount")));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        function.setEnvironment(environment);

        this.count = count;
    }

    @Override
    public void run() {
        if (count == 0) {
            if (!isCancelled()) {
                cancel();
            }
            return;
        } else if (count > 0) {
            count--;
        }

        Object ret = function.call();
        if (ret instanceof ScriptError) {
            Bukkit.getLogger().severe(String.valueOf(ret));
            cancel();
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
