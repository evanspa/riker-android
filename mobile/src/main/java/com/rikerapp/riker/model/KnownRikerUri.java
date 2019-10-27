package com.rikerapp.riker.model;

public enum KnownRikerUri {

    BML_FILE_IMPORT("bodyjournallogsfileimport"),
    LOGOUT_ALL_OTHER("logout-all-other"),
    BMLS("bodyjournallogs"),
    STRIPE_TOKENS("stripetokens"),
    LOGOUT("logout"),
    CHANGE_LOG("changelog"),
    USER_DATA_CHANGE_LOG("userdatachangelog"),
    REF_DATA_CHANGE_LOG("refdatachangelog"),
    SEND_PASSWORD_RESET_EMAIL("send-password-reset-email"),
    SEND_VERIFICATION_EMAIL("send-verification-email"),
    SORENESSES("sorenesses"),
    PLAN("plan"),
    SETS_FILE_IMPORT("setsfileimport"),
    SETS("sets")
    ;

    public final String uri;

    KnownRikerUri(final String uri) {
        this.uri = uri;
    }

    public final String absoluteUrl(final String userGlobalIdentifier) {
        return String.format("%s/%s", userGlobalIdentifier, uri);
    }
}
