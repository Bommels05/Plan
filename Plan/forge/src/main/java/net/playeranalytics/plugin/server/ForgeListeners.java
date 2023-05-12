package net.playeranalytics.plugin.server;

import net.playeranalytics.plan.gathering.listeners.ForgeListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeListeners implements Listeners {

    private final Set<ForgeListener> listeners;

    public ForgeListeners() {
        this.listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void registerListener(Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can not be null!");
        } else if (listener instanceof ForgeListener) {
            ForgeListener forgeListener = (ForgeListener) listener;
            if (!forgeListener.isEnabled()) {
                forgeListener.register();
                listeners.add(forgeListener);
            }
        } else {
            throw new IllegalArgumentException("Listener needs to be of type ForgeListener, but was " + listener.getClass());
        }
    }

    @Override
    public void unregisterListener(Object listener) {
        ((ForgeListener) listener).disable();
        listeners.remove(listener);
    }

    @Override
    public void unregisterListeners() {
        listeners.forEach(ForgeListener::disable);
        listeners.clear();
    }
}
