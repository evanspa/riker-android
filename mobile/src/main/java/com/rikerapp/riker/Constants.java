package com.rikerapp.riker;

import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.WeightUnit;

public final class Constants {

    private Constants() {}

    // common result codes
    public static final int RESULTCODE_CHART_CONFIG_SAVED = 82;
    public static final int RESULTCODE_CHART_CONFIG_CLEARED = 83;
    public static final int RESULTCODE_DASHBOARD_CHARTS_NEED_RELOAD = 84;

    public static final int DIVIDE_SCALE = 6;

    public static final int SECONDS_IN_HOUR = 3600;

    public static final int CHART_INITIAL_DATA_LOADER_ID = 0;

    public static final int ENTITIES_FETCH_LIMIT = 100;

    public static final String FRAGMENT_TAG_PROGRESS_DIALOG = "FRAGMENT_TAG_PROGRESS_DIALOG";

    public static final int MAX_IMPORT_ERRORS_RECORDED = 4;

    public static final String RIKER_URI_BARENAV_FAQ = "faqBareNav";
    public static final String RIKER_URI_BARENAV_TERMS_OF_SERVICE = "tosBareNav";
    public static final String RIKER_URI_BARENAV_PRIVACY_POLICY = "privacyBareNav";
    public static final String RIKER_URI_BARENAV_SECURITY_POLICY = "securityBareNav";

    public static final String RIKER_IOS_APP_STORE_URL = "https://itunes.apple.com/us/app/riker/id/1196920730?mt=8";

    public static final String RIKER_PREFERENCES = "RikerPreferences";
    //public static final String RIKER_PREFERENCES_NOT_BACKED_UP = "RikerPreferencesNotBackedUp";

    public static final int DEFAULT_WEIGHT_UOM_ID = WeightUnit.LBS.id;
    public static final int DEFAULT_SIZE_UOM_ID = SizeUnit.INCHES.id;
    public static final int DEFAULT_WEIGHT_INC_DEC_AMOUNT = 5;

    public static final String DATE_TIME_FORMAT = "MM/dd/yyyy h:mm:ss a"; // fyi, "h" is for 12-hr mode, "H" or "HH" is for 24-hr mode
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String DATE_FORMAT_HYPHENS = "MM-dd-yyyy";
    public static final String TIME_FORMAT = "h:mm:ss a"; // fyi, "h" is for 12-hr mode, "H" or "HH" is for 24-hr mode

    public static final int RATE_DAYS_SINCE_INSTALL = 20;
    public static final int RATE_LAUNCHES_SINCE_INSTALL = 10;
    public static final int RATE_DAYS_REMIND = 10;
    public static final int RATE_LAUNCHES_REMIND = 5;
}
