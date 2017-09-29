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

import android.net.Uri;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import cloud.artik.example.sharedevice.Config;

public class AuthHelper {

    public static final String ARTIKCLOUD_AUTHORIZE_URI = "https://accounts.artik.cloud/signin";
    public static final String ARTIKCLOUD_TOKEN_URI = "https://accounts.artik.cloud/token";

    public static final String INTENT_ARTIKCLOUD_AUTHORIZATION_RESPONSE
             = "cloud.artik.example.oauth.ARTIKCLOUD_AUTHORIZATION_RESPONSE";
    public static final String USED_INTENT = "USED_INTENT";

    public static AuthorizationRequest createAuthorizationRequest() {

        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(ARTIKCLOUD_AUTHORIZE_URI),
                Uri.parse(ARTIKCLOUD_TOKEN_URI),
                null
        );

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                Config.CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(Config.REDIRECT_URI)
        );

        return builder.build();

    }


}
