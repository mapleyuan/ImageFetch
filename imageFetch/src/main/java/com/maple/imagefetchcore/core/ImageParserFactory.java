package com.maple.imagefetchcore.core;

import com.maple.imagefetchcore.core.inter.IImageParser;

/**
 * Created by yuanweinan on 16/4/29.
 */
public class ImageParserFactory {

    public static IImageParser create(String url) {
        IImageParser imageParser;
        if (url.contains("douban")) {
            imageParser = new DoubanImageParser();
        } else {
            imageParser = new DefaultImageParser();
        }
        return imageParser;
    }
}
