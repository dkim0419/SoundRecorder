/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder.didagger2;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger @Module class providing an application Context.
 */
@Module
public class AppModule {
    private Context appContext;

    public AppModule(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return appContext;
    }
}
