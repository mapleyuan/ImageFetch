package com.maple.imagefetchcore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.maple.imagefetchcore.imagemanager.AsyncImageManager;
import com.maple.imagefetchcore.manager.ImageFetchManager;
import com.maple.imagefetchcore.maple.imageselector.ImagePickCustom;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncImageManager.getInstance(getApplicationContext());
        ImagePickCustom pickCustom = (ImagePickCustom) findViewById(R.id.image_show_grid);
        ImageFetchManager.getInstance(getApplicationContext()).init();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ImageFetchManager.getInstance(getApplicationContext()).fetchImage("https://www.douban.com/photos/album/1627063826/", null);
//
//            }
//        }).start();
    }
}
