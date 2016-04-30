package com.github.colinjeremie.newsbeautifier.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton to manage the requests
 * Each request is inserting into a queue
 * Basically it's a container of a {@link RequestQueue} accessible through a singleton
 *
 * Created by jerem_000 on 2/25/2016.
 */
public class MyRequestQueue {
    static private MyRequestQueue instance = null;
    private RequestQueue requestQueue;

    private MyRequestQueue(Context pContext) {
        this.requestQueue = Volley.newRequestQueue(pContext);
    }

    static public MyRequestQueue getInstance(Context pContext){
        if (instance == null){
            instance = new MyRequestQueue(pContext);
        }
        return instance;
    }

    public void addToRequestQueue(Request req){
        requestQueue.add(req);
    }

    public void start() {
        requestQueue.start();
    }
}
