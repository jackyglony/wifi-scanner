package com.shixunaoyou.wifiscanner.util;

public class Constants {
    public final static String ACCOUNT_SETTINGS_KEY = "account_settings";
    public final static String CHECK_UPDATE_KEY = "check_update";
    public final static String ABOUT_KEY = "about_application";
    public final static String LOGIN_STATUS_KEY = "login_status";

    public final static String USER_NAME_KEY = "user_name";
    public final static String PASSWORD_KEY = "password";

    public final static String AUTO_LOGIN_KEY = "enable_auto_login";
    public final static String ENABLE_NOTIFICATION_KEY = "enable_notify";
    public final static String ENABLE_SHOW_CHANNEL = "show_channel";
    public final static String ENABLE_SHOW_RSSI = "show_rssi";
    public final static String ENABLE_SERVICE_NOTIFICATION = "enable_notification";

    public final static String DATA_TRAFFIC = "data_traffic";

    public final static String ACCESS_POINT_KEY = "access_point";
    public final static int INVALID_NETWORK_ID = -1;

    public final static String UPDATE_APK_URL = "update_url";

    public final static String SSID_KEY = "ssid";
    public final static int CANNOT_CONNECT = -1;
    public final static int HAVE_LOGIN = 0;
    public final static int HAVE_LOGOUT = 1;
    public final static int NOT_FIND_SERVER = 2;
    public final static int NOT_NEED_LOGIN = 3;
    public final static int NO_WIFI = 4;
    public final static int LOGIN_FALLURE = 5;
    public final static int NO_CONNECTION = 6;
    public final static int NO_USER_NAME = 7;
    public final static int STATUS_UNKOWN = 8;
    public final static int CONNECTED = 9;

    public static final String TEST_URL = "www.baidu.com";
    public static final String DEFAULT_WIFI_SERVER = "10.1.0.1:3660";
    public static final String TEST_KEYWORD = "www.baidu.com";
    public static final String TEST_SHORT_BAIDU = "baidu.com";
    public static final String IS_SERVICE_UPDATE = "is_service_update";

    public static final String LAST_LOGIN_TIME = "last_login_time";

    public static final String GATE_CONFIGURE_KEY = "gateway_configure";

    public static final String WIFI_SERVER_KEY = "591wifi.com";
    public static final String SHARE_INFO_KEY = "share_information";

    public static final String LOGIN_ERROR_MESSAGE = "LOGIN_ERROR";
    public static final String WRONG_UPDATE_URL = "wrong_url";
    public static final int DEFAULT_MODE = 0;
    public static final String FILTER_MODE_KEY = "wifi_filter_mode_int";
    public static final String MAIL_ADDRESS = "wifi_user_mail";

    /**
     * @link res/values/arrays/wifi_filter_mode_values
     */
    public static final int FILTER_MODE_VISIBLE = 0;
    public static final int FILTER_MODE_ALL = 1;
    public static final int FILTER_MODE_OPEN = 2;
    public static final int FILTER_MODE_AUTHORIZE = 3;

    public static final int NOT_IN_RANGE = -1;

    public static final int UNKNOWN_STATUS = -1;
    public static final int PRE_CONDITION_OK = 0;
    public static final int NO_ENOUGH_SPACE = 1;
    public static final int NO_AVAIL_DISK = 2;
    public static final int DOWNLOAD_ERROR = 3;
    public static final int DOWNLOAD_COMPLETE = 4;

    public static final String TOKEN_REQUEST_ERROR = "{\"errno\":\"-1\", \"errMsg\":\"由于网络原因请求失败，请稍后重试！\" }";
    public static final String REGISTER_REQUEST_ERROR = "{\"errno\":\"-1\", \"errMsg\":\"由于网络原因请求失败，请稍后重试！\" }";
    public static final String SUCCESSFUL_CODE = "0";

    public static final int LOGIN_ACTION = 1;
    public static final int LOGOUT_ACTION = 2;

    public static final String CMCC = "中国移动免费热点";
    public static final String CHINA_UNICOM = "中国联通免费热点";
    public static final String CHINANET = "中国电信免费热点";
    public static final String SAVE_PATH = "/android/Downloads/WifiScanner/";
    public static final String APP_LIST = "app_list";

    public static final int BOOT_COMPLETE = 1;
    public static final String REBOOT_START = "reboot";
}
