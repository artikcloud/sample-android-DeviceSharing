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

package cloud.artik.example.oauth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import net.openid.appauth.AuthState;

import org.json.JSONException;
import org.json.JSONObject;

import cloud.artik.model.Device;
import cloud.artik.model.User;

// Tracking authorization state and storing into the persistent storage
public class AuthStateDAL {

    private static final String AUTH_PREFERENCES_NAME = "AuthStatePreference";
    private static final String USER_PREFERENCES_NAME = "UserStatePreference";
    private static final String DEVICE_PREFERENCES_NAME = "DEVICE_STATE";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USER_STATE = "USER_STATE";
    private static final String DEVICE_STATE = "DEVICE_STATE";

    @NonNull
    private  Activity activity;

    public AuthStateDAL(@NonNull Activity activity) {
        this.activity = activity;
    }

    @NonNull
    public AuthState readAuthState() {
        SharedPreferences authPrefs = activity.getSharedPreferences(AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String stateStr = authPrefs.getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(stateStr)) {
            try {
                return AuthState.jsonDeserialize(stateStr);
            } catch(JSONException ignore) {
                Log.w("AUTH", ignore.getMessage(), ignore);
            }
        }
        return new AuthState();
    }

    public void writeAuthState(@NonNull AuthState state) {
        SharedPreferences authPrefs = activity.getSharedPreferences(AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE);
        authPrefs.edit()
                .putString(AUTH_STATE, state.jsonSerializeString())
                .apply();
    }


    public void writeUserState(@NonNull User state) {
        SharedPreferences authPrefs = activity.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE);
        authPrefs.edit()
                .putString(USER_STATE, new Gson().toJson(state))
                .apply();
    }

    public User readUserState() {
        SharedPreferences authPrefs = activity.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String stateStr = authPrefs.getString(USER_STATE, null);
        if (!TextUtils.isEmpty(stateStr)) {
            Log.d("MyApp", "Serializing this data to User class:" + stateStr);
            User user = new Gson().fromJson(stateStr, User.class);
            return user;
        }
        return new User();
    }

    public void writeDeviceState(@NonNull Device state) {
        SharedPreferences authPrefs = activity.getSharedPreferences(DEVICE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        authPrefs.edit()
                .putString(DEVICE_STATE, new Gson().toJson(state))
                .apply();
    }

    public Device readDeviceState() {
        SharedPreferences authPrefs = activity.getSharedPreferences(DEVICE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String stateStr = authPrefs.getString(DEVICE_STATE, null);
        if (!TextUtils.isEmpty(stateStr)) {
            Log.d("MyApp", "Serializing this data to Device class:" + stateStr);
            Device device = new Gson().fromJson(stateStr, Device.class);
            return device;
        }
        return new Device();
    }


    public void clearAuthState() {
        activity.getSharedPreferences(AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

}
