package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class MainSupport extends MasterSupport implements Cloneable {

    // because these user-object datums (ha!) can come back on *any* web service response, so we
    // need them here in this parent class
    public Date verifiedAt;
    public Date newishMovementsAddedAt;
    public Date paidEnrollmentEstablishedAt;
    public boolean isPaymentPastDue;
    public Date paidEnrollmentCancelledAt;
    public Date finalFailedPaymentAttemptOccurredAt;
    public Date informedOfMaintenanceAt;
    public Date maintenanceStartsAt;
    public Integer maintenanceDuration;
    public Date validateAppStoreReceiptAt;

    public final void populateUserCommon(final JsonObject jsonObject) {
        verifiedAt                          = Utils.getDate(jsonObject,    User.key("verified-at"));
        newishMovementsAddedAt              = Utils.getDate(jsonObject,    User.key("new-movements-added-at"));
        paidEnrollmentEstablishedAt         = Utils.getDate(jsonObject,    User.key("paid-enrollment-established-at"));
        isPaymentPastDue                    = Utils.getBoolean(jsonObject, User.key("is-payment-past-due"));
        paidEnrollmentCancelledAt           = Utils.getDate(jsonObject,    User.key("paid-enrollment-cancelled-at"));
        finalFailedPaymentAttemptOccurredAt = Utils.getDate(jsonObject,    User.key("final-failed-payment-attempt-occurred-at"));
        informedOfMaintenanceAt             = Utils.getDate(jsonObject,    User.key("informed-of-maintenance-at"));
        maintenanceStartsAt                 = Utils.getDate(jsonObject,    User.key("maintenance-starts-at"));
        maintenanceDuration                 = Utils.getInteger(jsonObject, User.key("maintenance-duration"));
        validateAppStoreReceiptAt           = Utils.getDate(jsonObject,    User.key("validate-app-store-receipt-at"));
    }

    public String currentPassword; // if entity requires user's current password to be set on it

    public Date loggedAt;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 7).
                append(syncInProgress).
                append(synced).
                append(syncHttpRespCode).
                append(syncRetryAt).
                append(syncErrMask).
                toHashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MainSupport mainSupport = (MainSupport)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(syncRetryAt, mainSupport.syncRetryAt) &&
                syncInProgress == mainSupport.syncInProgress &&
                Utils.equalOrBothNull(syncHttpRespCode, mainSupport.syncHttpRespCode) &&
                Utils.equalOrBothNull(syncErrMask, mainSupport.syncErrMask);
    }

    public void overwriteReadonlyProperties(final MainSupport mainSupport) {
        paidEnrollmentEstablishedAt = mainSupport.paidEnrollmentEstablishedAt;
        newishMovementsAddedAt = mainSupport.newishMovementsAddedAt;
        informedOfMaintenanceAt = mainSupport.informedOfMaintenanceAt;
        maintenanceStartsAt = mainSupport.maintenanceStartsAt;
        maintenanceDuration = mainSupport.maintenanceDuration;
        isPaymentPastDue = mainSupport.isPaymentPastDue;
        paidEnrollmentCancelledAt = mainSupport.paidEnrollmentCancelledAt;
        finalFailedPaymentAttemptOccurredAt = mainSupport.finalFailedPaymentAttemptOccurredAt;
        validateAppStoreReceiptAt = mainSupport.validateAppStoreReceiptAt;
    }

    public void overwriteDomainProperties(final MainSupport mainSupport) {
        overwriteReadonlyProperties(mainSupport);
    }

    public void overwrite(final MainSupport mainSupport) {
        super.overwrite(mainSupport);
        overwriteDomainProperties(mainSupport);
    }

    public final Date maintenanceEndsAt() {
        if (maintenanceStartsAt != null && maintenanceDuration != null) {
            final DateTime maintenanceStartsAtDt = new DateTime(maintenanceStartsAt);
            return maintenanceStartsAtDt.plusMinutes(maintenanceDuration).toDate();
        }
        return null;
    }

    public final boolean hasUnAckdUpcomingMaintenanceWithLastMaintenanceAckAt(final Date maintenanceAckAt) {
        if (maintenanceStartsAt != null && maintenanceDuration != null && informedOfMaintenanceAt != null) {
            final DateTime now = DateTime.now();
            return now.isBefore(new DateTime(maintenanceStartsAt)) &&
                    !now.isAfter(new DateTime(maintenanceEndsAt())) &&
                    (maintenanceAckAt == null || new DateTime(maintenanceAckAt).isBefore(new DateTime(informedOfMaintenanceAt)));
        }
        return false;
    }

    public final boolean isInMaintenanceWindow() {
        if (maintenanceStartsAt != null && maintenanceDuration != null) {
            final DateTime now = DateTime.now();
            final DateTime maintenanceStartsAtDt = new DateTime(maintenanceStartsAt);
            if (now.isAfter(maintenanceStartsAtDt) || now.isEqual(maintenanceStartsAtDt)) {
                final DateTime maintenanceEndsAtDt = new DateTime(maintenanceEndsAt());
                return now.isBefore(maintenanceEndsAtDt) || now.isEqual(maintenanceEndsAtDt);
            }
        }
        return false;
    }
}
