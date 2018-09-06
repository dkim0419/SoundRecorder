package by.naxa.soundrecorder;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

public class SoundRecorderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        if (BuildConfig.USE_CRASHLYTICS) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        }
    }

}
