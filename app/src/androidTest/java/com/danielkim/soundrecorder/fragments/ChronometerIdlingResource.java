package com.danielkim.soundrecorder.fragments;

import androidx.test.espresso.IdlingResource;

public class ChronometerIdlingResource implements IdlingResource {
    private volatile ResourceCallback resourceCallback;
    private final long startTime;
    private final long waitingTime;

    public ChronometerIdlingResource(long waitingTime) {
        this.startTime = System.currentTimeMillis();
        this.waitingTime = waitingTime;
    }

    @Override
    public String getName() {
        return ChronometerIdlingResource.class.getName() + ":" + waitingTime;
    }

    @Override
    public boolean isIdleNow() {
        long elapsed = System.currentTimeMillis() - startTime;
        boolean isIdle = (elapsed >= waitingTime);
        if (isIdle) resourceCallback.onTransitionToIdle();
        return isIdle;    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }
}
