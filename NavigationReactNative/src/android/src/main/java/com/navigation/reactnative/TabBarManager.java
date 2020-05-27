package com.navigation.reactnative;

import android.view.View;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

public class TabBarManager extends ViewGroupManager<TabBarView> {

    @Nonnull
    @Override
    public String getName() {
        return "NVTabBar";
    }

    @Nonnull
    @Override
    protected TabBarView createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new TabBarView(reactContext);
    }

    @ReactProp(name = "selectedTab")
    public void setSelectedTab(TabBarView view, int selectedTab) {
        int eventLag = view.nativeEventCount - view.mostRecentEventCount;
        if (eventLag == 0 && view.getCurrentItem() != selectedTab) {
            view.selectedTab = selectedTab;
            if (view.getTabsCount() > selectedTab)
                view.setCurrentItem(selectedTab, false);
        }
    }

    @ReactProp(name = "mostRecentEventCount")
    public void setMostRecentEventCount(TabBarView view, int mostRecentEventCount) {
        view.mostRecentEventCount = mostRecentEventCount;
    }

    @ReactProp(name = "tabCount")
    public void setImages(TabBarView view, int tabCount) {
    }

    @ReactProp(name = "swipeable")
    public void setSwipeable(TabBarView view, boolean swipeable) {
        view.swipeable = swipeable;
    }

    @ReactProp(name = "badges")
    public void setBadges(TabBarView view, ReadableArray badges) {
    }

    @Override
    public int getChildCount(TabBarView parent) {
        return parent.getTabsCount();
    }

    @Override
    public View getChildAt(TabBarView parent, int index) {
        return parent.getTabAt(index);
    }

    @Override
    public void addView(TabBarView parent, View child, int index) {
        parent.addTab((TabBarItemView) child, index);
    }

    @Override
    public void removeViewAt(TabBarView parent, int index) {
        parent.removeTab(index);
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onTabSelected", MapBuilder.of("registrationName", "onTabSelected"))
                .build();
    }

    @Override
    protected void onAfterUpdateTransaction(@Nonnull TabBarView view) {
        super.onAfterUpdateTransaction(view);
        if (view.getTabLayout() != null) {
            view.getTabLayout().populateTabIcons();
        }
        if (view.getTabNavigation() != null) {
            view.getTabNavigation().initRedDotView();
        }
    }
}
