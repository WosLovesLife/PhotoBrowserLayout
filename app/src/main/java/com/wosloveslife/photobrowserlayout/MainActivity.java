package com.wosloveslife.photobrowserlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int[] res = {R.drawable.img2, R.drawable.img3, R.drawable.img4, R.drawable.img5, R.drawable.img6,
            R.drawable.img7, R.drawable.img8, R.drawable.img9, R.drawable.img1};

    private PhotoBrowserLayout mPhotoBrowserLayout;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ViewGroup mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
//            view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(parent.getMeasuredWidth() / 3, (int) (parent.getMeasuredWidth() / 2.5f));
            view.setLayoutParams(layoutParams);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
//            holder.mImageView.setController(Fresco.newDraweeControllerBuilder().setImageRequest(ImageRequestBuilder.newBuilderWithResourceId(mAddress.get(position)).build()).build());
            holder.mImageView.setImageResource(mAddress.get(position));
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPhotoBrowserLayout = new PhotoBrowserLayout(MainActivity.this);
                    mContainer.addView(mPhotoBrowserLayout);
                    mPhotoBrowserLayout.setData(mAddress, new PhotoBrowserLayout.SharedItem(holder.mImageView), position);
                    mPhotoBrowserLayout.setOnRequestListener(new PhotoBrowserLayout.OnRequestListener() {
                        @Override
                        public View onPageChanged(int position) {
                            RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(position);
                            return viewHolder != null ? viewHolder.itemView : null;
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAddress != null ? mAddress.size() : 0;
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }
    }

    @Override
    public void onBackPressed() {
        if (mContainer.getChildAt(mContainer.getChildCount() - 1) == mPhotoBrowserLayout) {
            mPhotoBrowserLayout.dismiss(new PhotoBrowserLayout.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mContainer.removeView(mPhotoBrowserLayout);
                }
            });
        } else {
            super.onBackPressed();
        }
    }
}
