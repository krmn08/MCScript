package com.krmnserv321.mcscript.script.eval;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

class ScriptCommand extends BukkitCommand {
    private boolean isVarArgs;
    private Function function;

    ScriptCommand(String name, boolean isVarArgs, Function function) {
        super(name);
        this.isVarArgs = isVarArgs;
        this.function = function;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        try {
            Environment environment = EvalUtils.newEnclosedEnvironment(function.getEnvironment());
            environment.putConstant("sender", sender);
            function.setEnvironment(environment);

            Object ret;
            if (isVarArgs) {
                ret = function.call((Object) args);
            } else {
                //noinspection RedundantCast
                ret = function.call((Object[]) args);
            }

            if (ret instanceof ScriptError) {
                Bukkit.getLogger().severe(String.valueOf(ret));
                return false;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(usageMessage);
            return false;
        }

        return true;
    }
}
