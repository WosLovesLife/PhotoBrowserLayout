package com.wosloveslife.photobrowserlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangh on 2017/5/14.
 */

public class ListActivity extends AppCompatActivity {
    int[] res = {R.drawable.img2, R.drawable.img3, R.drawable.img4, R.drawable.img5, R.drawable.img6,
            R.drawable.img7, R.drawable.img8, R.drawable.img9, R.drawable.img1};

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ViewGroup mContainer;

    private PopupWindow mPopupWindow;
    private PhotoBrowser2 mPhotoBrowser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);

        List<Integer> address = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            address.add(res[i % res.length]);
        }
        mAdapter = new Adapter();
        mAdapter.mAddress = address;
        mRecyclerView.setAdapter(mAdapter);

        mContainer = (ViewGroup) ((ViewGroup) ((ViewGroup) getWindow().getDecorView()).getChildAt(0)).getChildAt(1);
    }

    class Adapter extends RecyclerView.Adapter<Holder> {
        List<Integer> mAddress;

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView view = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_iamge, parent, false);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(parent.getMeasuredWidth() / 3, (int) (parent.getMeasuredWidth() / 2.5f));
            view.setLayoutParams(layoutParams);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, int position) {
            holder.mImageView.setImageResource(mAddress.get(position));
            holder.mImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int[] screenLocation = new int[2];
                    holder.mImageView.getLocationInWindow(screenLocation);
                    Rect rect = new Rect();
                    holder.mImageView.getLocalVisibleRect(rect);

                    mPhotoBrowser = new PhotoBrowser2(ListActivity.this);
                    mPhotoBrowser.setData(mAddress, holder.getAdapterPosition());
                    mPhotoBrowser.mSharedElement = new PhotoBrowser2.SharedElement(screenLocation[0], screenLocation[1],
                            holder.mImageView.getWidth(), holder.mImageView.getHeight(), holder.mImageView.getScaleType(),
                            ((BitmapDrawable) holder.mImageView.getDrawable()).getBitmap());
                    mPopupWindow = new PopupWindow(mPhotoBrowser, mContainer.getWidth(), mContainer.getHeight());
                    mPopupWindow.showAtLocation(mContainer, Gravity.CENTER, 0, 0);
                    mPhotoBrowser.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAddress != null ? mAddress.size() : 0;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPhotoBrowser.onBackPressed(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mPopupWindow.dismiss();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mPopupWindow.dismiss();
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }
    }
}
