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

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cloud.artik.example.R;
import cloud.artik.example.oauth.AuthStateDAL;

/**
 * Activity to see `shares` you have made for a device
 */

public class ListSharesActivity extends AppCompatActivity {

	Service service;
	AuthStateDAL auth;
	ListView listView;

	ArrayList<String> listItems;
	ArrayAdapter<String> adapter;

	AlertDialog deleteShareDialog = null;

	public static class AlertDialogBuilder {


		public static AlertDialog create(
						String dialogTitle,
						String dialogMessage,
						String confirmButton,
						String cancelButton,
						View view,
						DialogInterface.OnClickListener listener,
						Context ctx) {

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(dialogTitle);
			builder.setMessage(dialogMessage);

			builder.setPositiveButton(confirmButton, listener);

			return builder.create();
		}

	}

	public AlertDialog getShareDeviceDialog() {

		if (deleteShareDialog != null) return deleteShareDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(ListSharesActivity.this);

		builder
						.setTitle("Delete Share")
						.setMessage("Are you sure you want to stop sharing this device?");


		builder.setPositiveButton("Delete Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				//api call delete
			}
		})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// cancel
							}
						});

		deleteShareDialog = builder.create();
		return deleteShareDialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_shares);

		service = new Service(ListSharesActivity.this);
		auth = new AuthStateDAL(ListSharesActivity.this);
		listView = (ListView) findViewById(R.id.shares_list);
		listItems = new ArrayList<String>();

		listItems.add("Please wait ... ");

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		listView.setAdapter(adapter);

		adapter.notifyDataSetChanged();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}

		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Log.d("My App", "Button 'Share Device' clicked");

				//AlertDialog dialog = getShareDeviceDialog();

				View dialogView = null;

				AlertDialog dialog = AlertDialogBuilder.create(
								"Delete Share",
								"Are you sure you want to delete?",
								"Delete Share", "Cancel",
								dialogView,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {

										//TODO: implement delete
										Log.d("App", "Button was clicked");

									}

								}, ListSharesActivity.this);


				dialog.show();

			}

		});

		Log.d("app", "list shares for device: " + auth.readDeviceState().getId());

		service.listSharesForDeviceAsync(auth.readDeviceState().getId(), new Service.APICallback() {

			@Override
			public void onSuccess(JSONObject result) {

				Log.d("App", "Got listShares result: " + result);

				listItems.clear();

				try {

					// sample response

					//{"data":{"shares":[{"id":"f270afd233614200a48070ea634fa328","email":"<REDACTED>","status":"PENDING","sharedOn":1505516894000},
					//{"id":"a2203a15857e4cf29f1e3e142ed80f4a","email":"<REDACTED>","status":"PENDING","sharedOn":1505516822000}]},"total":2,"offset":0,"count":2}

					JSONObject shares = result.getJSONObject("data");
					JSONArray sharesList = shares.getJSONArray("shares");

					if (sharesList.length() == 0) {

						listItems.add("\nIt does not appear you have shared the device with anyone.  Go back and \n\n" +
										"1) `Create Device` (if not already), then \n" +
										"2) `Share Device` to send an email invitation");

						adapter.notifyDataSetChanged();
					}

					for (int i = 0; i < sharesList.length(); i++) {
						JSONObject shareInfo = sharesList.getJSONObject(i);

						Log.d("App", "Share Info:" + shareInfo);

						listItems.add(shareInfo.toString(2));

					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

				adapter.notifyDataSetChanged();

			}

			@Override
			public void onError(VolleyError error) {

				Log.d("App", "Got listShares result: " + error);

				listItems.clear();
				Toast toast = Toast.makeText(ListSharesActivity.this, "Error: " + new String(error.networkResponse.data), Toast.LENGTH_LONG);
				toast.show();
				adapter.notifyDataSetChanged();

			}

		});

	}
}