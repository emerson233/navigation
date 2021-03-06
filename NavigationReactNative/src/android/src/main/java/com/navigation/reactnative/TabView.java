package com.navigation.reactnative;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public interface TabView {
    void setupWithViewPager(@Nullable ViewPager viewPager);

    int getTabCount();

    void setTitle(int index, String title);

    void setIcon(int index, Drawable icon);
}
