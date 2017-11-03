/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder.didagger2;

import android.content.Context;
import android.support.annotation.NonNull;

import com.danielkim.soundrecorder.database.DBHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger @Module class providing a DBHelper object (Singleton for the app's lifecycle scope) to
 * interact with local database.
 */
@Module
public class DBHelperModule {
    @Provides
    @Singleton
    @NonNull
    public DBHelper provideDBHelper(Context context) {
        return new DBHelper(context);
    }
}
