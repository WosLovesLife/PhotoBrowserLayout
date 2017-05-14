package com.wosloveslife.photobrowserlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by zhangh on 2017/5/7.
 */

public class PhotoBrowserLayout extends FrameLayout {

    private ViewPager mViewPager;
    private Adapter mAdapter;
    private boolean mFirst;
    private SharedItem mSharedItem;

    private int mDuration;

    public PhotoBrowserLayout(@NonNull Context context) {
        this(context, null);
    }

    public PhotoBrowserLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoBrowserLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoBrowserLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate();
    }

    private void onCreate() {
        setBackgroundResource(R.color.black);
        mDuration = 1280;

        mViewPager = new ViewPager(getContext());
        addView(mViewPager);

        mAdapter = new Adapter();
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                notice2updatePage(position);
            }
        });
    }

    private View notice2updatePage(int position) {
        if (mOnRequestListener == null) {
            return null;
        }
        View view = mOnRequestListener.onPageChanged(position);
        if (view == null) {
            mSharedItem.mIsOutOfVisible = true;
        }
        return view;
    }

    private int[] addOffset(int[] source) {
        int[] offset = new int[2];
        getLocationOnScreen(offset);
        source[0] = -source[0] + offset[0];
        source[1] = -source[1] + offset[1];
        return source;
    }

    private class Adapter extends PagerAdapter {
        List<Integer> mAddresses;

        @Override
        public int getCount() {
            return mAddresses != null ? mAddresses.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final FrameLayout frameLayout = new FrameLayout(container.getContext());
            final SimpleDraweeView imageView = (SimpleDraweeView) LayoutInflater.from(container.getContext()).inflate(R.layout.item_iamge2, container, false);
            frameLayout.addView(imageView);
            container.addView(frameLayout);
            imageView.setImageResource(mAddresses.get(position));
            imageView.setController(Fresco.newDraweeControllerBuilder().setImageRequest(ImageRequestBuilder.newBuilderWithResourceId(mAddresses.get(position)).build()).build());
            if (mFirst) {
                mFirst = false;
                mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        mSharedItem.mRectReal.set(0, 0, mViewPager.getMeasuredWidth(), mViewPager.getMeasuredHeight());

                        FrameLayout.LayoutParams params = (LayoutParams) imageView.getLayoutParams();
                        params.gravity = Gravity.CENTER;
                        params.width = mSharedItem.mRectDrawable.width();
                        params.height = mSharedItem.mRectDrawable.height();
                        imageView.setLayoutParams(params);

                        mSharedItem.mLocationReal[0] = getScrollX();
                        mSharedItem.mLocationReal[1] = getScrollY();
                        // add the layout marin of screen top
                        mSharedItem.mLocationOrigin = addOffset(mSharedItem.mLocationOrigin);
                        scrollTo(mSharedItem.mLocationOrigin[0], mSharedItem.mLocationOrigin[1]);

                        final int deltaW = mSharedItem.mRectReal.width() - mSharedItem.mRectOrigin.width();
                        final int deltaH = mSharedItem.mRectReal.height() - mSharedItem.mRectOrigin.height();
                        final int deltaX = mSharedItem.mLocationReal[0] - mSharedItem.mLocationOrigin[0];
                        final int deltaY = mSharedItem.mLocationReal[1] - mSharedItem.mLocationOrigin[1];
                        final int deltaWImage = mSharedItem.mRectReal.width() - params.width;
                        final int deltaHImage = mSharedItem.mRectReal.height() - params.height;

                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                        valueAnimator.setDuration(mDuration);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                Float value = (Float) animation.getAnimatedValue();
                                ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
                                params.width = (int) (mSharedItem.mRectOrigin.width() + deltaW * value);
                                params.height = (int) (mSharedItem.mRectOrigin.height() + deltaH * value);
                                mViewPager.setLayoutParams(params);
                                scrollTo((int) (mSharedItem.mLocationOrigin[0] + deltaX * value), (int) (mSharedItem.mLocationOrigin[1] + deltaY * value));

                                ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) imageView.getLayoutParams();
                                layoutParams.width = (int) (mSharedItem.mRectDrawable.width() + deltaWImage * value);
                                layoutParams.height = (int) (mSharedItem.mRectDrawable.height() + deltaHImage * value);
                                imageView.setLayoutParams(layoutParams);

                                getBackground().setAlpha((int) (255 * value));
                            }
                        });
                        valueAnimator.start();
                        mViewPager.post(new Runnable() {
                            @Override
                            public void run() {
                                setVisibility(VISIBLE);
                            }
                        });
                    }
                });
            }
            return frameLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public void setData(List<Integer> res, SharedItem sharedItem, int position) {
        mFirst = true;
        setVisibility(INVISIBLE);
        mSharedItem = sharedItem;
        mAdapter.mAddresses = res;
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(position);
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    private OnRequestListener mOnRequestListener;

    public void setOnRequestListener(OnRequestListener listener) {
        mOnRequestListener = listener;
    }

    public interface OnRequestListener {
        View onPageChanged(int position);
    }

    private OnDismissListener mOnDismissListener;

    public void setOnRequestListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public static class SharedItem {

        private boolean mIsOval;
        private boolean mIsOutOfVisible;
        private Rect mRectOrigin = new Rect();
        private int[] mLocationOrigin = new int[2];
        private Rect mRectReal = new Rect();
        private int[] mLocationReal = new int[2];
        private Rect mRectDrawable;

        public SharedItem(View view) {
            if (view == null) {
                return;
            }
            view.getLocalVisibleRect(mRectOrigin);
            view.getLocationOnScreen(mLocationOrigin);
            mLocationOrigin[0] = mLocationOrigin[0] + mRectOrigin.left;
            mLocationOrigin[1] = mLocationOrigin[1] + mRectOrigin.top;

            if (view instanceof ImageView) {
                mRectDrawable = ((ImageView) view).getDrawable().getBounds();
            }
        }
    }

    public void dismiss(final OnDismissListener onDismiss) {
        if (mTouching) {
            return;
        }

        View newPage = notice2updatePage(mViewPager.getCurrentItem());
        SharedItem sharedItem = new SharedItem(newPage);
        mSharedItem.mRectOrigin = sharedItem.mRectOrigin;
        mSharedItem.mLocationOrigin = addOffset(sharedItem.mLocationOrigin);
        mSharedItem.mRectDrawable = sharedItem.mRectDrawable;

        final int currentAlpha = DrawableCompat.getAlpha(getBackground());
        if (mSharedItem.mIsOutOfVisible) {
            int scrollY = getScrollY();
            int targetY = -getMeasuredHeight();
            final int deltaY = targetY - scrollY;
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "scrollY", scrollY, targetY);
            objectAnimator.setDuration(mDuration);
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Integer) animation.getAnimatedValue();
                    float var = value / deltaY;
                    getBackground().setAlpha((int) (currentAlpha - currentAlpha * var));
                }
            });
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onDismiss.onDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    onDismiss.onDismiss();
                }
            });
            objectAnimator.start();
            return;
        }

        ViewGroup view = (ViewGroup) mViewPager.getChildAt(0);
        final View imageView = view.getChildAt(0);

        final int width = mViewPager.getWidth();
        final int height = mViewPager.getHeight();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();

        final int deltaW = width - mSharedItem.mRectOrigin.width();
        final int deltaH = height - mSharedItem.mRectOrigin.height();
        final int deltaX = scrollX - mSharedItem.mLocationOrigin[0];
        final int deltaY = scrollY - mSharedItem.mLocationOrigin[1];
        final int deltaWImage = width - mSharedItem.mRectDrawable.width();
        final int deltaHImage = height - mSharedItem.mRectDrawable.height();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(mDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
                params.width = (int) (width - deltaW * value);
                params.height = (int) (height - deltaH * value);
                mViewPager.setLayoutParams(params);
                scrollTo((int) (scrollX - deltaX * value), (int) (scrollY - deltaY * value));

                ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) imageView.getLayoutParams();
                layoutParams.width = (int) (width - deltaWImage * value);
                layoutParams.height = (int) (height - deltaHImage * value);
                Log.w(TAG, "onAnimationUpdate: width = " + layoutParams.width +"; height = "+ layoutParams.height);
                imageView.setLayoutParams(layoutParams);

                getBackground().setAlpha((int) (currentAlpha - currentAlpha * value));

                if (value == 1) {
                    onDismiss.onDismiss();
                }
            }
        });
        valueAnimator.start();
    }

    boolean mTouching;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouching = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
