package com.navigation.reactnative;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.List;

public class TabBarView extends ViewPager {
    private List<TabBarItemView> tabs = new ArrayList<>();
    boolean swipeable = true;

    public TabBarView(Context context) {
        super(context);
        addOnPageChangeListener(new TabChangeListener());
        Activity activity = ((ReactContext) context).getCurrentActivity();
        setAdapter(new Adapter(getFragmentManager(activity)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestLayout();
        post(measureAndLayout);
        if (getTabView() != null)
            getTabView().setupWithViewPager(this);
    }

    TabLayoutView getTabLayout() {
        for (int i = 0; getParent() != null && i < ((ViewGroup) getParent()).getChildCount(); i++) {
            View child = ((ViewGroup) getParent()).getChildAt(i);
            if (child instanceof TabLayoutView)
                return (TabLayoutView) child;
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

    private FragmentManager getFragmentManager(Activity activity) {
        ViewParent parent = this;
        Fragment fragment = null;
        while (parent != null) {
            if (parent instanceof NavigationBoundary) {
                fragment = ((NavigationBoundary) parent).getFragment();
                break;
            }
            parent = parent.getParent();
        }
        return fragment == null? ((FragmentActivity) activity).getSupportFragmentManager():  fragment.getChildFragmentManager();
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
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
        return getAdapter() != null ? getAdapter().tabFragments.size() : 0;
    }

    TabBarItemView getTabAt(int index) {
        return getAdapter() != null ? getAdapter().tabFragments.get(index).tabBarItem : null;
    }

    void addTab(TabBarItemView tab, int index) {
        if (getAdapter() != null)
            getAdapter().addTab(tab, index);
    }

    void removeTab(int index) {
        if (getAdapter() != null)
            getAdapter().removeTab(index);
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

    class Adapter extends FragmentPagerAdapter {
        List<TabFragment> tabFragments = new ArrayList<>();
        FragmentManager fragmentManager;

        Adapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.fragmentManager = fragmentManager;
        }

        void addTab(TabBarItemView tab, int index) {
            for (TabFragment fragment: tabFragments)  {
                if (fragment.tabBarItem == tab) {
                    return;
                }
            }
            tabFragments.add(index, new TabFragment(tab));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                tab.setElevation(-1 * index);
            notifyDataSetChanged();
            setOffscreenPageLimit(tabFragments.size() + 1);
        }

        void removeTab(int index) {
            tabFragments.remove(index);
            notifyDataSetChanged();
            setOffscreenPageLimit(tabFragments.size() + 1);
        }

        @Override
        public int getCount() {
            return tabFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabFragments.get(position).tabBarItem.title;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public long getItemId(int position) {
            return tabFragments.get(position).hashCode();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            post(measureAndLayout);
            return super.instantiateItem(container, position);
        }
    }

    private class TabChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            WritableMap event = Arguments.createMap();
            event.putInt("tab", position);
            ReactContext reactContext = (ReactContext) getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(),"onTabSelected", event);
            if (getAdapter() != null)
                getAdapter().tabFragments.get(position).tabBarItem.pressed();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
