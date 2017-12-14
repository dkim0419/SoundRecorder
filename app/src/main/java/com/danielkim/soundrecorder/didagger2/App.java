/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
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
