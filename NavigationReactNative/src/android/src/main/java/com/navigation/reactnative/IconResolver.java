package com.navigation.reactnative;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.PixelUtil;

import androidx.annotation.Nullable;

class IconResolver {
    private Context context;

    IconResolver(Context context) {
        this.context = context;
    }

    private static class CustomDrawableWithIntrinsicSize extends ForwardingDrawable implements Drawable.Callback {
        private final ImageInfo imageInfo;

        CustomDrawableWithIntrinsicSize(Drawable drawable, ImageInfo imageInfo) {
            super(drawable);
            this.imageInfo = imageInfo;
        }

        @Override
        public int getIntrinsicWidth() {
            return imageInfo.getWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return imageInfo.getHeight();
        }
    }

    private static class DrawableWithIntrinsicSize extends BitmapDrawable {
        private int width;
        private int height;

        DrawableWithIntrinsicSize(Resources resources, Bitmap bitmap, ReadableMap source) {
            super(resources, bitmap);
            width = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_WIDTH)));
            height = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_HEIGHT)));
        }

        @Override
        public int getIntrinsicWidth() {
            return width;
        }

        @Override
        public int getIntrinsicHeight() {
            return height;
        }
    }

    interface IconResolverListener {
        void setDrawable(Drawable d);
    }

    static void setIconSource(final ReadableMap source, final IconResolverListener iconResolverListener, final Context context) {
        String uri = source != null ? source.getString(PROP_ICON_URI) : null;
        if (uri == null) {
            iconResolverListener.setDrawable(null);
        } else if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("file://")) {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri));
            ImageRequest request = builder.build();
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, context);
            dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                @Override
                protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    if (!dataSource.isFinished()) {
                        return;
                    }
                    CloseableReference<CloseableImage> imageRef = dataSource.getResult();
                    if (imageRef != null) {
                        CloseableImage image = imageRef.get();
                        try {
                            if (image instanceof CloseableBitmap) {
                                Bitmap bitmap = (((CloseableBitmap) image).getUnderlyingBitmap());
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);
                                    Drawable drawable = new DrawableWithIntrinsicSize(context.getResources(), bitmapCopy, source);
                                    iconResolverListener.setDrawable(drawable);
                                }
                            }
                        } finally {
                            imageRef.close();
                        }
                    }
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                }
            }, UiThreadImmediateExecutorService.getInstance());
        } else {
            int drawableResId = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
            if (drawableResId != 0)
                iconResolverListener.setDrawable(context.getResources().getDrawable(drawableResId));
        }
    }

    abstract static class IconControllerListener extends BaseControllerListener<ImageInfo> {
        private final DraweeHolder holder;
        private IconImageInfo iconImageInfo;

        IconControllerListener(DraweeHolder holder) {
            this.holder = holder;
        }

        void setIconImageInfo(IconImageInfo iconImageInfo) {
            this.iconImageInfo = iconImageInfo;
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);
            final ImageInfo info = iconImageInfo != null ? iconImageInfo : imageInfo;
            setDrawable(new CustomDrawableWithIntrinsicSize(holder.getTopLevelDrawable(), info));
        }

        protected abstract void setDrawable(Drawable d);

        @Override
        public void onFailure(String id, Throwable throwable) {
            super.onFailure(id, throwable);
        }
    }

    private static class IconImageInfo implements ImageInfo {
        private int width;
        private int height;

        IconImageInfo(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public QualityInfo getQualityInfo() {
            return null;
        }

    }

    void setIconSource(ReadableMap source, IconControllerListener controllerListener, DraweeHolder holder) {
        String uri = source != null ? source.getString(PROP_ICON_URI) : null;
        if (uri == null) {
            controllerListener.setIconImageInfo(null);
            controllerListener.setDrawable(null);
        } else if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("file://")) {
            controllerListener.setIconImageInfo(getIconImageInfo(source));
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(uri))
                    .setControllerListener(controllerListener)
                    .setOldController(holder.getController())
                    .build();
            holder.setController(controller);
            holder.getTopLevelDrawable().setVisible(true, true);
        } else {
            controllerListener.setDrawable(getDrawableByName(uri));
        }
    }

    private int getDrawableResourceByName(String name) {
        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());
    }

    private Drawable getDrawableByName(String name) {
        int drawableResId = getDrawableResourceByName(name);
        if (drawableResId != 0) {
            return context.getResources().getDrawable(getDrawableResourceByName(name));
        } else {
            return null;
        }
    }

    private static final String PROP_ICON_URI = "uri";
    private static final String PROP_ICON_WIDTH = "width";
    private static final String PROP_ICON_HEIGHT = "height";

    IconImageInfo getIconImageInfo(ReadableMap source) {
        if (source.hasKey(PROP_ICON_WIDTH) && source.hasKey(PROP_ICON_HEIGHT)) {
            final int width = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_WIDTH)));
            final int height = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_HEIGHT)));
            return new IconImageInfo(width, height);
        } else {
            return null;
        }
    }
}