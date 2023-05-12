package net.playeranalytics.plugin.scheduling;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ForgeRunnableFactory implements RunnableFactory {

    private final ScheduledExecutorService executorService;
    private final Set<ForgeTask> tasks;

    public ForgeRunnableFactory() {
        this.executorService = Executors.newScheduledThreadPool(8);
        this.tasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public UnscheduledTask create(Runnable runnable) {
        return new UnscheduledForgeTask(getExecutorService(), runnable, task -> {
        });
    }

    private ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public UnscheduledTask create(PluginRunnable runnable) {
        return new UnscheduledForgeTask(getExecutorService(), runnable, runnable::setCancellable);
    }

    @Override
    public void cancelAllKnownTasks() {
        this.tasks.forEach(Task::cancel);
        this.tasks.clear();
        executorService.shutdown();
    }
}
