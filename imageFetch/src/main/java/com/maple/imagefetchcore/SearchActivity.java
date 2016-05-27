package com.maple.imagefetchcore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.maple.imagefetchcore.utils.AndroidUtils;

/**
 * @author yuanweinan
 */
public class SearchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.if_search_main);
        mSearchEdit = (EditText) findViewById(R.id.rss_source_search_id);
        findViewById(R.id.rss_source_search_Ok_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = mSearchEdit.getText().toString();
                if (TextUtils.isEmpty(word)) {
                    return;
                }
                //上传统计
//                MobclickAgent.onEvent(mContext, "keyword", word);

                AndroidUtils.openOnDefaultBrowser(AndroidUtils.BAIDU + word, getApplicationContext());
            }
        });
    }
    private EditText mSearchEdit;
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void start(Context context) {
        if (null == context) {
            return;
        }
        Intent intent = new Intent(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
}
