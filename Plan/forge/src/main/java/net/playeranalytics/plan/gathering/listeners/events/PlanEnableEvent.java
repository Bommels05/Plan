package net.playeranalytics.plan.gathering.listeners.events;

import net.minecraftforge.eventbus.api.Event;
import net.playeranalytics.plan.PlanForge;

public class PlanEnableEvent extends Event {

    private final PlanForge plan;

    public PlanEnableEvent(PlanForge plan) {
        this.plan = plan;
    }

    public PlanForge getPlan() {
        return plan;
    }
}
