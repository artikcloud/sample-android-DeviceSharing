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

public class Config {

	//TODO:  Replace clientID
	public static final String CLIENT_ID = "Your Application Client Id";

	// MUST be consistent with "AUTH REDIRECT URL" of your application.
	// Set up at developer.artik.cloud for your Application
	public static final String REDIRECT_URI = "cloud.artik.example.oauth://oauth2callback";

	// Device Types accessible by the Application
	// Sample uses: cloud.artik.sample.demofiresensor
	public static final String DEVICE_TYPE_ID = "dtce45703593274ba0b4feedb83bc152d8";

}
