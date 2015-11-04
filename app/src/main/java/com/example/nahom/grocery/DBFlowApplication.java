package com.example.nahom.grocery;

import android.app.Application;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by Nahom on 10/11/15.
 *
 *
 * This is the Application class for DBFlow
 */
public class DBFlowApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
