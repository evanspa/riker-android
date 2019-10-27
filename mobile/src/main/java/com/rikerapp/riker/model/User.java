package com.rikerapp.riker.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Parcel
public final class User extends MainSupport implements Cloneable {

    public static final String JSON_KEY_PREFIX = "user";

    public UserSettings userSettings;
    public String name;
    public String email;
    public String password;
    public String confirmPassword;
    public String lastChargeId;
    public Date trialAlmostExpiredNoticeSentAt;
    public String latestStripeTokenId;
    public Date nextInvoiceAt;
    public Integer nextInvoiceAmount;
    public Date lastInvoiceAt;
    public Integer lastInvoiceAmount;
    public String currentCardLast4;
    public String currentCardBrand;
    public Integer currentCardExpYear;
    public Integer currentCardExpMonth;
    public Date trialEndsAt;
    public String stripeCustomerId;
    public Integer cancelSubscription;
    public String paidEnrollmentCancelledReason;
    public String appStoreReceiptDataBase64;
    public Integer maxAllowedSetImport;
    public Integer maxAllowedBmlImport;

    public List<BodySegment> bodySegmentList = new ArrayList<>();
    public List<MuscleGroup> muscleGroupList = new ArrayList<>();
    public List<Muscle> muscleList = new ArrayList<>();
    public List<MuscleAlias> muscleAliasList = new ArrayList<>();
    public List<Movement> movementList = new ArrayList<>();
    public List<MovementAlias> movementAliasList = new ArrayList<>();
    public List<MovementVariant> movementVariantList = new ArrayList<>();
    public List<OriginationDevice> originationDeviceList = new ArrayList<>();
    public List<Set> setList = new ArrayList<>();
    public List<BodyMeasurementLog> bmlList = new ArrayList<>();

    public static String key(final String key) {
        return String.format("%s/%s", JSON_KEY_PREFIX, key);
    }

    public static User toUser(final JsonObject jsonObject, final String globalIdentifier) {
        final User user = new User();
        user.populateCommon(globalIdentifier, jsonObject, JSON_KEY_PREFIX);
        user.populateUserCommon(jsonObject);
        user.name =                           Utils.getString(jsonObject,  key("name"));
        user.email =                          Utils.getString(jsonObject,  key("email"));
        user.lastChargeId =                   Utils.getString(jsonObject,  key("last-charge-id"));
        user.trialAlmostExpiredNoticeSentAt = Utils.getDate(jsonObject,    key("trial-almost-expired-notice-sent-at"));
        user.latestStripeTokenId =            Utils.getString(jsonObject,  key("latest-stripe-token-id"));
        user.nextInvoiceAt =                  Utils.getDate(jsonObject,    key("next-invoice-at"));
        user.nextInvoiceAmount =              Utils.getInteger(jsonObject, key("next-invoice-amount"));
        user.lastInvoiceAt =                  Utils.getDate(jsonObject,    key("last-invoice-at"));
        user.lastInvoiceAmount =              Utils.getInteger(jsonObject, key("last-invoice-amount"));
        user.currentCardLast4 =               Utils.getString(jsonObject,  key("current-card-last4"));
        user.currentCardBrand =               Utils.getString(jsonObject,  key("current-card-brand"));
        user.currentCardExpYear =             Utils.getInteger(jsonObject, key("current-card-exp-year"));
        user.currentCardExpMonth =            Utils.getInteger(jsonObject, key("current-card-exp-month"));
        user.trialEndsAt =                    Utils.getDate(jsonObject,    key("trial-ends-at"));
        user.stripeCustomerId =               Utils.getString(jsonObject,  key("stripe-customer-id"));
        user.paidEnrollmentCancelledReason =  Utils.getString(jsonObject,  key("paid-enrollment-cancelled-reason"));
        user.appStoreReceiptDataBase64 =      Utils.getString(jsonObject,  key("app-store-receipt-data-base64"));
        user.maxAllowedSetImport =            Utils.getInteger(jsonObject, key("max-allowed-set-import"));
        user.maxAllowedBmlImport =            Utils.getInteger(jsonObject, key("max-allowed-bml-import"));
        final JsonElement jsonElementEmbedded = jsonObject.get("_embedded");
        if (jsonElementEmbedded != null) {
            final JsonArray jsonArrayEmbedded = jsonElementEmbedded.getAsJsonArray();
            final int numEmbedded = jsonArrayEmbedded.size();
            final KnownRikerMediaType knownRikerMediaTypes[] = KnownRikerMediaType.values();
            for (int i = 0; i < numEmbedded; i++) {
                final JsonElement jsonElement = jsonArrayEmbedded.get(i);
                final JsonObject entity = jsonElement.getAsJsonObject();
                final String mediaType = Utils.getString(entity, "media-type");
                if (mediaType != null) {
                    final KnownRikerMediaType knownRikerMediaType = KnownRikerMediaType.lookup(mediaType, knownRikerMediaTypes);
                    if (knownRikerMediaType != null) {
                        final String entityGlobalIdentifier = Utils.getString(entity, "location");
                        final JsonObject entityPayload = entity.getAsJsonObject("payload");
                        switch (knownRikerMediaType) {
                            case BODY_SEGMENT:
                                user.bodySegmentList.add(BodySegment.toBodySegment(entityPayload, entityGlobalIdentifier));
                                break;
                            case MUSCLE_GROUP:
                                user.muscleGroupList.add(MuscleGroup.toMuscleGroup(entityPayload, entityGlobalIdentifier));
                                break;
                            case MUSCLE:
                                user.muscleList.add(Muscle.toMuscle(entityPayload, entityGlobalIdentifier));
                                break;
                            case MUSCLE_ALIAS:
                                user.muscleAliasList.add(MuscleAlias.toMuscleAlias(entityPayload, entityGlobalIdentifier));
                                break;
                            case MOVEMENT:
                                user.movementList.add(Movement.toMovement(entityPayload, entityGlobalIdentifier));
                                break;
                            case MOVEMENT_ALIAS:
                                user.movementAliasList.add(MovementAlias.toMovementAlias(entityPayload, entityGlobalIdentifier));
                                break;
                            case MOVEMENT_VARIANT:
                                user.movementVariantList.add(MovementVariant.toMovementVariant(entityPayload, entityGlobalIdentifier));
                                break;
                            case ORIGINATION_DEVICES:
                                user.originationDeviceList.add(OriginationDevice.toOriginationDevice(entityPayload, entityGlobalIdentifier));
                                break;
                            case USER_SETTINGS:
                                user.userSettings = UserSettings.toUserSettings(entityPayload, entityGlobalIdentifier);
                                break;
                            case SET:
                                user.setList.add(Set.toSet(entityPayload, entityGlobalIdentifier));
                                break;
                            case BML:
                                user.bmlList.add(BodyMeasurementLog.toBml(entityPayload, entityGlobalIdentifier));
                                break;
                        }
                    }
                }

            }
        }
        return user;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(29, 31).
                append(name).
                append(email).
                append(password).
                append(verifiedAt).
                toHashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final User user = (User)object;
        return super.equals(object) &&
                Utils.equalOrBothNull(name, user.name) &&
                Utils.equalOrBothNull(email, user.email) &&
                Utils.equalOrBothNull(password, user.password) &&
                Utils.equalOrBothNull(verifiedAt, user.verifiedAt);
    }

    public final void overwriteDomainProperties(final User user) {
        super.overwriteDomainProperties(user);
        name = user.name;
        email = user.email;
        password = user.password;
        verifiedAt = user.verifiedAt;
        lastChargeId = user.lastChargeId;
        trialAlmostExpiredNoticeSentAt = user.trialAlmostExpiredNoticeSentAt;
        latestStripeTokenId = user.latestStripeTokenId;
        nextInvoiceAt = user.nextInvoiceAt;
        nextInvoiceAmount = user.nextInvoiceAmount;
        lastInvoiceAt = user.lastInvoiceAt;
        lastInvoiceAmount = user.lastInvoiceAmount;
        currentCardLast4 = user.currentCardLast4;
        currentCardBrand = user.currentCardBrand;
        currentCardExpYear = user.currentCardExpYear;
        currentCardExpMonth = user.currentCardExpMonth;
        trialEndsAt = user.trialEndsAt;
        stripeCustomerId = user.stripeCustomerId;
        maxAllowedSetImport = user.maxAllowedSetImport;
        maxAllowedBmlImport = user.maxAllowedBmlImport;
    }

    @Override
    public final void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        overwriteDomainProperties((BodyMeasurementLog) modelSupport);
    }

    public final boolean hasPaidAccount() {
        return paidEnrollmentEstablishedAt != null;
    }

    public final boolean hasPaidIapAccount() {
        return hasPaidAccount() && validateAppStoreReceiptAt != null;
    }

    public final boolean hasCancelledPaidAccount() {
        return paidEnrollmentCancelledAt != null;
    }

    public final boolean hasLapsedPaidAccount() {
        return finalFailedPaymentAttemptOccurredAt != null;
    }

    public final boolean hasTrialAccount() {
        return trialEndsAt != null &&
                !hasPaidAccount() &&
                !hasCancelledPaidAccount() &&
                !hasLapsedPaidAccount();
    }

    public final boolean isTrialPeriodExpired() {
        return hasTrialAccount() && new DateTime(trialEndsAt).isBeforeNow();
    }

    public final boolean isTrialPeriodAlmostExpired() {
        return !isTrialPeriodExpired() && new DateTime(trialEndsAt).minusDays(5).isBeforeNow();
    }

    public final boolean isBadAccount() {
        return isTrialPeriodExpired() || hasLapsedPaidAccount() || hasCancelledPaidAccount();
    }

    public final String userIdPartFromGlobalIdentifier() {
        if (globalIdentifier != null) {
            return globalIdentifier.substring(globalIdentifier.lastIndexOf("/") + 1);
        }
        return null;
    }
}
