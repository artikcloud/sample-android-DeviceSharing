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

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cloud.artik.client.ApiClient;
import cloud.artik.example.oauth.AuthStateDAL;


public class Service {

	private static final String URL_ENDPOINT = "https://api.artik.cloud/v1.1";
	private Activity context = null;
	final static ApiClient apiClient = null;

	Service(Activity context) {
		this.context = context;
	}

	public interface APICallback {
		void onSuccess(JSONObject result);

		void onError(VolleyError error);
	}

	@Deprecated
	public void getUserProfile(final APICallback callback) {

		Log.d("App", "getting /users/self");

		String path = "/users/self";

		JSONObject body = null;

		makeAPICall(URL_ENDPOINT + path, Request.Method.GET, body, callback);

	}

	public void getUserProfileAsync(final APICallback callback) {

		Log.d("App", "Calling getUserProfileAsync()");

		String path = "/users/self";

		JSONObject body = null;

		makeAPICallAsync(URL_ENDPOINT + path, Request.Method.GET, body, callback);

	}


	//TODO: remove userID from parameter
	public void listUserDevicesAsync(String userId, final APICallback callback) {

		//TODO: query options
		String path = "/users/" + userId + "/devices?includeShareInfo=true";
		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "listUserDevicesAsync()");

		JSONObject body = null;

		makeAPICallAsync(URL_ENDPOINT + path, Request.Method.GET, body, callback);

	}

	@Deprecated
	public void createDevice(JSONObject body) {

		String path = "/devices";

		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "makeAPICall()");

		JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, endpoint, body,

						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								Log.d("App", "Got Response()" + response.toString());
								//mTxtDisplay.setText("Response: " + response.toString());
							}

						}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub

				if (error != null) {
					Log.d("App Error", error.toString());
				}

			}


		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {

				AuthStateDAL authState = new AuthStateDAL(context);
				Map<String, String> params = new HashMap<String, String>();
				params.put("Authorization", "Bearer " + authState.readAuthState().getAccessToken());


				return params;

			}
		};


		RequestQueue requestQueue = Volley.newRequestQueue(context);
		requestQueue.add(jsonRequest);

	}

	public void createDeviceAsync(JSONObject body, APICallback callback) {

		String path = "/devices";

		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "makeAPICall()");

		makeAPICallAsync(URL_ENDPOINT + path, Request.Method.POST, body, callback);
	}

	@Deprecated
	public void shareDevice(String deviceId, JSONObject body) {

		String path = "/devices/" + deviceId + "/shares";

		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "makeAPICall()");

		JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, endpoint, body,

						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								Log.d("App", "Got Response for shareDevice()" + response.toString());
								//mTxtDisplay.setText("Response: " + response.toString());
							}

						}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if (error == null || error.networkResponse == null) {
					return;
				}

				String body;
				//get status code here
				final String statusCode = String.valueOf(error.networkResponse.statusCode);
				//get response body and parse with appropriate encoding
				try {
					body = new String(error.networkResponse.data, "UTF-8");

					Log.d("Error Message", statusCode + ":" + body);
				} catch (UnsupportedEncodingException e) {
					// exception
				}

			}


		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {

				AuthStateDAL authState = new AuthStateDAL(context);
				Map<String, String> params = new HashMap<String, String>();
				params.put("Authorization", "Bearer " + authState.readAuthState().getAccessToken());


				return params;

			}
		};


		RequestQueue requestQueue = Volley.newRequestQueue(context);
		requestQueue.add(jsonRequest);

	}

	public void shareDeviceAsync(String deviceId, JSONObject body, APICallback callback) {

		String path = "/devices/" + deviceId + "/shares";

		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "makeAPICall()");

		makeAPICallAsync(URL_ENDPOINT + path, Request.Method.POST, body, callback);
	}


	public void listSharesForDeviceAsync(String deviceId, APICallback callback) {

		String path = "/devices/" + deviceId + "/shares";
		String endpoint = URL_ENDPOINT + path;

		Log.d("App", "list shares ()");

		JSONObject body = null;

		makeAPICallAsync(URL_ENDPOINT + path, Request.Method.GET, body, callback);
	}

	@Deprecated
	public void makeAPICall(String url, int requestMethod, JSONObject body, final APICallback callback) {

		Log.d("App", "makeAPICall()");

		JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, body,

						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								Log.d("App", "Got Response()" + response.toString());

								callback.onSuccess(response);
								//mTxtDisplay.setText("Response: " + response.toString());
							}

						}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub

				if (error != null) {
					Log.d("App Error", error.toString());
				}


			}


		}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {

				AuthStateDAL authState = new AuthStateDAL(context);
				Map<String, String> params = new HashMap<String, String>();
				params.put("Authorization", "Bearer " + authState.readAuthState().getAccessToken());


				return params;

			}
		};


		RequestQueue requestQueue = Volley.newRequestQueue(context);
		requestQueue.add(jsonRequest);
	}

	public void makeAPICallAsync(String url, int requestMethod, JSONObject body, final APICallback callback) {

		Log.d("App", "makeAPICallAsync()");

		AuthStateDAL authState = new AuthStateDAL(context);

		JsonObjectRequest jsonRequest = new JsonObjectRequest(requestMethod, url, body,

						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								Log.d("App", "Got Response()" + response.toString());

								callback.onSuccess(response);

							}

						},

						new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {

								if (error.networkResponse == null) {

									if (error.getClass().equals(TimeoutError.class)) {

										//timeout error
										Toast.makeText(context, "Request Timed Out. Check your network connection.",
														Toast.LENGTH_LONG).show();
									}

								}

								if (error.networkResponse != null && error.networkResponse.data != null) {
									Log.d("App Error", "Error description:" + (new String(error.networkResponse.data)));
									callback.onError(error);
								}


							}

						}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {

				AuthStateDAL authState = new AuthStateDAL(context);
				Map<String, String> params = new HashMap<String, String>();
				params.put("Authorization", "Bearer " + authState.readAuthState().getAccessToken());
				params.put("Content-type", "application/json; charset=utf-8");


				return params;

			}
		};


		RequestQueue requestQueue = Volley.newRequestQueue(context);
		requestQueue.add(jsonRequest);
	}


	//TODO rename method name, & implement as needed as singleton ...
	public ApiClient getApiClient() {

		// Access the RequestQueue through your singleton class.
		// MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

		return apiClient;

	}


}
