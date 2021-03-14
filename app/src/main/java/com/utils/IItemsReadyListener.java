package com.utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mac on 8/19/16.
 */
public interface IItemsReadyListener {
    public void onItemsReady(ArrayList<JSONObject> data);
}
