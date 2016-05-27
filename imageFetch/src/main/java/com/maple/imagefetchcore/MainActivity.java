package com.maple.imagefetchcore;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.maple.imagefetchcore.imagemanager.AsyncImageManager;
import com.maple.imagefetchcore.manager.ImageFetchManager;
import com.maple.imagefetchcore.maple.easyadapter.BaseViewHolderHelper;
import com.maple.imagefetchcore.maple.easyadapter.EasyAdapter;
import com.maple.imagefetchcore.maple.imageselector.pojo.FileUnit;
import com.maple.imagefetchcore.maple.imageselector.pojo.FolderUnit;
import com.maple.imagefetchcore.ui.IFSelectDialog;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements IFSelectDialog.IFetchListener {

    private GridView mGridView;
    private View mAddView;
    private Context mContext;
    private BaseAdapter mGridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IFSelectDialog(MainActivity.this).show(MainActivity.this);

            }
        });

        mContext = getApplicationContext();
        AsyncImageManager.getInstance(getApplicationContext());
        ImageFetchManager.getInstance(getApplicationContext()).init();
        mAddView = findViewById(R.id.im_add_id);
        mGridView = (GridView) findViewById(R.id.im_home_grid_id);

        mAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IFSelectDialog(MainActivity.this).show(MainActivity.this);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.if_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_websearch) {
            SearchActivity.start(mContext);
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onFetchSuccess() {
        showView();
        FileUnit fileUnit = ImageFetchManager.getInstance(mContext).getFileUnits();
        mGridAdapter = new EasyAdapter<FolderUnit, BaseViewHolderHelper>(mContext, R.layout.cp_imagefolder_gridview_item, fileUnit.mFolderList) {
            @Override
            public void convert(BaseViewHolderHelper viewHolderHelper, int position, FolderUnit data) {

            }
        };
        mGridView.setAdapter(mGridAdapter);
    }

    @Override
    public void onFetchFail() {
        //do nothing
    }
}
