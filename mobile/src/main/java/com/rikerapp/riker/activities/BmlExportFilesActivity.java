package com.rikerapp.riker.activities;

import com.rikerapp.riker.Function;
import com.rikerapp.riker.Utils;

import java.io.File;

public final class BmlExportFilesActivity extends FilesActivity {

    @Override
    public final File filesDirectory() { return getFilesDir(); }

    @Override
    public final String fileNameFilter() { return "-body-measurement-logs-"; }

    @Override
    public final boolean deleteOnSwipe() { return true; }

    @Override
    public final Function.FileOnTouch fileOnTouchFn() {
        return fileName -> Utils.shareExportFile(this, fileName, null);
    }

    @Override
    public final Function.IntToString headerTextFn() {
        return numFiles -> String.format("You have %d %s.", numFiles, Utils.pluralize("body measurement log export file", numFiles));
    }

    @Override
    public final boolean suppressSubHeaderTextView() { return false; }
}

