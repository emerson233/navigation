package com.navigation.reactnative;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager.widget.ViewPager;

public class TabLayoutView extends TabLayout {
    private final MultiDraweeHolder<GenericDraweeHierarchy> tabsHolder =
            new MultiDraweeHolder<>();
    private IconResolver iconResolver;

    class TabIconControllerListener extends IconResolver.IconControllerListener {
        private final ImageView iconView;

        TabIconControllerListener(ImageView iconView, DraweeHolder holder) {
            super(holder);
            this.iconView = iconView;
        }

        @Override
        protected void setDrawable(Drawable d) {
            iconView.setImageDrawable(d);
            post(measureAndLayout);
            requestLayout();
        }
    }


    private GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(getContext().getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFadeDuration(0)
                .build();
    }

    int defaultTextColor;
    int selectedTintColor;
    int unselectedTintColor;

    public TabLayoutView(Context context) {
        super(context);
        if (getTabTextColors() != null)
            selectedTintColor = unselectedTintColor = defaultTextColor = getTabTextColors().getDefaultColor();
        setSelectedTabIndicatorColor(defaultTextColor);
        iconResolver = new IconResolver(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TabBarView tabBar = getTabBar();
        if (tabBar != null) {
            setupWithViewPager(tabBar);
            populateTabIcons();
        }
        tabsHolder.onAttach();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tabsHolder.onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        tabsHolder.onDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        tabsHolder.onAttach();
    }

    void populateTabIcons() {
        tabsHolder.clear();
        TabBarView tabBar = getTabBar();
        if (tabBar != null && tabBar.getAdapter() != null) {
            for(int i = 0; i < getTabCount(); i++) {
                View view;
                TabLayout.Tab tab = getTabAt(i);
                if (tab.getCustomView() != null) {
                    view = tab.getCustomView();
                } else {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, tab.view, false);
                    tab.setCustomView(view);
                }
                TextView textView = view.findViewById(R.id.text);
                textView.setText(tab.getText());
                ImageView iconView = view.findViewById(R.id.icon);
                View badgeDot = view.findViewById(R.id.badge_dot);
                int[][] states = new int[][] {
                        new int[] { android.R.attr.state_selected},
                        new int[] {-android.R.attr.state_selected}
                };

                int[] colors = new int[] {
                        selectedTintColor,
                        unselectedTintColor
                };
                ColorStateList tintColor = new ColorStateList(states, colors);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ImageViewCompat.setImageTintList(iconView, tintColor);
                }
                if (tab.isSelected()) {
                    iconView.setColorFilter(selectedTintColor, PorterDuff.Mode.SRC_IN);
                } else {
                    iconView.setColorFilter(unselectedTintColor, PorterDuff.Mode.SRC_IN);
                }
                textView.setTextColor(tintColor);
                tab.setCustomView(view);
                TabBarItemView itemView = tabBar.getAdapter().tabFragments.get(i).tabBarItem;
                ReadableMap iconSource = itemView.imageResource;
                if (iconSource != null) {
                    DraweeHolder<GenericDraweeHierarchy> holder =
                    DraweeHolder.create(createDraweeHierarchy(), getContext());
                    TabIconControllerListener controllerListener = new TabIconControllerListener(iconView, holder);
                    controllerListener.setIconImageInfo(iconResolver.getIconImageInfo(iconSource));
                    iconResolver.setIconSource(iconSource, controllerListener, holder);
                    tabsHolder.add(holder);
                } else {
                    tab.setIcon(null);
                }
                if (itemView.badgeColor != null) {
                    Drawable wrappedDrawable = DrawableCompat.wrap(badgeDot.getBackground());
                    DrawableCompat.setTint(wrappedDrawable, itemView.badgeColor);
                    badgeDot.setBackground(wrappedDrawable);
                } else {
                    badgeDot.setBackgroundResource(R.drawable.badge_dot);
                }
                if ("BADGE_DOT".equals(itemView.badge)) {
                    badgeDot.setVisibility(View.VISIBLE);
                } else {
                    badgeDot.setVisibility(View.GONE);
                }
            }
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
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
        post(measureAndLayout);
        if (viewPager != null && viewPager.getAdapter() != null) {
            viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    post(measureAndLayout);
                }
            });
        }
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
}
