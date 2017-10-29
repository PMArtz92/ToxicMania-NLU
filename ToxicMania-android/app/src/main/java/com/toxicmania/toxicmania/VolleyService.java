package com.toxicmania.toxicmania;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Vihanga Liyanage on 10/20/2017.
 */

public class VolleyService {

    private RequestQueue queue;

    public VolleyService(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void volleyGet(final String requestType, String url, final VolleyCallback callback) {
        try {
            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (callback != null)
                        callback.notifySuccess(requestType, response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (callback != null)
                        callback.notifyError(requestType, error);
                }
            });

            queue.add(jsonObj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void volleyPost(final String requestType, String url, JSONObject sendObj, final VolleyCallback callback) {
        try {
            JsonObjectRequest jsonObj = new JsonObjectRequest(url, sendObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (callback != null)
                        callback.notifySuccess(requestType, response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (callback != null)
                        callback.notifyError(requestType, error);
                }
            });

            queue.add(jsonObj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
