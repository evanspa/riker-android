package com.rikerapp.riker.importexport;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;

import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.OriginationDevice;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class PrepareBmlsImportTask extends AsyncTask<Void, Void, BmlImportPrepResult> {

    private static final int NUM_FIELDS = 16;

    private final RikerApp rikerApp;
    private final Uri fileUri;

    public PrepareBmlsImportTask(final RikerApp rikerApp, final Uri fileUri) {
        super();
        this.rikerApp = rikerApp;
        this.fileUri = fileUri;
    }

    @Override
    protected final BmlImportPrepResult doInBackground(final Void... noArgs) {
        CSVParser csvParser;
        final List<ImportError> errors = new ArrayList<>();
        boolean anyReferenceErrors = false;
        final Map<Integer, OriginationDevice> allOriginationDevices = Utils.toMap(rikerApp.dao.originationDevices());
        final List<BodyMeasurementLog> bmlsToSave = new ArrayList<>();
        try {
            final Cursor cursor = this.rikerApp.getContentResolver().query(fileUri, null, null, null, null, null);
            cursor.moveToFirst();
            final String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
            final InputStream inputStream = this.rikerApp.getContentResolver().openInputStream(fileUri);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            csvParser = CSVFormat.DEFAULT.parse(reader);
            final List<CSVRecord> recordList = csvParser.getRecords();
            final int numRecords = recordList.size();
            for (int i = 1; i < numRecords; i++) { // i starts at 1 (not 0) so we skip headings line
                final CSVRecord csvRecord = recordList.get(i);
                if (csvRecord.size() != NUM_FIELDS) {
                    errors.add(new ImportError(String.format("Wrong number of records found (%d).  Should be: %d", csvRecord.size(), NUM_FIELDS), i, false));
                } else {
                    Integer originationDeviceId = null;
                    try {
                        originationDeviceId = new Integer(csvRecord.get(15));
                        if (allOriginationDevices.get(originationDeviceId) == null) {
                            anyReferenceErrors = true;
                            errors.add(new ImportError(String.format("References Riker system data not present on your device."), i, true));
                        }
                    } catch (NumberFormatException nfe) {
                        errors.add(new ImportError("Invalid origination device ID value.", i, false));
                    }
                    if (errors.size() == 0) {
                        final Date loggedAt = Utils.parseDate(csvRecord, 1, "Invalid logged at date.", errors, i);
                        final BigDecimal bodyWeight = Utils.parseBigDecimal(csvRecord, 2, "Invalid body weight value.", errors, i);
                        final int bodyWeightUomId = Utils.parseInteger(csvRecord, 4, "Invalid body weight units value.", errors, i);
                        final BigDecimal calfSize = Utils.parseBigDecimal(csvRecord, 5, "Invalid calf size value.", errors, i);
                        final BigDecimal chestSize = Utils.parseBigDecimal(csvRecord, 6, "Invalid chest size value.", errors, i);
                        final BigDecimal armSize = Utils.parseBigDecimal(csvRecord, 7, "Invalid arm size value.", errors, i);
                        final BigDecimal neckSize = Utils.parseBigDecimal(csvRecord, 8, "Invalid neck size value.", errors, i);
                        final BigDecimal waistSize = Utils.parseBigDecimal(csvRecord, 9, "Invalid waist size value.", errors, i);
                        final BigDecimal thighSize = Utils.parseBigDecimal(csvRecord, 10, "Invalid thigh size value.", errors, i);
                        final BigDecimal forearmSize = Utils.parseBigDecimal(csvRecord, 11, "Invalid forearm size value.", errors, i);
                        final Integer sizeUomId = Utils.parseInteger(csvRecord, 13, "Invalid size units value.", errors, i);
                        if (errors.size() == 0) {
                            final BodyMeasurementLog bml = new BodyMeasurementLog();
                            bml.importedAt = new Date();
                            bml.loggedAt = loggedAt;
                            bml.bodyWeight = bodyWeight;
                            bml.bodyWeightUom = bodyWeightUomId;
                            bml.sizeUom = sizeUomId;
                            bml.calfSize = calfSize;
                            bml.chestSize = chestSize;
                            bml.armSize = armSize;
                            bml.neckSize = neckSize;
                            bml.waistSize = waistSize;
                            bml.thighSize = thighSize;
                            bml.forearmSize = forearmSize;
                            bml.originationDeviceId = originationDeviceId;
                            bmlsToSave.add(bml);
                        }
                    }
                }
            }
            csvParser.close();
            return new BmlImportPrepResult(bmlsToSave, errors, anyReferenceErrors, displayName);
        } catch (Throwable any) {
            return new BmlImportPrepResult(any);
        }
    }

    @Override
    protected final void onPostExecute(final BmlImportPrepResult importPrepResult) {
        EventBus.getDefault().post(importPrepResult);
    }
}
