package com.maple.imagefetchcore.core;


import com.maple.imagefetchcore.core.bean.DoubanDivUnit;
import com.maple.imagefetchcore.core.inter.IImageParser;
import com.maple.imagefetchcore.utils.LogUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 豆瓣相册获取
 * Created by yuanweinan on 16/4/29.
 */
public class DoubanImageParser implements IImageParser {
    @Override
    public void parse(String url, IParseResult parseResult) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements divs = doc.select("div.photo_wrap");
                if (divs == null || divs.size() == 0) {
                    if (parseResult != null) {
                        parseResult.onFail();
                    }
                }
                List<DoubanDivUnit> units = new ArrayList<>();
                for (Element div : divs) {
                    DoubanDivUnit unit = new DoubanDivUnit();
                    Element href = div.select("a[href]").get(0);
                    unit.mHref = href.attr("href");
                    unit.mThumbnail = href.select("img").get(0).attr("src");
                    LogUtil.i("Douban---href:" + unit.mHref + "  thumbnail:" + unit.mThumbnail);
                    units.add(unit);
                }
                if (parseResult != null) {
                    parseResult.onFinish(units);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (parseResult != null) {
                    parseResult.onFail();
                }
            }
    }


}
