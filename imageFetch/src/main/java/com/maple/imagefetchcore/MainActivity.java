package com.maple.imagefetchcore;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.maple.imagefetchcore.imagemanager.AsyncImageManager;
import com.maple.imagefetchcore.manager.ImageFetchManager;
import com.maple.imagefetchcore.maple.imageselector.pojo.FileUnit;

public class MainActivity extends AppCompatActivity {

    private View mGridView;
    private View mAddView;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        AsyncImageManager.getInstance(getApplicationContext());
        ImageFetchManager.getInstance(getApplicationContext()).init();
        mAddView = findViewById(R.id.im_add_id);
        mGridView = findViewById(R.id.im_home_grid_id);
        mAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "跳到搜索页面", Toast.LENGTH_LONG).show();
            }
        });
        showView();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ImageFetchManager.getInstance(getApplicationContext()).fetchImage("https://www.douban.com/photos/album/1627063826/", null);
//
//            }
//        }).start();
    }

    private void showView() {
        FileUnit fileUnit = ImageFetchManager.getInstance(mContext).getFileUnits();
        if (fileUnit == null) {
            mGridView.setVisibility(View.GONE);
            mAddView.setVisibility(View.VISIBLE);
        } else {
            mGridView.setVisibility(View.VISIBLE);
            mAddView.setVisibility(View.GONE);
        }
    }
}
