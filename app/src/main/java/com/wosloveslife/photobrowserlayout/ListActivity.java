package com.wosloveslife.photobrowserlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangh on 2017/5/14.
 */

public class ListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ViewGroup mContainer;

    private PopupWindow mPopupWindow;
    private PhotoBrowser2 mPhotoBrowser;

    String[] ADDRESS = {
            "http://wx2.sinaimg.cn/mw690/707e96d5gy1ffo4d9sf3jj20qp0gpn2q.jpg",
            "http://wx2.sinaimg.cn/mw690/707e96d5gy1ffo4da51kyj20go09cgn9.jpg",
            "http://wx2.sinaimg.cn/mw690/005Bd8p4gy1ffmm2o5v13j30sg0lcn0b.jpg",
            "http://wx1.sinaimg.cn/mw690/697b3ffbly1ffo5ok606vj20dw0hu77h.jpg",
            "http://wx4.sinaimg.cn/mw690/697b3ffbly1ffo5ojxwelj20im0ahgof.jpg",
            "http://wx4.sinaimg.cn/mw690/69267083gy1ffltpxv6rkj22b42vtqva.jpg",
            "http://wx3.sinaimg.cn/mw690/69267083gy1ffluiltyqyj224x2o3qv7.jpg"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);

        List<String> address = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            address.add(ADDRESS[i % ADDRESS.length]);
        }
        mAdapter = new Adapter();
        mAdapter.mAddress = address;
        mRecyclerView.setAdapter(mAdapter);

        mContainer = (ViewGroup) ((ViewGroup) ((ViewGroup) getWindow().getDecorView()).getChildAt(0)).getChildAt(1);
    }

    class Adapter extends RecyclerView.Adapter<Holder> {
        List<String> mAddress;

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            SimpleDraweeView view = (SimpleDraweeView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_iamge, parent, false);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(parent.getMeasuredWidth() / 3, (int) (parent.getMeasuredWidth() / 2.5f));
            view.setLayoutParams(layoutParams);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            holder.mImageView.setImageURI(mAddress.get(position));
            holder.mImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int[] screenLocation = new int[2];
                    holder.mImageView.getLocationInWindow(screenLocation);

                    mPhotoBrowser = new PhotoBrowser2(ListActivity.this);
                    mPhotoBrowser.setData(mAddress, holder.getAdapterPosition());

                    Bitmap bitmap = PhotoBrowser2.getCacheBitmap(Uri.parse(mAddress.get(holder.getAdapterPosition())));
                    mPhotoBrowser.mSharedElement = new PhotoBrowser2.SharedElement(
                            screenLocation[0], screenLocation[1],
                            holder.mImageView.getWidth(), holder.mImageView.getHeight(),
                            PhotoBrowser2.fbType2ImgType(holder.mImageView.getHierarchy().getActualImageScaleType()),
                            bitmap);

                    mPopupWindow = new PopupWindow(mPhotoBrowser, mContainer.getWidth(), mContainer.getHeight());
                    int[] out = new int[2];
                    mContainer.getLocationInWindow(out);
                    mPopupWindow.showAtLocation(mContainer, Gravity.NO_GRAVITY, 0, out[1]);
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
        SimpleDraweeView mImageView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (SimpleDraweeView) itemView;
        }
    }
}
