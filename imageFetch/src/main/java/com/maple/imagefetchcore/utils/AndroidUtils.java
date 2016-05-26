package com.maple.imagefetchcore.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by yuanweinan on 16-4-26.
 */
public class AndroidUtils {

    public static final String BAIDU = "https://www.baidu.com/s?wd=";

    public static void openOnDefaultBrowser(String url, Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            intent.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            try {

                Intent intent = new Intent();
                intent.setAction("Android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                context.startActivity(intent);
            } catch (android.content.ActivityNotFoundException ee) {
                Toast.makeText(context, "未找到该手机浏览器", Toast.LENGTH_LONG).show();
            }
        }
    }
}
