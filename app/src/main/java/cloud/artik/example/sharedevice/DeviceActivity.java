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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import net.openid.appauth.AuthState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cloud.artik.example.R;
import cloud.artik.example.oauth.AuthStateDAL;
import cloud.artik.model.Device;
import cloud.artik.model.User;

public class DeviceActivity extends AppCompatActivity {

    private Service service;

    private AuthStateDAL auth;

    private AlertDialog dialogShareDevice = null;
    private Button buttonShareDevice = null;
    private Button buttonShareStatus = null;
    private Button buttonCreateDevice = null;
    private Button buttonLogin = null;

    private TextView apiResponseTextView = null;
    private TextView loginInfoTextView = null;

    private String deviceName = null;
    private User user;

    @Override
    protected void onResume() {

        super.onResume();

        getAndDisplayUserInfo();

    }

    private void buttonStateInit() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonCreateDevice.setEnabled(true);
                buttonShareDevice.setEnabled(false);
                buttonShareStatus.setEnabled(false);
            }
        });

    }

    private void buttonStateAfterCreateDevice() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonCreateDevice.setEnabled(false);
                buttonShareDevice.setEnabled(true);
                buttonShareStatus.setEnabled(true);

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device);

        //handles setting and retrieving our user token
        auth = new AuthStateDAL(DeviceActivity.this);

        //facade for our ARITK Cloud REST calls
        service = new Service(DeviceActivity.this);

        // UI elements
        initUIElements();

        buttonStateInit();

        //retrieve logged in user info if available
        getAndDisplayUserInfo();


    }

    private void getAndDisplayUserInfo() {

        // get and display user profile information
        service.getUserProfileAsync(new Service.APICallback() {

            @Override
            public void onSuccess(JSONObject result) {

                printResponse("getUserProfileAsync()", result + "\n\n", apiResponseTextView);

                buttonLogin.setVisibility(View.GONE);
                buttonCreateDevice.setEnabled(true);

                try {

                    // data is wrapped in a "data" response
                    JSONObject userProfileResponse = result.getJSONObject("data");
                    user = new Gson().fromJson(userProfileResponse.toString(), User.class);
                    displayAuthInfo();

                } catch (JSONException e) {
                    e.printStackTrace();
                    printResponse("Error extracting data for getUserProfileAsync()", e.getMessage(), apiResponseTextView);
                    buttonLogin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(VolleyError error) {

                printResponse("Please Login.", "Does not appear you are logged in or your token has expired.", apiResponseTextView);

                buttonLogin.setVisibility(View.VISIBLE);

            }
        });
    }

    private void initUIElements() {

        //ui elements
        buttonShareDevice = (Button) findViewById(R.id.btn_share_device);
        buttonShareStatus = (Button) findViewById(R.id.btn_share_status);
        buttonCreateDevice = (Button) findViewById(R.id.btn_create_device);
        buttonLogin = (Button) findViewById(R.id.btn_login);

        apiResponseTextView = (TextView) findViewById(R.id.textview_api_response);
        loginInfoTextView = (TextView) findViewById(R.id.textview_login_info);

        apiResponseTextView.setTextIsSelectable(true);
        apiResponseTextView.setTextSize(12);

        loginInfoTextView.setTextIsSelectable(true);
        loginInfoTextView.setTextSize(12);

        buttonCreateDevice.setEnabled(false);

        // init click handlers
        buttonCreateDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                JSONObject body = new JSONObject();

                try {

                    body.put("uid", user.getId());
                    body.put("dtid", Config.DEVICE_TYPE_ID);
                    body.put("name", deviceName);
                    body.put("manifestVersionPolicy", "LATEST");

                } catch (Exception e) {
                    e.printStackTrace();
                    return;

                }
                service.createDeviceAsync(body, new Service.APICallback() {

                    @Override
                    public void onSuccess(JSONObject result) {

                        //sample response
                        //{"connected":true,"createdOn":1507003156000,"dtid":"dtce45703593274ba0b4feedb83bc152d8","id":"c35f2a4eb7ba45718f6715fecd0b297f","manifestVersion":1,"manifestVersionPolicy":"LATEST","name":"Sharing Demo A-56","needProviderAuth":false,"properties":{},"providerCredentials":{},"uid":"<redacted>"}

                        buttonStateAfterCreateDevice();
                        printSuccessResponse("createDeviceAsync()", result);

                        try {

                            // here we save above data for retrieving device ID later to make other api calls.
                            // data is wrapped in a "data" response
                            JSONObject resultData = result.getJSONObject("data");
                            Device device = new Gson().fromJson(resultData.toString(), Device.class);
                            service.writeDeviceState(device);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(VolleyError error) {

                        printErrorResponse("createDeviceAsync()", error);

                    }

                });

            }
        });

        buttonShareDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog dialog = getShareDeviceDialog();
                dialog.show();

            }
        });

        buttonShareStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DeviceActivity.this, ListSharesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DeviceActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

        loginInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonLogin.setVisibility(View.VISIBLE);
            }
        });


        // device name for session
        deviceName = generateDeviceName();

        TextView txtViewCreateDeviceInfo = (TextView) findViewById(R.id.txt_create_device_info);
        txtViewCreateDeviceInfo.setText(String.format(txtViewCreateDeviceInfo.getText().toString(), deviceName));

        TextView txtViewShareDeviceInfo = (TextView) findViewById(R.id.txt_share_device_info);
        txtViewShareDeviceInfo.setText(String.format(txtViewShareDeviceInfo.getText().toString(), deviceName));

        TextView txtViewShareStatusInfo = (TextView) findViewById(R.id.txt_share_status_info);
        txtViewShareStatusInfo.setText(String.format(txtViewShareStatusInfo.getText().toString(), deviceName));


    }

    private void displayAuthInfo() {


        AuthState authState = auth.readAuthState();

        if( user.getEmail() != null
                && authState.getAccessToken() != null
                && authState.getAccessTokenExpirationTime() != null)

        {

            final String userEmail = "\nEmail: " + user.getEmail() + " (change)";
            final String userToken = "\nUser Token: " + authState.getAccessToken();
            final String expiration = "\nExpires: " + new Date(authState.getAccessTokenExpirationTime());


            loginInfoTextView.setTextIsSelectable(true);
            loginInfoTextView.setTextSize(12);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    loginInfoTextView.setText("Your last login status:\n");
                    loginInfoTextView.append(userEmail);
                    loginInfoTextView.append(userToken);
                    loginInfoTextView.append(expiration);
                }
            });

        }

    }

    private AlertDialog getShareDeviceDialog() {

        if (dialogShareDevice != null) return dialogShareDevice;

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);

        builder.setMessage("Enter email address")
                .setTitle("Share Device to");

        final EditText input = new EditText(DeviceActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        builder.setView(input)

                .setPositiveButton("Send Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject body = new JSONObject();

                        try {

                            body.put("email", input.getText().toString());
                            Log.d("Debug", "Sending email is:" + body.get("email"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        service.shareDeviceAsync(service.readDeviceState().getId(), body, new Service.APICallback() {

                            @Override
                            public void onSuccess(JSONObject result) {

                                printSuccessResponse("sharedDeviceAsync()", result);
                            }

                            @Override
                            public void onError(VolleyError error) {

                                printErrorResponse("sharedDeviceAsync()", error);

                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel
                    }
                });

        dialogShareDevice = builder.create();
        return dialogShareDevice;
    }


    private void printResponse(final String title, final String description, final TextView view) {

        final String text = String.format("%s\n%s\n", title, description);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(text);
            }
        });

    }


    private void printSuccessResponse(String title, JSONObject result) {

        Log.d("App", String.format("Success for [%s]:\n", title) + result.toString());
        printResponse(String.format("Success for [%s]:\n", title), result.toString(), apiResponseTextView);

    }

    private void printErrorResponse(String title, VolleyError error) {

        Log.d("App", String.format("There was an error for: [%s]\n", title) + error.getMessage());

        if (error.networkResponse != null && error.networkResponse.data != null) {
            Log.d("App", String.format("There was an error for: [%s]\n", title) + (new String(error.networkResponse.data)));
            printResponse(String.format("There was an error for: [%s]\n", title), (new String(error.networkResponse.data)), apiResponseTextView);
        } else {
            printResponse(String.format("There was an error for: [%s]\n", title), error.getMessage(), apiResponseTextView);
        }

    }

    private String generateDeviceName() {

        return "Sharing Demo A-" + (int) Math.floor(Math.random() * 1000);

    }

}
