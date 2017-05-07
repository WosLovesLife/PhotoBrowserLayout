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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

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
        mDuration = 1000;

        mViewPager = new ViewPager(getContext());
        addView(mViewPager);

        mAdapter = new Adapter();
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mOnRequestListener == null) {
                    return;
                }
                View view = mOnRequestListener.onPageChanged(position);
                if (view == null) {
                    mSharedItem.mIsOutOfVisible = true;
                    return;
                }
                SharedItem sharedItem = new SharedItem(view);
                mSharedItem.mRectOrigin = sharedItem.mRectOrigin;
                int[] offset = new int[2];
                getLocationOnScreen(offset);
                sharedItem.mLocationOrigin[0] = -sharedItem.mLocationOrigin[0] + offset[0];
                sharedItem.mLocationOrigin[1] = -sharedItem.mLocationOrigin[1] + offset[1];
                mSharedItem.mLocationOrigin = sharedItem.mLocationOrigin;
            }
        });
    }

    class Adapter extends PagerAdapter {
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
            final SimpleDraweeView imageView = (SimpleDraweeView) LayoutInflater.from(container.getContext()).inflate(R.layout.item_iamge2, container, false);
            container.addView(imageView);
            imageView.setImageResource(mAddresses.get(position));
            imageView.setController(Fresco.newDraweeControllerBuilder().setImageRequest(ImageRequestBuilder.newBuilderWithResourceId(mAddresses.get(position)).build()).build());
            if (mFirst) {
                mFirst = false;
                mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        Rect bounds = imageView.getDrawable().getBounds();
//                        final int originMarginW = -(bounds.width() - mSharedItem.mRectOrigin.width());
//                        final int originMarginH = -(bounds.height() - mSharedItem.mRectOrigin.height());
//                        mSharedItem.mRectOrigin = bounds;
//                        ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) mViewPager.getLayoutParams();
//                        final int realMarginW = layoutParams.rightMargin;
//                        final int realMarginH = layoutParams.bottomMargin;
//                        layoutParams.rightMargin = originMarginW;
//                        layoutParams.bottomMargin = originMarginH;
//                        mViewPager.setLayoutParams(layoutParams);

//                        imageView.getDrawable().setBounds(0,0,mSharedItem.mRectOrigin.width(),mSharedItem.mRectOrigin.height());
//                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        mSharedItem.mRectReal.set(0, 0, mViewPager.getMeasuredWidth(), mViewPager.getMeasuredHeight());
                        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
                        params.width = mSharedItem.mRectOrigin.width();
                        params.height = mSharedItem.mRectOrigin.height();
                        mViewPager.setLayoutParams(params);

                        mSharedItem.mLocationReal[0] = getScrollX();
                        mSharedItem.mLocationReal[1] = getScrollY();
                        int[] offset = new int[2];
                        getLocationOnScreen(offset);
                        mSharedItem.mLocationOrigin[0] = -mSharedItem.mLocationOrigin[0] + offset[0];
                        mSharedItem.mLocationOrigin[1] = -mSharedItem.mLocationOrigin[1] + offset[1];
                        scrollTo(mSharedItem.mLocationOrigin[0], mSharedItem.mLocationOrigin[1]);

                        final int deltaW = mSharedItem.mRectReal.width() - mSharedItem.mRectOrigin.width();
                        final int deltaH = mSharedItem.mRectReal.height() - mSharedItem.mRectOrigin.height();
                        final int deltaX = mSharedItem.mLocationReal[0] - mSharedItem.mLocationOrigin[0];
                        final int deltaY = mSharedItem.mLocationReal[1] - mSharedItem.mLocationOrigin[1];
//                        final int deltaMarginW = realMarginW - originMarginW;
//                        final int deltaMarginH = realMarginH - originMarginH;

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

//                                ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) mViewPager.getLayoutParams();
//                                layoutParams.rightMargin = (int) (originMarginW + deltaMarginW * value);
//                                layoutParams.bottomMargin = (int) (originMarginH + deltaMarginH * value);
//                                mViewPager.setLayoutParams(layoutParams);
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
            return imageView;
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

        public SharedItem(View view) {
            if (view == null) {
                return;
            }
            view.getLocalVisibleRect(mRectOrigin);
            view.getLocationOnScreen(mLocationOrigin);
            mLocationOrigin[0] = mLocationOrigin[0] + mRectOrigin.left;
            mLocationOrigin[1] = mLocationOrigin[1] + mRectOrigin.top;
        }
    }

    public void dismiss(final OnDismissListener onDismiss) {
        if (mSharedItem.mIsOutOfVisible) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "scrollY", getScrollY(), -getMeasuredHeight());
            objectAnimator.setDuration(mDuration);
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

        final int width = mViewPager.getWidth();
        final int height = mViewPager.getHeight();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();

        final int deltaW = width - mSharedItem.mRectOrigin.width();
        final int deltaH = height - mSharedItem.mRectOrigin.height();
        final int deltaX = scrollX - mSharedItem.mLocationOrigin[0];
        final int deltaY = scrollY - mSharedItem.mLocationOrigin[1];

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
                params.width = (int) (width - deltaW * value);
                params.height = (int) (height - deltaH * value);
                mViewPager.setLayoutParams(params);
                scrollTo((int) (scrollX - deltaX * value), (int) (scrollY - deltaY * value));

                if (value == 1) {
                    onDismiss.onDismiss();
                }
            }
        });
        valueAnimator.start();
    }
}
