/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder.didagger2;

import android.app.Application;

/**
 * Custom Application class.
 * It initializes AppComponent for Dagger2.
 */

public class App extends Application {
    private static AppComponent component;

    public static AppComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = buildComponent();
    }

    protected AppComponent buildComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .dBHelperModule(new DBHelperModule())
                .build();
    }
}
