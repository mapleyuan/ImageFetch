package com.maple.imagefetchcore.maple.imageselector.pojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuanweinan on 16-5-26.
 */
public class FileUnit {
    public List<FolderUnit> mFolderList = new ArrayList<>();

    public static FileUnit transferTo(File[] childs) {
        if (childs == null) {
            return null;
        }
        FileUnit fileUnit = new FileUnit();
        int size = childs.length;
        for (int i = 0; i < size; i++) {
            fileUnit.mFolderList.add(new FolderUnit(childs[i]));
        }
        return fileUnit;
    }
}
