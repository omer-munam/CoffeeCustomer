package com.utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mac on 30/09/2017.
 */

public class Constants {

    public static final String LOCATION_CHANGED                 = "location_changed_string";
    public static final String EXTRA_FRAGMENT_NAME              = "fragment";
    public static String DEF_CURRENCY                     = "SAR ";
    public static HashMap<String,JSONObject> itemMap      = new HashMap<>();
    public static HashMap<String,Integer> itemqty         = new HashMap<>();
    public static float totalPrice                              = 0;
    public static String formatted                              = "";
    public static String GOOGLE_API                             = "AIzaSyBuq0O9L3ocfogzUMSEQGHyj9JDPv92oWU";
    public static String SUBCAT_PRESENT                         = "_sub_cat_pre_se_nt_";
    public static String PRODUCT_PRESENT                        = "_product_cat_pre_se_nt_";
    public static String QTY_CHANGED                            = "_qt_cat_pre_se_nt_";
    public static String ITEM_DELETED                            = "_abcdqt_cat_pre_se_nt_";
    public static String ORDERS_RELOAD                            = "_ewdfsdfscdqt_cat_pre_se_nt_";
    public static String PRODUCT_RELOAD                            = "_dee_deee_ddd";

    public static int FROM_CAMERA 			= 1;
    public static int FROM_GALLERY			= 0;
    public static final int FROM_IMAGE			= 433;

    public static final int MIN_STORE_LIST                      = 100;
    public static final int FACEBOOK                            = 1;
    public static final int GOOGLE                              = 2;
    public static final int TWITTER                             = 3;
    public static final String ANDROID_DEVICE                   = "1";
    public static final String PLACED                           = "0";
    public static final String ACCEPTED                         = "1";
    public static final String READY                            = "2";
    public static final String DELIVERED                        = "3";
    public static final String CLOSED                           = "4";
    public static final String CANCELLED                        = "5";
    public static final String DEADLINEOVER                     = "1";

    public static String USER_PATTERN 		                    = "^[a-zA-Z0-9_-]{3,15}$";
    public static String PWD_PATTERN		                    = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\\\S+$).{6,}$";
}
