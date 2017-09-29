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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

	private static final int INDENT_LEVEL = 2;

	private static Service service;
	private AuthStateDAL auth;

	private EditText sendToEmailView = null;
	private AlertDialog dialogShareDevice = null;
	private AlertDialog dialogShareStatus = null;
	private Button buttonShareDevice = null;
	private Button buttonShareStatus = null;
	private Button buttonCreateDevice = null;
	private Button buttonLogin = null;

	private TextView apiResponseTextView = null;
	private TextView loginInfoTextView = null;

	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d("App", "DeviceActivity.java => onCreate()");

		setContentView(R.layout.activity_device);

		service = new Service(DeviceActivity.this);
		auth = new AuthStateDAL(DeviceActivity.this);

		/**
		 *  UI elements
		 */
		buttonShareDevice = (Button) findViewById(R.id.btn_share_device);
		buttonShareStatus = (Button) findViewById(R.id.btn_share_status);
		buttonCreateDevice = (Button) findViewById(R.id.btn_create_device);
		buttonLogin = (Button) findViewById(R.id.btn_login);

		apiResponseTextView = (TextView) findViewById(R.id.textview_api_response);
		loginInfoTextView = (TextView) findViewById(R.id.textview_login_info);

		// create a simple device name for the sample
		final String deviceName = generateDeviceName();

		TextView txtViewCreateDeviceInfo = (TextView) findViewById(R.id.txt_create_device_info);
		txtViewCreateDeviceInfo.setText(String.format(txtViewCreateDeviceInfo.getText().toString(), deviceName));

		TextView txtViewShareDeviceInfo = (TextView) findViewById(R.id.txt_share_device_info);
		txtViewShareDeviceInfo.setText(String.format(txtViewShareDeviceInfo.getText().toString(), deviceName));

		TextView txtViewShareStatusInfo = (TextView) findViewById(R.id.txt_share_status_info);
		txtViewShareStatusInfo.setText(String.format(txtViewShareStatusInfo.getText().toString(), deviceName));

		buttonCreateDevice.setEnabled(false);
		service.getUserProfileAsync(new Service.APICallback() {

			@Override
			public void onSuccess(JSONObject result) {

				printToConsole(apiResponseTextView, "< Success getUserProfileAsync()", result);
				buttonLogin.setVisibility(View.GONE);
				buttonCreateDevice.setEnabled(true);

				try {

					// user profile is wrapped in "data" response

					JSONObject userProfileResponse = result.getJSONObject("data");
					user = new Gson().fromJson(userProfileResponse.toString(), User.class);
					displayAuthInfo();

				} catch (JSONException e) {
					e.printStackTrace();
					buttonLogin.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onError(VolleyError error) {

				String errorMessage = "";

				if (error.networkResponse != null && error.networkResponse.data != null) {

					errorMessage = new String(error.networkResponse.data);
				}

				printToConsole(apiResponseTextView,
								"Please Login.", "It does not appear you are logged in or your token has expired.");

				buttonLogin.setVisibility(View.VISIBLE);

			}
		});


		buttonCreateDevice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {

				JSONObject body = new JSONObject();

				try {

					body.put("uid", user.getId());
					body.put("dtid", Config.DEVICE_TYPE_ID);
					body.put("name", deviceName);
					body.put("manifestVersionPolicy", "LATEST");

				} catch (JSONException e) {
					e.printStackTrace();
				}

				Log.d("App", "Error:" + body.toString());

				service.createDeviceAsync(body, new Service.APICallback() {

					@Override
					public void onSuccess(JSONObject result) {

						//sample success response:
						//{"data":{"id":"4631d5b736994678ac29992f4565ef34","uid":"e03d4458bc8b462db048775dc17107f9","dtid":"dtce45703593274ba0b4feedb83bc152d8","name":"XYZ Throw - 754.0","manifestVersion":1,"manifestVersionPolicy":"LATEST","needProviderAuth":false,"cloudAuthorization":"NO_AUTHORIZATION","eid":null,"certificateSignature":null,"certificateInfo":null,"createdOn":1505782728000,"connected":true}}

						buttonCreateDevice.setEnabled(false);

						Log.d("App", "successfully created device:" + result.toString());

						apiResponseTextView.setText(formatResponse("Create Share Success with data:", result));

						try {
							JSONObject resultData = result.getJSONObject("data");
							String shareID = resultData.getString("id");

							//TODO:  do not use ARTIK class here, it might change.  create your own class
							Device device = new Gson().fromJson(resultData.toString(), Device.class);

							auth.writeDeviceState(device);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					//TODO:  make sure all of them say error.getMessage();
					@Override
					public void onError(VolleyError error) {

						Log.d("App", "error created device:" + error.getMessage());
						apiResponseTextView.setText(error.toString());
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
				finish();


			}
		});

		loginInfoTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				buttonLogin.setVisibility(View.VISIBLE);
			}
		});

	}

	private void displayAuthInfo() {


		AuthState authState = auth.readAuthState();

		String userEmail = "\nEmail: " + user.getEmail();
		String userToken = "\nUser Token: " + authState.getAccessToken().toString();
		String expiration = "\nExpires: " + new Date(authState.getAccessTokenExpirationTime()).toString();

		loginInfoTextView.setTextIsSelectable(true);
		loginInfoTextView.setTextSize(12);
		loginInfoTextView.setText("Your last login status:\n");

		loginInfoTextView.append(userEmail);
		loginInfoTextView.append(userToken);
		loginInfoTextView.append(expiration);

	}

	public AlertDialog getShareDeviceDialog() {

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

								service.shareDeviceAsync(auth.readDeviceState().getId(), body, new Service.APICallback() {

									@Override
									public void onSuccess(JSONObject result) {

										Log.d("App", "Got success from shareDeviceAsync():" + result.toString());
										printConsole(result.toString());
									}

									@Override
									public void onError(VolleyError error) {

										Log.d("App", "Got error from shareDeviceAsync():" + (new String(error.networkResponse.data)));
										printConsole(new String(error.networkResponse.data));

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

	public AlertDialog getShareStatusDialog() {

		if (dialogShareStatus != null) return dialogShareStatus;

		AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);

		builder.setMessage("Device Share Status")
						.setTitle("Device Share");

		builder.setView(sendToEmailView)

						.setPositiveButton("Send Share", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								// list all shares for device
								service.listSharesForDeviceAsync(auth.readDeviceState().getId(), new Service.APICallback() {

									@Override
									public void onSuccess(JSONObject result) {

										Log.d("app", "Success — Shares for device: " + result.toString());

									}

									@Override
									public void onError(VolleyError error) {

										Log.d("app", "Error — Shares for device: " + error.toString());
									}
								});


							}
						})

						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								// cancel
							}
						});

		dialogShareStatus = builder.create();
		return dialogShareStatus;
	}

	public String formatResponse(String title, JSONObject json) {

		StringBuilder builder = new StringBuilder();

		builder.append(title + "\n");

		try {

			builder.append(json.toString(INDENT_LEVEL));

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return builder.toString();
	}

	//refactor
	@Deprecated
	private void printConsole(final String text) {

		DeviceActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				apiResponseTextView.setText(text);

			}
		});
	}

	private void printToConsole(TextView view, String title, String description) {

		view.setTextIsSelectable(true);
		view.setTextSize(12);

		String content = view.getText().toString();

		view.setText(title + "\n");
		view.append(description + "\n");
		view.append(content + "\n\n");
	}

	private void printToConsole(TextView view, String title, JSONObject description) {

		Log.d("TAG", "called consoleAppend()");

		try {
			printToConsole(view, title, description.toString(INDENT_LEVEL));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private String generateDeviceName() {

		return "Sharing Demo A-" + (int) Math.floor(Math.random() * 1000);

	}

}
