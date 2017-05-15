package com.wosloveslife.photobrowserlayout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wosloveslife.photobrowserlayout.animations.EnterScreenAnimations;
import com.wosloveslife.photobrowserlayout.animations.ExitScreenAnimations;

/**
 * Created by zhangh on 2017/5/14.
 */

public class PhotoBrowser2 extends FrameLayout {
    private static final String TAG = PhotoBrowser2.class.getSimpleName();

    private static final long IMAGE_TRANSLATION_DURATION = 3000;

    private ImageView mEnlargedImage;
    private ImageView mTransitionImage;

    private AnimatorSet mExitingAnimation;

    private EnterScreenAnimations mEnterScreenAnimations;
    private ExitScreenAnimations mExitScreenAnimations;

    public PhotoBrowser2(@NonNull Context context) {
        this(context, null);
    }

    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoBrowser2(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onCreate() {
        LayoutInflater.from(getContext()).inflate(R.layout.image_details_activity_layout, this);

        mEnlargedImage = (ImageView) findViewById(R.id.enlarged_image);

        final View mainContainer = findViewById(R.id.main_container);

        initializeTransitionView();

        mEnterScreenAnimations = new EnterScreenAnimations(mTransitionImage, mEnlargedImage, mainContainer);
        mExitScreenAnimations = new ExitScreenAnimations(mTransitionImage, mEnlargedImage, mainContainer);

        initializeEnlargedImageAndRunAnimation();
    }

    private void initializeTransitionView() {
        mTransitionImage = new ImageView(getContext());
        addView(mTransitionImage);

        int thumbnailTop = mSharedElement.top - getStatusBarHeight();
        int thumbnailLeft = mSharedElement.left;
        int thumbnailWidth = mSharedElement.width;
        int thumbnailHeight = mSharedElement.height;
        ImageView.ScaleType scaleType = mSharedElement.scaleType;

        // We set initial margins to the view so that it was situated at exact same spot that view from the previous screen were.
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTransitionImage.getLayoutParams();
        layoutParams.height = thumbnailHeight;
        layoutParams.width = thumbnailWidth;
        layoutParams.setMargins(thumbnailLeft, thumbnailTop, 0, 0);

        mTransitionImage.setScaleType(scaleType);

        mTransitionImage.setImageBitmap(mSharedElement.mBitmap);
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
        mEnlargedImage.setImageBitmap(mSharedElement.mBitmap);
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
                    case 0:
                        /*
                         * 1. start animation on first frame
                         */
                        final int[] finalLocationOnTheScreen = new int[2];
                        mEnlargedImage.getLocationOnScreen(finalLocationOnTheScreen);

                        mEnterScreenAnimations.playEnteringAnimation(
                                finalLocationOnTheScreen[0], // left
                                finalLocationOnTheScreen[1], // top
                                mEnlargedImage.getWidth(),
                                mEnlargedImage.getHeight());

                        return true;
                    case 1:
                        /*
                         * 2. Do nothing. We just draw this frame
                         */

                        return true;
                }
                /*
                 * 3.
                 * Make view on previous screen invisible on after this drawing frame
                 * Here we ensure that animated view will be visible when we make the viw behind invisible
                 */
                Log.v(TAG, "run, onAnimationStart");
//                mBus.post(new ChangeImageThumbnailVisibility(false));

                mEnlargedImage.getViewTreeObserver().removeOnPreDrawListener(this);

                Log.v(TAG, "onPreDraw, << mFrames " + mFrames);

                return true;
            }
        });
    }

    public void show() {
        onCreate();
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
}
