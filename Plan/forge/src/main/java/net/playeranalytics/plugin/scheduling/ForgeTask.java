package net.playeranalytics.plugin.scheduling;

import java.util.concurrent.Future;

public class ForgeTask implements Task {

    private final Future<?> task;

    public ForgeTask(Future<?> task) {
        this.task = task;
    }

    @Override
    public boolean isGameThread() {
        return false;
    }

    @Override
    public void cancel() {
        task.cancel(false);
    }
}
