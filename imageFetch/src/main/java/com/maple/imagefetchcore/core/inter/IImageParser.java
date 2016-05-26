package com.maple.imagefetchcore.core.inter;

import java.util.List;

/**
 *
 * Created by yuanweinan on 16/4/29.
 */
public interface IImageParser {
    /**
     * 解析结果
     * */
     interface IParseResult<T> {
        void onFinish(List<T> list);
        void onFail();
    }
    void parse(String url, IParseResult parseResult);
}
