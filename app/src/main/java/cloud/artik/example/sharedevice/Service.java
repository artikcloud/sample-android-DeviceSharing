/*
 * Copyright (C) 2017 Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.artik.example.sharedevice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cloud.artik.example.oauth.AuthStateDAL;
import cloud.artik.model.Device;


class Service {


    private static final String API_URL = "https://api.artik.cloud/v1.1";

    //used to get / retrieve values from shared prefererences
    private static final String DEVICE_PREFERENCES_NAME = "DEVICE_STATE";
    private static final String DEVICE_STATE = "DEVICE_STATE";


    private Activity context = null;

    private final AuthStateDAL authState;


    interface APICallback {

        void onSuccess(JSONObject result);
        void onError(VolleyError error);
    }

    Service(Activity context) {
        this.context = context;
        authState = new AuthStateDAL(context);
    }

    private void makeAPICallAsync(String url, int requestMethod, JSONObject body, final APICallback callback) {

        Toast.makeText(context, String.format("Making API call: %s", url),
                Toast.LENGTH_SHORT).show();

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                callback.onSuccess(response);
            }

        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error.networkResponse == null) {

                    if (error.getClass().equals(TimeoutError.class)) {

                        //timeout error
                        Toast.makeText(context, "Request Timed Out. Check your network connection.",
                                Toast.LENGTH_SHORT).show();
                    }

                }

                if (error.networkResponse != null && error.networkResponse.data != null) {
                    Log.d("App Error", "Error description:" + (new String(error.networkResponse.data)));
                    callback.onError(error);
                }

            }
        };

        JsonObjectRequest jsonRequest =
                new JsonObjectRequest(requestMethod, url, body, successListener, errorListener) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " + authState.readAuthState().getAccessToken());
                        params.put("Content-type", "application/json; charset=utf-8");


                        return params;

                    }
                };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonRequest);
    }

    void getUserProfileAsync(final APICallback callback) {

        String path = "/users/self";
        JSONObject body = null;
        makeAPICallAsync(API_URL + path, Request.Method.GET, body, callback);

    }

    void createDeviceAsync(JSONObject body, APICallback callback) {

        String path = "/devices";
        makeAPICallAsync(API_URL + path, Request.Method.POST, body, callback);
    }

    void shareDeviceAsync(String deviceId, JSONObject body, APICallback callback) {

        String path = String.format("/devices/%s/shares", deviceId);
        makeAPICallAsync(API_URL + path, Request.Method.POST, body, callback);
    }


    void listDeviceSharesAsync(String deviceId, APICallback callback) {

        String path = String.format("/devices/%s/shares", deviceId);
        JSONObject body = null;
        makeAPICallAsync(API_URL + path, Request.Method.GET, body, callback);

    }

    //body requires "email": sentToEmailAddress per API spec
    void deleteDeviceShareAsync(String deviceId, String shareId, APICallback callback) {

        String path = String.format("/devices/%s/shares/%s", deviceId, shareId);
        JSONObject body = null;
        makeAPICallAsync(API_URL + path, Request.Method.DELETE, body, callback);
    }

    //cache for storing the device (ie: deviceId) created for this demo
    void writeDeviceState(@NonNull Device state) {
        SharedPreferences authPrefs = context.getSharedPreferences(DEVICE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        authPrefs.edit()
                .putString(DEVICE_STATE, new Gson().toJson(state))
                .apply();
    }

    //cache for retrieving the device (ie: deviceId / name) created for this demo
    Device readDeviceState() {
        SharedPreferences authPrefs = context.getSharedPreferences(DEVICE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String stateStr = authPrefs.getString(DEVICE_STATE, null);
        Device device = new Device();

        if (!TextUtils.isEmpty(stateStr)) {
            Log.d("MyApp", "Serializing this data to Device class:" + stateStr);
            device = new Gson().fromJson(stateStr, Device.class);
        }

        return device;
    }

}
