package com.navigation.reactnative;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class TabNavigationView extends BottomNavigationView implements TabView {
    boolean bottomTabs;
    int defaultTextColor;
    int selectedTintColor;
    int unselectedTintColor;
    private ViewPager.OnPageChangeListener pageChangeListener;
    private DataSetObserver dataSetObserver;

    public TabNavigationView(Context context) {
        super(context);
        //set to show all the TabTarItem label
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        TabLayoutView tabLayout = new TabLayoutView(context);
        selectedTintColor = unselectedTintColor = defaultTextColor = tabLayout.defaultTextColor;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TabBarView tabBar = getTabBar();
        if (bottomTabs && tabBar != null) {
            setupWithViewPager(tabBar);
            //show unread RedDot view
            initRedDotView();
            tabBar.populateTabs();
        }
    }

    private TabBarView getTabBar() {
        for(int i = 0; getParent() != null && i < ((ViewGroup) getParent()).getChildCount(); i++) {
            View child = ((ViewGroup) getParent()).getChildAt(i);
            if (child instanceof TabBarView)
                return (TabBarView) child;
        }
        return null;
    }

    @Override
    public void setupWithViewPager(@Nullable final ViewPager viewPager) {
        if (viewPager != null && viewPager.getAdapter() != null) {
            final PagerAdapter pagerAdapter = viewPager.getAdapter();
            buildMenu(pagerAdapter);
            setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    viewPager.setCurrentItem(menuItem.getOrder(), false);
                    return true;
                }
            });
            if (pageChangeListener != null)
                viewPager.removeOnPageChangeListener(pageChangeListener);
            pageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    setSelectedItemId(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
            viewPager.addOnPageChangeListener(pageChangeListener);
            if (dataSetObserver != null)
                pagerAdapter.unregisterDataSetObserver(dataSetObserver);
            dataSetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    buildMenu(pagerAdapter);
                    setSelectedItemId(viewPager.getCurrentItem());
                }
            };
            pagerAdapter.registerDataSetObserver(dataSetObserver);
            setSelectedItemId(viewPager.getCurrentItem());
        }
    }

    private void buildMenu(PagerAdapter pagerAdapter) {
        getMenu().clear();
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            getMenu().add(Menu.NONE, i, i, pagerAdapter.getPageTitle(i));
        }
        requestLayout();
        post(measureAndLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initRedDotView() {
        BottomNavigationMenuView menuView = null;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof BottomNavigationMenuView) {
                menuView = (BottomNavigationMenuView) child;
                break;
            }
        }
        TabBarView tabBar = getTabBar();
        if (menuView != null && tabBar != null && tabBar.getAdapter() != null) {
            int dp8 = getResources().getDimensionPixelSize(R.dimen.space_8);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView.LayoutParams params = new BottomNavigationItemView.LayoutParams(dp8, dp8);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                params.leftMargin = dp8 * 2;
                params.topMargin = dp8 / 2;
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
                DotView dotView = new DotView(this.getContext());
                dotView.setBackgroundColor(Color.RED);
                TabBarItemView tabBarItemView = tabBar.getAdapter().tabFragments.get(i).tabBarItem;
                if (tabBarItemView.badgeColor != null) {
                    Drawable wrappedDrawable = DrawableCompat.wrap(dotView.getBackground());
                    DrawableCompat.setTint(wrappedDrawable, tabBarItemView.badgeColor);
                    dotView.setBackground(wrappedDrawable);
                } else {
                    dotView.setBackgroundResource(R.drawable.badge_dot);
                }
                if ("BADGE_DOT".equals(tabBarItemView.badge)) {
                    itemView.addView(dotView, params);
                } else {
                    for (int j = 0; j < itemView.getChildCount(); j++) {
                        View view = itemView.getChildAt(j);
                        if (view instanceof DotView) {
                            itemView.removeView(view);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getTabCount() {
        return getMenu().size();
    }

    @Override
    public void setTitle(int index, String title) {
        getMenu().getItem(index).setTitle(title);
        post(measureAndLayout);
    }

    public void setIcon(int index, Drawable icon) {
        getMenu().getItem(index).setIcon(icon);
        post(measureAndLayout);
    }

    final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };
}
