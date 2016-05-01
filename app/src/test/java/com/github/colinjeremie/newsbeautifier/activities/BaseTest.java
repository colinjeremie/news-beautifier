package com.github.colinjeremie.newsbeautifier.activities;

import com.github.colinjeremie.newsbeautifier.MyDatabase;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.After;
import org.robolectric.RuntimeEnvironment;

/**
 * All tests must inherit from this class to clean the SQLite db
 *
 * Created by jerem_000 on 5/1/2016.
 */
public class BaseTest {

    @After
    public void resetDb() throws Exception{
        FlowManager.getDatabase(MyDatabase.NAME).reset(RuntimeEnvironment.application);
        FlowManager.destroy();
    }
}
