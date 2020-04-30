package com.navigation.reactnative;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

public class TabBarItemManager extends ViewGroupManager<TabBarItemView> {

    @Nonnull
    @Override
    public String getName() {
        return "NVTabBarItem";
    }

    @ReactProp(name = "index")
    public void setIndex(TabBarItemView view, int index) {
        view.index = index;
    }

    @ReactProp(name = "title")
    public void setTitle(TabBarItemView view, String title) {
        view.setTitle(title);
    }

    @ReactProp(name = "image")
    public void setImage(TabBarItemView view, @Nullable ReadableMap icon) {
        view.setIconSource(icon);
    }

    @ReactProp(name = "badge")
    public void setBadge(TabBarItemView view, String title) {
        view.badge = title;
    }

    @ReactProp(name = "badgeColor", customType = "Color")
    public void setTitle(TabBarItemView view, Integer color) {
        view.badgeColor = color;
    }

    @Nonnull
    @Override
    protected TabBarItemView createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new TabBarItemView(reactContext);
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onPress", MapBuilder.of("registrationName", "onPress"))
                .build();
    }
}

