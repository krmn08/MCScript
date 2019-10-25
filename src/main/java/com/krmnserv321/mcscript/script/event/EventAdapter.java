package com.krmnserv321.mcscript.script.event;

import com.krmnserv321.mcscript.MCScript;
import com.krmnserv321.mcscript.script.eval.Environment;
import com.krmnserv321.mcscript.script.eval.ScriptError;
import com.krmnserv321.mcscript.script.eval.EvalUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class EventAdapter implements Listener {

    private static Map<String, EnumMap<EventPriority, EventList>> eventMap = new HashMap<>();

    private static RegisteredListener LOWEST_LISTENER;
    private static RegisteredListener LOW_LISTENER;
    private static RegisteredListener NORMAL_LISTENER;
    private static RegisteredListener HIGH_LISTENER;
    private static RegisteredListener HIGHEST_LISTENER;
    private static RegisteredListener MONITOR_LISTENER;

    private static EventAdapter adapter;

    static {
        adapter = new EventAdapter();

        LOWEST_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onLowest(event)), EventPriority.LOWEST, MCScript.getInstance(), false);
        LOW_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onLow(event)), EventPriority.LOW, MCScript.getInstance(), false);
        NORMAL_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onNormal(event)), EventPriority.NORMAL, MCScript.getInstance(), false);
        HIGH_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onHigh(event)), EventPriority.HIGH, MCScript.getInstance(), false);
        HIGHEST_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onHighest(event)), EventPriority.HIGHEST, MCScript.getInstance(), false);
        MONITOR_LISTENER = new RegisteredListener(adapter, ((listener, event) -> adapter.onMonitor(event)), EventPriority.MONITOR, MCScript.getInstance(), false);
    }

    private EventAdapter() {
    }

    public static void adapt() {
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            handlerList.register(LOWEST_LISTENER);
            handlerList.register(LOW_LISTENER);
            handlerList.register(NORMAL_LISTENER);
            handlerList.register(HIGH_LISTENER);
            handlerList.register(HIGHEST_LISTENER);
            handlerList.register(MONITOR_LISTENER);
        }
    }

    public static void register(EventFunction function) {
        String name = function.getToken() + "Event";
        if (!eventMap.containsKey(name)) {
            eventMap.put(name, new EnumMap<>(EventPriority.class));
        }

        EnumMap<EventPriority, EventList> enumMap = eventMap.get(name);
        if (!enumMap.containsKey(function.getPriority())) {
            enumMap.put(function.getPriority(), new EventList());
        }

        enumMap.get(function.getPriority()).add(function);
    }

    public static void unregisterAll() {
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            handlerList.unregister(LOWEST_LISTENER);
            handlerList.unregister(LOW_LISTENER);
            handlerList.unregister(NORMAL_LISTENER);
            handlerList.unregister(HIGH_LISTENER);
            handlerList.unregister(HIGHEST_LISTENER);
            handlerList.unregister(MONITOR_LISTENER);
        }

        eventMap.clear();
    }

    @EventHandler
    public void onLowest(Event event) {
        callEvent(event, EventPriority.LOWEST);
    }

    @EventHandler
    public void onLow(Event event) {
        callEvent(event, EventPriority.LOW);
    }

    @EventHandler
    public void onNormal(Event event) {
        callEvent(event, EventPriority.NORMAL);
    }

    @EventHandler
    public void onHigh(Event event) {
        callEvent(event, EventPriority.HIGH);
    }

    @EventHandler
    public void onHighest(Event event) {
        callEvent(event, EventPriority.HIGHEST);
    }

    @EventHandler
    public void onMonitor(Event event) {
        callEvent(event, EventPriority.MONITOR);
    }

    private void callEvent(Event event, EventPriority priority) {
        EnumMap<EventPriority, EventList> enumMap = eventMap.get(event.getEventName());
        if (enumMap == null) {
            return;
        }

        EventList eventList = enumMap.get(priority);

        if (eventList == null) {
            return;
        }

        for (EventFunction function : eventList) {
            Environment environment = EvalUtils.newEnclosedEnvironment(function.getEnvironment());
            environment.putConstant("event", event);
            function.setEnvironment(environment);
            Object ret = function.call();
            if (ret instanceof ScriptError) {
                Bukkit.getLogger().severe(String.valueOf(ret));
            }
        }
    }

    private static class EventList extends ArrayList<EventFunction> {
    }
}
