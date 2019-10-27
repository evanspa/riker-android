package com.rikerapp.riker;

public enum AnalyticsEvent {

    SET_SAVED_LOCAL_WHILE_OFFLINE("e_r_set_saved_local_wh_offline"),
    SET_SAVED_LOCAL_WHILE_UNAUTH("e_r_set_saved_local_wh_unauth"),
    SET_SAVED_LOCAL_WHILE_BAD_ACCOUNT("e_r_set_saved_local_wh_bad_acct"),
    SET_SAVED_LOCAL_WHILE_ANONYMOUS("e_r_set_saved_local_wh_anon"),
    BML_SAVED_LOCAL_WHILE_OFFLINE("e_r_bml_saved_local_wh_offline"),
    BML_SAVED_LOCAL_WHILE_UNAUTH("e_r_bml_saved_local_wh_unauth"),
    BML_SAVED_LOCAL_WHILE_BAD_ACCOUNT("e_r_bml_saved_local_wh_bad_acct"),
    BML_SAVED_LOCAL_WHILE_ANONYMOUS("e_r_bml_saved_local_wh_anon"),
    SETS_EXPORTED("sets_exported"),
    BMLS_EXPORTED("bmls_exported"),
    ALL_DATA_DELETED("deleted_all_local_data"),
    ENABLE_GOOGLE_FIT("enable_google_fit"),
    CANCEL_GOOGLE_FIT_CONNECT_ATTEMPT("cancel_google_fit_connect_attempt"),
    RETRY_GOOGLE_FIT_CONNECT_ATTEMPT("retry_google_fit_connect_attempt"),
    RATED("app_rated")
    ;

    public final String eventName;

    AnalyticsEvent(final String eventName) {
        this.eventName = eventName;
    }
}
