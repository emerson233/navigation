package com.navigation.reactnative;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TabBarView extends ViewPager {
    int selectedTab = 0;
    boolean swipeable = true;
    private boolean layoutRequested = false;
    int nativeEventCount;
    int mostRecentEventCount;
    private boolean dataSetChanged = false;

    public TabBarView(Context context) {
        super(context);
        addOnPageChangeListener(new TabChangeListener());
        Adapter adapter = new Adapter();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (getCurrentItem() != selectedTab && getTabsCount() > selectedTab)
                    setCurrentItem(selectedTab, false);
            }
        });
        setAdapter(adapter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestLayout();
        if (getTabView() != null)
            getTabView().setupWithViewPager(this);
        populateTabs();
    }

    void populateTabs() {
        TabView tabView = getTabView();
        if (tabView != null && getAdapter() != null) {
            for(int i = 0; i < tabView.getTabCount(); i++) {
                getAdapter().tabs.get(i).setTabView(tabView, i);
            }
        }
    }

    TabLayoutView getTabLayout() {
        for (int i = 0; getParent() != null && i < ((ViewGroup) getParent()).getChildCount(); i++) {
            View child = ((ViewGroup) getParent()).getChildAt(i);
            if (child instanceof TabLayoutView)
                return (TabLayoutView) child;
        }
        return null;
    }

    TabNavigationView getTabNavigation() {
        for (int i = 0; getParent() != null && i < ((ViewGroup) getParent()).getChildCount(); i++) {
            View child = ((ViewGroup) getParent()).getChildAt(i);
            if (child instanceof TabNavigationView)
                return (TabNavigationView) child;
        }
        return null;
    }

    TabView getTabView() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent instanceof CoordinatorLayout) {
            parent = (ViewGroup) parent.getChildAt(0);
        }
        for(int i = 0; parent != null && i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TabView)
                return (TabView) child;
        }
        return null;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (!layoutRequested) {
            layoutRequested = true;
            post(measureAndLayout);
        }
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            layoutRequested = false;
            measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Nullable
    @Override
    public Adapter getAdapter() {
        return (Adapter) super.getAdapter();
    }

    int getTabsCount() {
        return getAdapter() != null ? getAdapter().tabs.size() : 0;
    }

    View getTabAt(int index) {
        return getAdapter() != null ? getAdapter().tabs.get(index).content.get(0) : null;
    }

    void addTab(TabBarItemView tab, int index) {
        if (getAdapter() != null) {
            getAdapter().addTab(tab, index);
            populateTabs();
        }
    }

    void removeTab(int index) {
        if (getAdapter() != null) {
            getAdapter().removeTab(index);
            populateTabs();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            if (swipeable && super.onInterceptTouchEvent(ev)) {
                NativeGestureUtil.notifyNativeGestureStarted(this, ev);
                return true;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return swipeable && super.onTouchEvent(ev);
        } catch (IllegalArgumentException ignored) {
        }

        return false;
    }

    private class Adapter extends PagerAdapter {
        private List<TabBarItemView> tabs = new ArrayList<>();

        void addTab(TabBarItemView tab, int index) {
            tabs.add(index, tab);
            dataSetChanged = true;
            notifyDataSetChanged();
            dataSetChanged = false;
        }

        void removeTab(int index) {
            tabs.remove(index);
            dataSetChanged = true;
            notifyDataSetChanged();
            dataSetChanged = false;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position).title;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            for(int i = 0; i < tabs.size(); i++) {
                TabBarItemView tab = tabs.get(i);
                if (tab.content.get(0) == object)
                    return i;
            }
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            TabBarItemView tab = tabs.get(position);
            container.addView(tab.content.get(0), 0);
            return tab.content.get(0);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    private class TabChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (!dataSetChanged)
                nativeEventCount++;
            selectedTab = position;
            WritableMap event = Arguments.createMap();
            event.putInt("tab", position);
            event.putInt("eventCount", nativeEventCount);
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(),"onTabSelected", event);
            if (getAdapter() != null)
                getAdapter().tabs.get(position).pressed();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
