package com.rikerapp.riker.sql.columns;

import com.rikerapp.riker.sql.ColumnType;

public enum UserColumn {

    NAME("getName", ColumnType.TEXT),
    EMAIL("email", ColumnType.TEXT),
    PASSWORD_HASH("password_hash", ColumnType.TEXT),
    VERIFIED_AT("verified_at", ColumnType.INTEGER),
    LAST_CHARGE_ID("last_charge_id", ColumnType.TEXT),
    TRIAL_ALMOST_EXPIRED_NOTICE_SENT_AT("trial_almost_expired_notice_sent_at", ColumnType.INTEGER),
    LATEST_STRIPE_TOKEN_ID("latest_stripe_token_id", ColumnType.TEXT),
    NEXT_INVOICE_AT("next_invoice_at", ColumnType.INTEGER),
    NEXT_INVOICE_AMOUNT("next_invoice_amount", ColumnType.INTEGER),
    LAST_INVOICE_AT("last_invoice_at", ColumnType.INTEGER),
    LAST_INVOICE_AMOUNT("last_invoice_amount", ColumnType.INTEGER),
    CURRENT_CARD_LAST4("current_card_last4", ColumnType.TEXT),
    CURRENT_CARD_BRAND("current_card_brand", ColumnType.TEXT),
    CURRENT_CARD_EXP_MONTH("current_card_exp_month", ColumnType.INTEGER),
    CURRENT_CARD_EXP_YEAR("current_card_exp_year", ColumnType.INTEGER),
    TRIAL_ENDS_AT("trial_ends_at", ColumnType.INTEGER),
    STRIPE_CUSTOMER_ID("stripe_customer_id", ColumnType.TEXT),
    PAID_ENROLLMENT_ESTABLISHED_AT("paid_enrollment_established_at", ColumnType.INTEGER),
    NEW_MOVEMENTS_ADDED_AT("new_movements_added_at", ColumnType.INTEGER),
    INFORMED_OF_MAINTENANCE_AT("informed_of_maintenance_at", ColumnType.INTEGER),
    MAINTENANCE_STARTS_AT("maintenance_starts_at", ColumnType.INTEGER),
    MAINTENANCE_DURATION("maintenance_duration", ColumnType.INTEGER),
    IS_PAYMENT_PAST_DUE("is_payment_past_due", ColumnType.INTEGER),
    PAID_ENROLLMENT_CANCELLED_AT("paid_enrollment_cancelled_at", ColumnType.INTEGER),
    FINAL_FAILED_PAYMENT_ATTEMPT_OCCURRED_AT("final_failed_payment_attempt_occurred_at", ColumnType.INTEGER),
    VALIDATE_APP_STORE_RECEIPT_AT("validate_app_store_receipt_at", ColumnType.INTEGER),
    MAX_ALLOWED_SET_IMPORT("max_allowed_set_import", ColumnType.INTEGER),
    MAX_ALLOWED_BML_IMPORT("max_allowed_bml_import", ColumnType.INTEGER)
    ;

    public final String name;
    public final ColumnType type;

    UserColumn(final String name, final ColumnType type) {
        this.name = name;
        this.type = type;
    }
}
