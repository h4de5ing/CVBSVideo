package com.example.twovideotest.notifity;

import java.util.ArrayList;
import java.util.List;
//https://blog.csdn.net/jason0539/article/details/45055233

public class Transporter implements Watched {
    private List<Watcher> list = new ArrayList<Watcher>();

    @Override
    public void addWatcher(Watcher watcher) {
        list.add(watcher);
    }

    @Override
    public void removeWatcher(Watcher watcher) {
        list.remove(watcher);
    }

    @Override
    public void notifyWatchers() {
        for (Watcher watcher : list)
            watcher.update();
    }
}
