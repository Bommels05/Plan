package net.playeranalytics.plugin.scheduling;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class UnscheduledForgeTask implements UnscheduledTask {

    private final ScheduledExecutorService scheduler;
    private final Runnable runnable;
    private final Consumer<Task> cancellableConsumer;

    public UnscheduledForgeTask(ScheduledExecutorService scheduler, Runnable runnable, Consumer<Task> cancellableConsumer) {
        this.scheduler = scheduler;
        this.runnable = runnable;
        this.cancellableConsumer = cancellableConsumer;
    }

    @Override
    public Task runTaskAsynchronously() {
        ForgeTask task = new ForgeTask(this.scheduler.submit(this.runnable));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTaskLaterAsynchronously(long delayTicks) {
        ForgeTask task = new ForgeTask(this.scheduler.schedule(
                this.runnable,
                delayTicks * 50,
                TimeUnit.MILLISECONDS
        ));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTaskTimerAsynchronously(long delayTicks, long periodTicks) {
        ForgeTask task = new ForgeTask(this.scheduler.scheduleAtFixedRate(
                runnable,
                delayTicks * 50,
                periodTicks * 50,
                TimeUnit.MILLISECONDS
        ));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTask() {
        return runTaskAsynchronously();
    }

    @Override
    public Task runTaskLater(long delayTicks) {
        return runTaskLaterAsynchronously(delayTicks);
    }

    @Override
    public Task runTaskTimer(long delayTicks, long periodTicks) {
        return runTaskTimerAsynchronously(delayTicks, periodTicks);
    }
}
