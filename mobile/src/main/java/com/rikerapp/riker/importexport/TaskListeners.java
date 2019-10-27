package com.rikerapp.riker.importexport;

public interface TaskListeners {

    interface SetPrepImportAsyncTaskListener { void taskComplete(String fileName, SetImportPrepResult taskResult); }
    interface SetImportAsyncTaskListener { void taskComplete(SetImportResult taskResult); }
    interface BmlPrepImportAsyncTaskListener { void taskComplete(String fileName, BmlImportPrepResult taskResult); }
    interface BmlImportAsyncTaskListener { void taskComplete(BmlImportResult taskResult); }
}
