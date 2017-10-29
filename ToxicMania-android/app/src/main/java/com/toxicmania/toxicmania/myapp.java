package com.toxicmania.toxicmania;

import android.app.Application;
import android.os.SystemClock;

/**
 * Created by PM Artz on 10/12/2017.
 */

public class myapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(2000);
    }
}
