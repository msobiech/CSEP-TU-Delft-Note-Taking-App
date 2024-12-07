package client.utils;

import java.util.Timer;

public class DebounceService {
    Timer debounceTimer = new Timer();

    public void runTask(Runnable task) {
        if(task != null) {
            debounceTimer.cancel();
            task.run();
            task = null;
        }
    }

    public void debounce() {

    }
}
