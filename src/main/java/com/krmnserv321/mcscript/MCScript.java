package com.krmnserv321.mcscript;

import com.krmnserv321.mcscript.script.Lexer;
import com.krmnserv321.mcscript.script.Parser;
import com.krmnserv321.mcscript.script.Program;
import com.krmnserv321.mcscript.script.ast.expression.Expression;
import com.krmnserv321.mcscript.script.ast.expression.FunctionDefinition;
import com.krmnserv321.mcscript.script.ast.statement.ExpressionStatement;
import com.krmnserv321.mcscript.script.ast.statement.ImportStatement;
import com.krmnserv321.mcscript.script.ast.statement.Statement;
import com.krmnserv321.mcscript.script.eval.*;
import com.krmnserv321.mcscript.script.event.EventAdapter;
import com.krmnserv321.mcscript.script.event.ScriptDisableEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MCScript extends JavaPlugin implements Listener {
    private static final String SCRIPT_PATH = "plugins/MCScript/script/";
    private static final String SEPARATOR = System.lineSeparator();

    private static Map<String, String> defineMap = new LinkedHashMap<>();

    private static Set<Player> replSet = new HashSet<>();
    private static Map<Player, String> scriptMap = new HashMap<>();
    private static Map<Player, Environment> environmentMap = new HashMap<>();

    private static Set<Command> commandSet = new HashSet<>();

    private static Environment commandEnvironment;

    private static MCScript instance;

    private Pattern MACRO_PATTERN = Pattern.compile("\\w+?(\\(.+?\\))?");
    private Pattern PAREN_PATTERN = Pattern.compile("\\(.+?\\)");

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        reload();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("mcs")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    SimpleCommandMap commandMap = Reflection.getCommandMap();
                    commandSet.forEach(cmd -> cmd.unregister(commandMap));
                    try {
                        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
                        field.setAccessible(true);
                        Map knownCommands = (Map) field.get(commandMap);
                        for (Iterator iterator = knownCommands.entrySet().iterator(); iterator.hasNext(); ) {
                            Object o = iterator.next();
                            Map.Entry entry = (Map.Entry) o;
                            //noinspection SuspiciousMethodCalls
                            if (commandSet.contains(entry.getValue())) {
                                iterator.remove();
                            }
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    EventAdapter.unregisterAll();
                    reload();
                    sendMessage(sender, "Reload complete");
                    return true;
                } else if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args[0].equalsIgnoreCase("repl")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("start")) {
                                repl(player, true);
                                return true;
                            } else if (args[1].equalsIgnoreCase("stop")) {
                                repl(player, false);
                                return true;
                            }
                        } else {
                            if (replSet.contains(sender)) {
                                repl(player, false);
                            } else {
                                repl(player, true);
                            }

                            return true;
                        }
                    }
                }

                String input = String.join(" ", args);
                eval(sender, input);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getLabel().equalsIgnoreCase("mcs")) {
            return super.onTabComplete(sender, command, alias, args);
        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            if ("repl".startsWith(args[0].toLowerCase())) {
                result.add("repl");
            }
            if ("reload".startsWith(args[0].toLowerCase())) {
                result.add("reload");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("repl")) {
                if ("start".startsWith(args[1].toLowerCase())) {
                    result.add("start");
                }
                if ("stop".startsWith(args[1].toLowerCase())) {
                    result.add("stop");
                }
            }
        }

        return result;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin() == this) {
            Bukkit.getPluginManager().callEvent(new ScriptDisableEvent());
        }
    }

    @SuppressWarnings("EmptyMethod")
    @EventHandler
    public void onScriptDisableEvent(ScriptDisableEvent e) {
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (replSet.contains(player)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + e.getMessage());
            String script = scriptMap.get(player);

            String input = script == null ? e.getMessage() : script + "\n" + e.getMessage();

            Parser parser = new Parser(new Lexer(applyDefine(input)));
            Program program = parser.parseProgram();
            List<String> errors = parser.getErrors();

            char last = input.charAt(input.length() - 1);
            if (last == '\\') {
                scriptMap.put(player, input);
                return;
            }

            if (!errors.isEmpty()) {
                if (last == ';') {
                    sendError(player, "parser errors:");
                    for (String error : errors) {
                        sendError(player, error);
                    }
                    scriptMap.remove(player);
                } else {
                    scriptMap.put(player, input);
                }
                return;
            }

            scriptMap.remove(player);

            Bukkit.getScheduler().runTask(this, () -> {
                Object evaluated = program.eval(environmentMap.get(player));
                if (evaluated instanceof ScriptError) {
                    sendError(player, evaluated.toString());
                } else {
                    player.sendMessage(String.valueOf(evaluated));
                }
            });
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        environmentMap.put(event.getPlayer(), EvalUtils.newEnclosedEnvironment(commandEnvironment));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        environmentMap.remove(event.getPlayer());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void reload() {
        try {
            File folder = getDataFolder();

            File configFile = new File(folder, "config.yml");

            FileConfiguration config;
            if (!configFile.exists()) {
                //noinspection ConstantConditions
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("default.yml")));
                config.save(configFile);
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);
            }

            File defineFile = new File(folder, "define.yml");

            FileConfiguration define;
            if (defineFile.createNewFile()) {
                define = new YamlConfiguration();
            } else {
                define = YamlConfiguration.loadConfiguration(defineFile);
            }

            defineMap.clear();

            for (String key : define.getKeys(false)) {
                defineMap.put(key, define.getString(key, ""));
            }

            PluginManager manager = getServer().getPluginManager();

            for (String depend : config.getStringList("Depend")) {
                Plugin plugin = getServer().getPluginManager().getPlugin(depend);
                if (plugin == null) {
                    getLogger().severe("plugin not found: " + depend);
                    return;
                }

                if (!plugin.isEnabled()) {
                    manager.enablePlugin(plugin);
                }
            }

            config.getStringList("SoftDepend").stream()
                    .map(s -> getServer().getPluginManager().getPlugin(s))
                    .filter(Objects::nonNull)
                    .filter(plugin -> !plugin.isEnabled())
                    .forEach(plugin -> getServer().getPluginManager().enablePlugin(plugin));

            PublicEnvironment publicEnvironment = new PublicEnvironment(getClassLoader());
            commandEnvironment = new Environment(publicEnvironment);

            List<String> ignoreList = config.getStringList("Ignore");
            config.getStringList("Import").forEach(publicEnvironment::importClass);

            getBukkitClasses().forEach(publicEnvironment::importClass);

            for (Method method : Bukkit.class.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    String name = method.getName();
                    publicEnvironment.put(name, new ClassMethod(Bukkit.class, name, false));
                }
            }

            publicEnvironment.put("PLUGIN", MCScript.getInstance());

            String scriptPath = config.getString("ScriptPath", SCRIPT_PATH);

            //noinspection ConstantConditions
            File scriptFolder = new File(scriptPath);
            scriptFolder.mkdirs();

            List<Program> programList = Files.find(scriptFolder.toPath(), Integer.MAX_VALUE, ((path, attr) -> !attr.isDirectory() && path.toString().endsWith(".mcs")))
                    .map(path -> {
                        String s = path.toString().replace(File.separatorChar, '/');
                        String sub = s.substring(scriptPath.length());
                        if (sub.indexOf(sub.length() - 1) == '/') {
                            sub = sub.substring(0, sub.length() - 1);
                        }
                        for (String ignore : ignoreList) {
                            if (sub.contains(ignore)) {
                                return null;
                            }
                        }

                        getLogger().info("loading... " + path.getFileName());
                        String input;
                        try {
                            input = String.join(SEPARATOR, Files.readAllLines(path));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }

                        Parser parser = new Parser(new Lexer(sub, applyDefine(input)));
                        Program program = parser.parseProgram();

                        List<String> errors = parser.getErrors();
                        if (!errors.isEmpty()) {
                            getLogger().severe("parser errors:");
                            errors.forEach(getLogger()::severe);
                            return null;
                        }

                        return program;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<Program, Environment> envMap = new HashMap<>();
            for (Iterator<Program> iter = programList.iterator(); iter.hasNext(); ) {
                Program program = iter.next();
                Environment environment = new Environment(publicEnvironment);
                envMap.put(program, environment);

                Iterator<Statement> iterator = program.getStatements().iterator();
                while (iterator.hasNext()) {
                    Statement statement = iterator.next();

                    if (statement instanceof ImportStatement) {
                        Object evaluated = statement.eval(environment);
                        if (evaluated instanceof ScriptError) {
                            getLogger().severe(String.valueOf(evaluated));
                            iter.remove();
                            break;
                        }

                        iterator.remove();
                    }

                    if (statement instanceof ExpressionStatement) {
                        Expression expression = ((ExpressionStatement) statement).getExpression();
                        if (expression instanceof FunctionDefinition) {
                            Object evaluated = expression.eval(environment);
                            if (evaluated instanceof ScriptError) {
                                getLogger().severe(String.valueOf(evaluated));
                                iter.remove();
                                break;
                            }

                            iterator.remove();
                        }
                    }
                }
            }

            for (Program program : programList) {
                for (Statement statement : program.getStatements()) {
                    Object evaluated = statement.eval(envMap.get(program));
                    if (evaluated instanceof ScriptError) {
                        getLogger().severe(String.valueOf(evaluated));
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        EventAdapter.adapt();

        for (Player player : getServer().getOnlinePlayers()) {
            environmentMap.put(player, EvalUtils.newEnclosedEnvironment(commandEnvironment));
        }
    }

    public static MCScript getInstance() {
        return instance;
    }

    public void eval(CommandSender sender, String input) {
        Parser parser = new Parser(new Lexer(applyDefine(input)));
        Program program = parser.parseProgram();
        List<String> errors = parser.getErrors();
        if (!errors.isEmpty()) {
            sendError(sender, "parser errors:");
            for (String error : errors) {
                sendError(sender, error);
            }
            return;
        }

        Object evaluated = program.eval(sender instanceof Player ? environmentMap.get(sender) : commandEnvironment);
        if (evaluated instanceof ScriptError) {
            sendError(sender, evaluated.toString());
        } else {
            sender.sendMessage(String.valueOf(evaluated));
        }
    }

    private Set<Class> getBukkitClasses() throws UnsupportedEncodingException {
        String path = URLDecoder.decode(Bukkit.class.getResource("Bukkit.class").getPath(), "UTF-8");
        String jarPath = path.substring("file:/".length(), path.lastIndexOf("!"));
        ClassLoader loader = getClassLoader();
        try {
            JarFile jarFile = new JarFile(jarPath);
            return jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .filter(name -> name.startsWith("org/bukkit"))
                    .map(name -> name.replace("/", "."))
                    .map(name -> name.substring(0, name.length() - 6))
                    .map(name -> {
                        try {
                            return loader.loadClass(name);
                        } catch (ClassNotFoundException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(clazz -> !clazz.isAnonymousClass())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptySet();
    }

    private String applyDefine(String str) {
        for (Map.Entry<String, String> entry : defineMap.entrySet()) {
            String macro = entry.getKey();
            String replace = entry.getValue();
            if (MACRO_PATTERN.matcher(macro).matches()) {
                Matcher parenMatcher = PAREN_PATTERN.matcher(macro);
                if (parenMatcher.find()) {
                    String g = parenMatcher.group();
                    List<String> params = Pattern.compile(",")
                            .splitAsStream(g.substring(1, g.length() - 1))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    Matcher matcher = Pattern.compile(macro.substring(0, macro.indexOf('(')) + PAREN_PATTERN).matcher(str);
                    while (matcher.find()) {
                        String group = matcher.group();
                        List<String> args = Pattern.compile(",")
                                .splitAsStream(group.substring(group.indexOf('(') + 1, group.length() - 1))
                                .map(String::trim)
                                .collect(Collectors.toList());

                        if (params.size() != args.size()) {
                            continue;
                        }

                        for (int i = 0; i < params.size(); i++) {
                            replace = replace.replace(params.get(i), args.get(i));
                        }

                        str = str.replace(group, replace);
                        replace = entry.getValue();
                    }
                } else {
                    str = str.replace(macro, replace);
                }
            }
        }
        return str;
    }

    private static void sendError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GREEN + "[ MCScript ] " + ChatColor.WHITE + message);
    }

    public void repl(Player player, boolean start) {
        if (start) {
            if (replSet.add(player)) {
                sendMessage(player, "REPL is enabled");
            } else {
                sendMessage(player, "REPL is already enabled");
            }
        } else {
            if (replSet.remove(player)) {
                sendMessage(player, "REPL is disabled");
            } else {
                sendMessage(player, "REPL is already disabled");
            }
        }
    }

    public static void registerCommand(Command command) {
        Reflection.getCommandMap().register("mcscript", command);
        commandSet.add(command);
    }
}
