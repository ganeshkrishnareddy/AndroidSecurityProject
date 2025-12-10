package com.android.unlock;

import android.app.Application;

/**
 * Created by yezersky on 15-1-2.
 */
public class UnlockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AVService.AVInit(this);
        AVService.AVOpen();
    }
}
