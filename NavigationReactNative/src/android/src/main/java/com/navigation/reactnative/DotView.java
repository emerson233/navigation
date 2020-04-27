package com.navigation.reactnative;

import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

public class DotView extends AppCompatTextView {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DotView(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
