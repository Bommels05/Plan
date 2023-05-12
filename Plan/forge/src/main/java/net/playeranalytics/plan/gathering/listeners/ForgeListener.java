package net.playeranalytics.plan.gathering.listeners;

public interface ForgeListener {

    void register();

    boolean isEnabled();

    void enable();

    void disable();

}
