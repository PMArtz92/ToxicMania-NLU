package com.toxicmania.toxicmania;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by Vihanga Liyanage on 10/20/2017.
 */

public interface VolleyCallback {
    void notifySuccess(String requestType, JSONObject response);
    void notifyError(String requestType, VolleyError error);
}