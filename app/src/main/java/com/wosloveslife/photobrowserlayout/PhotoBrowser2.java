package com.wosloveslife.photobrowserlayout;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.wosloveslife.photobrowserlayout.animations.EnterScreenAnimations;
import com.wosloveslife.photobrowserlayout.animations.ExitScreenAnimations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.R.attr.resource;
import static android.R.attr.scaleType;

/**
 * Created by zhangh on 2017/5/14.
 */

public class PhotoBrowser2 extends FrameLayout {
    private static final String TAG = PhotoBrowser2.class.getSimpleName();

    private SimpleDraweeView mEnlargedImage;
    private ImageView mTransitionImage;

    private EnterScreenAnimations mEnterScreenAnimations;
    private ExitScreenAnimations mExitScreenAnimations;
    private ViewPager mViewPager;
    private Adapter mAdapter;

    private boolean mFirst;

    public PhotoBrowser2(@NonNull Context context) {
        this(context, null);
    }

    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate();
    }

    protected void onCreate() {
        LayoutInflater.from(getContext()).inflate(R.layout.image_details_activity_layout, this);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new Adapter();
        mViewPager.setAdapter(mAdapter);
    }

    private class Adapter extends PagerAdapter {
        List<String> res;

        @Override
        public int getCount() {
            return res != null ? res.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_iamge2, container, false);
            final SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.simpleDraweeView);
            imageView.setImageBitmap(mSharedElement.mBitmap);
            final String address = res.get(position);
            if (mFirst) {
                mFirst = false;
                imageView.setVisibility(INVISIBLE);
                mEnlargedImage = imageView;
                mEnlargedImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEnlargedImage.setImageURI(address);
                    }
                }, 0);
                prepare();
            } else {
                imageView.setImageURI(address);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void prepare() {
        final View mainContainer = findViewById(R.id.main_container);

        initializeTransitionView();

        mEnterScreenAnimations = new EnterScreenAnimations(mTransitionImage, mEnlargedImage, mainContainer);
        mExitScreenAnimations = new ExitScreenAnimations(mTransitionImage, mEnlargedImage, mainContainer);

        initializeEnlargedImageAndRunAnimation();
    }

    private void initializeTransitionView() {
        mTransitionImage = new ImageView(getContext());

        int thumbnailTop = mSharedElement.top - getStatusBarHeight();
        int thumbnailLeft = mSharedElement.left;
        int thumbnailWidth = mSharedElement.width;
        int thumbnailHeight = mSharedElement.height;
        ImageView.ScaleType scaleType = mSharedElement.scaleType;

        // We set initial margins to the view so that it was situated at exact same spot that view from the previous screen were.
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(thumbnailWidth, thumbnailHeight);
        layoutParams.setMargins(thumbnailLeft, thumbnailTop, 0, 0);

        mTransitionImage.setScaleType(scaleType);

        addView(mTransitionImage, layoutParams);

        mTransitionImage.setImageBitmap(mSharedElement.mBitmap);
//        mTransitionImage.setImageResource(R.drawable.img1);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initializeEnlargedImageAndRunAnimation() {
        mEnlargedImage.getHierarchy().setPlaceholderImage(new BitmapDrawable(mSharedElement.mBitmap));
        runEnteringAnimation();
    }

    private void runEnteringAnimation() {
        Log.v(TAG, "runEnteringAnimation, addOnPreDrawListener");

        mEnlargedImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            int mFrames = 0;

            @Override
            public boolean onPreDraw() {
                // When this method is called we already have everything laid out and measured so we can start our animation
                Log.v(TAG, "onPreDraw, mFrames " + mFrames);

                switch (mFrames++) {
                    case 1:
                        /* 1. start animation on first frame */
                        final int[] finalLocationOnTheScreen = new int[2];
                        mEnlargedImage.getLocationOnScreen(finalLocationOnTheScreen);

                        mEnterScreenAnimations.playEnteringAnimation(
                                finalLocationOnTheScreen[0], // left
                                finalLocationOnTheScreen[1], // top
                                mEnlargedImage.getWidth(),
                                mEnlargedImage.getHeight());

                        return true;
                    case 0:
                        /* 2. Do nothing. We just draw this frame */

                        return true;
                }
                /*
                 * 3.
                 * Make view on previous screen invisible on after this drawing frame
                 * Here we ensure that animated view will be visible when we make the viw behind invisible
                 */
                Log.v(TAG, "run, onAnimationStart");

                mEnlargedImage.getViewTreeObserver().removeOnPreDrawListener(this);

                Log.v(TAG, "onPreDraw, << mFrames " + mFrames);

                return true;
            }
        });
    }

    public void setData(List<String> res, int position) {
        mAdapter.res = res;
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(position);
    }

    public void show() {
        mFirst = true;
    }

    SharedElement mSharedElement;

    public void setSharedElement(SharedElement sharedElement) {
        mSharedElement = sharedElement;
    }

    public static class SharedElement {
        int left;
        int top;
        int width;
        int height;
        ImageView.ScaleType scaleType;
        Bitmap mBitmap;

        public SharedElement(int left, int top, int width, int height, ImageView.ScaleType scaleType, Bitmap bitmap) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.scaleType = scaleType;
            this.mBitmap = bitmap;
        }
    }

    public void onBackPressed(Animator.AnimatorListener listener) {
        mEnterScreenAnimations.cancelRunningAnimations();

        int toTop = mSharedElement.top;
        int toLeft = mSharedElement.left;
        int toWidth = mSharedElement.width;
        int toHeight = mSharedElement.height;

        mExitScreenAnimations.playExitAnimations(
                toTop,
                toLeft,
                toWidth,
                toHeight,
                mEnterScreenAnimations.getInitialThumbnailMatrixValues());
        mExitScreenAnimations.addListener(listener);
    }

    public static Bitmap getCacheBitmap(Uri uri) {
        Bitmap bitmap = null;
        Fresco.getImagePipelineFactory().getBitmapMemoryCache().get(new SimpleCacheKey(uri.toString()));

        FileBinaryResource resource = (FileBinaryResource) Fresco.getImagePipelineFactory().getMainDiskStorageCache().getResource(new SimpleCacheKey(uri.toString()));
        if (resource == null) {
            return null;
        }
        File file = resource.getFile();
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;

    }

    public static ImageView.ScaleType fbType2ImgType(ScalingUtils.ScaleType scaleType) {
        if (scaleType == ScalingUtils.ScaleType.CENTER_CROP) {
            return ImageView.ScaleType.CENTER_CROP;
        } else if (scaleType == ScalingUtils.ScaleType.FOCUS_CROP) {
            return ImageView.ScaleType.MATRIX;
        } else {
            return ImageView.ScaleType.FIT_CENTER;
        }
    }
}
