package com.github.colinjeremie.newsbeautifier;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * * NewsBeautifier
 * Created by jerem_000 on 2/19/2016.
 */
@Database(name = MyDatabase.NAME, version = MyDatabase.VERSION)
public class MyDatabase {
    public static final String NAME = "News_Database";
    public static final int VERSION = 2;
}
