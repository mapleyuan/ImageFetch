package com.maple.imagefetchcore.core;


import com.maple.imagefetchcore.core.inter.IImageParser;
import com.maple.imagefetchcore.utils.LogUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by yuanweinan on 16/4/29.
 */
public class DefaultImageParser implements IImageParser {

    @Override
    public void parse(String url, IParseResult parseResult) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]"); // 具有 href 属性的链接
            Elements pngs = doc.select("img[src$=.png]");// 所有引用 png 图片的元素

            for (Element link : pngs) {
                String linkHref = link.attr("src");
                String linkText = link.text();
                LogUtil.i("href:" + linkHref + "  text:" + linkText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (parseResult != null) {
                parseResult.onFail();
            }
        }

    }
}
