package com.rikerapp.riker.activities;

import android.os.Environment;

import com.rikerapp.riker.Function;

import java.io.File;

public final class SetImportFromSDCardActivity extends FilesActivity {

    @Override
    public final File filesDirectory() { return Environment.getExternalStorageDirectory(); }

    @Override
    public final String fileNameFilter() { return null; }

    @Override
    public final boolean deleteOnSwipe() { return false; }

    @Override
    public final Function.FileOnTouch fileOnTouchFn() {
        //return fileName -> Utils.shareExportFile(this, fileName, null);
        return fileName -> {};
    }

    @Override
    public final Function.IntToString headerTextFn() {
        return numFiles -> "To import, tap your set data file.";
    }

    @Override
    public final boolean suppressSubHeaderTextView() { return true; }
}
