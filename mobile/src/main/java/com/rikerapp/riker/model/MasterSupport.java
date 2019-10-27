package com.rikerapp.riker.model;

import com.google.gson.JsonObject;
import com.rikerapp.riker.Utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class MasterSupport extends ModelSupport implements Cloneable {

    public Date createdAt;
    public Date updatedAt;
    public Date deletedAt;
    public boolean syncInProgress;
    public boolean synced;
    public Integer syncHttpRespCode;
    public Integer syncErrMask;
    public Date syncRetryAt;

    public final void populateCommon(final String globalIdentifier, final JsonObject jsonObject, final String keyPrefix) {
        super.populateCommon(globalIdentifier, jsonObject, keyPrefix);
        createdAt = Utils.getDate(jsonObject, String.format("%s/created-at", keyPrefix));
        updatedAt = Utils.getDate(jsonObject, String.format("%s/updated-at", keyPrefix));
        deletedAt = Utils.getDate(jsonObject, String.format("%s/deleted-at", keyPrefix));
    }

    @Override
    public void overwrite(final ModelSupport modelSupport) {
        super.overwrite(modelSupport);
        final MasterSupport masterSupport = (MasterSupport)modelSupport;
        createdAt = masterSupport.createdAt;
        updatedAt = masterSupport.updatedAt;
        deletedAt = masterSupport.deletedAt;
        syncInProgress = masterSupport.syncInProgress;
        synced = masterSupport.synced;
        syncHttpRespCode = masterSupport.syncHttpRespCode;
        syncErrMask = masterSupport.syncErrMask;
        syncRetryAt = masterSupport.syncRetryAt;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
                append(createdAt).
                append(updatedAt).
                append(deletedAt).
                toHashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) { return true; }
        if (!object.getClass().equals(getClass())) {
            return false;
        }
        final MasterSupport masterSupport = (MasterSupport)object;
        return Utils.equalOrBothNull(createdAt, masterSupport.createdAt) &&
                Utils.equalOrBothNull(updatedAt, masterSupport.updatedAt) &&
                Utils.equalOrBothNull(deletedAt, masterSupport.deletedAt) &&
                Utils.equalOrBothNull(syncRetryAt, masterSupport.syncRetryAt) &&
                syncInProgress == masterSupport.syncInProgress &&
                Utils.equalOrBothNull(syncHttpRespCode, masterSupport.syncHttpRespCode) &&
                Utils.equalOrBothNull(syncErrMask, masterSupport.syncErrMask);
    }
}
