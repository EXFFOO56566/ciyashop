package com.example.ciyashop.utils;

import com.ciyashop.library.apicall.URLS;

public class APIS {

    //TODO:Copy and Paste URL and Key Below from Admin Panel.

    public final String APP_URL = "";
    public final String WOO_MAIN_URL = APP_URL + "wp-json/wc/v2/";
    public final String MAIN_URL = APP_URL + "wp-json/pgs-woo-api/v1/";

    public static final String CONSUMERKEY = "";
    public static final String CONSUMERSECRET = "";
    public static final String OAUTH_TOKEN = "";
    public static final String OAUTH_TOKEN_SECRET = "";

    public static final String WOOCONSUMERKEY = "";
    public static final String WOOCONSUMERSECRET = "";
    public static final String version = "4.3.0";
    public static final String purchasekey = "";




    public APIS() {
        URLS.APP_URL = APP_URL;
        URLS.NATIVE_API = APP_URL + "wp-json/wc/v3/";
        URLS.WOO_MAIN_URL = WOO_MAIN_URL;
        URLS.MAIN_URL = MAIN_URL;
        URLS.version = version;
        URLS.CONSUMERKEY = CONSUMERKEY;
        URLS.CONSUMERSECRET = CONSUMERSECRET;
        URLS.OAUTH_TOKEN = OAUTH_TOKEN;
        URLS.OAUTH_TOKEN_SECRET = OAUTH_TOKEN_SECRET;
        URLS.WOOCONSUMERKEY = WOOCONSUMERKEY;
        URLS.WOOCONSUMERSECRET = WOOCONSUMERSECRET;
        URLS.PURCHASE_KEY = purchasekey;
    }
}