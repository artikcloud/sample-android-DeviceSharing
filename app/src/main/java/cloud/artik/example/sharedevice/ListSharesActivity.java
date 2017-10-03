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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class ListSharesActivity extends AppCompatActivity {

	//used to retrieve logged in user token
	AuthStateDAL auth;
	//wrapper for rest api
	Service service;


	//list view and adapter for listing the device share status
	ListView listView;
	ArrayList<String> listItems;
	ArrayAdapter<String> adapter;

	//show a delete share dialog
	AlertDialog deleteShareDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_shares);

		auth = new AuthStateDAL(ListSharesActivity.this);
		service = new Service(ListSharesActivity.this);

		initListView();
		initListViewData();

	}

	private void initListView() {

		listView = (ListView) findViewById(R.id.shares_list);
		listItems = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

		listItems.add("Please wait ... ");
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				JSONObject shareStatusInfo = null;

				String shareId = null;
				String deviceId = null;

				try {

					//listview contains response from earlier with share ID needed for deleting.
					shareStatusInfo = new JSONObject((String) listView.getItemAtPosition(position));
					shareId = shareStatusInfo.getString("id");
					deviceId = service.readDeviceState().getId();

				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				AlertDialog dialog = getDialogDeleteShare(deviceId, shareId);
				dialog.show();

			}

		});

	}

	private void initListViewData() {

		service.listDeviceSharesAsync(service.readDeviceState().getId(), new Service.APICallback() {

			@Override
			public void onSuccess(JSONObject result) {

				Log.d("App", "success list shares: " + result);

				//sample response
				//{"data":{"shares":[{"id":"484d3d54506748928a74c7ba7bd2cce6","email":"email...@gmail.com","status":"PENDING","sharedOn":1507003192000}]},"total":1,"offset":0,"count":1}


				listItems.clear();

				try {

					JSONObject shares = result.getJSONObject("data");
					JSONArray sharesList = shares.getJSONArray("shares");

					if (sharesList.length() == 0) {

						listItems.add("\nIt does not appear your device is shared with anyone.  Go back and \n\n" +
										"1) `Create Device` (if not already), then \n" +
										"2) `Share Device` to send a share email invitation");

						adapter.notifyDataSetChanged();
					}

					for (int i = 0; i < sharesList.length(); i++) {

						JSONObject shareInfo = sharesList.getJSONObject(i);
						listItems.add(shareInfo.toString(2));

					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

				adapter.notifyDataSetChanged();

			}

			@Override
			public void onError(VolleyError error) {

				Log.d("App", "Error listing shares: " + error);

				listItems.clear();
				Toast toast = Toast.makeText(
								ListSharesActivity.this, "Error: " + new String(error.networkResponse.data), Toast.LENGTH_LONG);
				toast.show();
				adapter.notifyDataSetChanged();

			}

		});
	}

	private AlertDialog getDialogDeleteShare(final String deviceId, final String shareId) {

		if (deleteShareDialog != null) return deleteShareDialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(ListSharesActivity.this);

		builder
						.setTitle("Delete Share")
						.setMessage("Are you sure you want to stop sharing this device?");


		builder.setPositiveButton("Delete Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				service.deleteDeviceShareAsync(deviceId, shareId, new Service.APICallback() {
					@Override
					public void onSuccess(JSONObject result) {

						// sample response
						// {"data":{"id":"6827a4434cda4a7ea5157c1b58ff95f4"}

						Log.d("App", "successfully deleted with response: " + result);

					}

					@Override
					public void onError(VolleyError error) {

						Log.d("App", "error while deleting with response: " + error);

					}
				});

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

}