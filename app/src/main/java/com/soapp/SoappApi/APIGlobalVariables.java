package com.soapp.SoappApi;

import okhttp3.MediaType;

/* Created by chang on 18/07/2017. */

public class APIGlobalVariables {

    public static final MediaType JPEG = MediaType.parse("image/jpg");

    // Registration
    public static String API_REGISTER = "https://api2.soappchat.com/api/v1/register";
    // Authenticate
    public static String API_AUTH = "https://api2.soappchat.com/api/v1/authenticate";
    // RegistrationApi
    public static String API_SMS = "https://api2.soappchat.com/api/v1/auth/sms";

    // for downloading media (incoming) via chat (updated to v1.3 on 5.01.2018)
    public static String API_RESOURCES_1_3 = "https://api2.soappchat.com/api/v1.3/resource/";
    //TODO: preferences Keys
    // Registrations
    public static String STRPREF_COUNTRY_CODE = "COUNTRY_CODE";
    public static String STRPREF_PHONE_NUMBER = "PHONE_NUMBER";
    public static String STRPREF_IS_REGISTERED = "IS_REGISTERED";
    public static String STRPREF_USER_TOKEN = "USER_TOKEN";
    public static String STRPREF_USER_ID = "USER_ID";
    public static String STRPREF_DEVICE_ID = "DEVICE_ID";
    public static String STRPREF_XMPP_PASSWORD = "XMPP_PASSWORD";
    public static String STRPREF_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static String STRPREF_TOKEN_TYPE = "TOKEN_TYPE";
    public static String STRPREF_EXPIRES_IN = "EXPIRES_IN";
    public static String STRPREF_REFRESH_TOKEN = "REFRESH_TOKEN";


}
