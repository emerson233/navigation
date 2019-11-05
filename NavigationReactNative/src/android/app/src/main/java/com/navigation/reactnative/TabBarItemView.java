package com.navigation.reactnative;

import android.content.Context;
import android.view.ViewGroup;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TabBarItemView extends ViewGroup implements NavigationBoundary {
    Fragment fragment;
    protected String title;
    protected @Nullable ReadableMap imageResource;
    protected Integer badgeColor;
    protected String badge;


    public TabBarItemView(Context context) {
        super(context);
    }

    void setImage(@Nullable ReadableMap image) {
        imageResource = image;
    }

    protected void pressed() {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(),"onPress", null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }
}
