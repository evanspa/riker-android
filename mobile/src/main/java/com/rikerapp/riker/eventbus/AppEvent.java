package com.rikerapp.riker.eventbus;

import com.rikerapp.riker.importexport.ExportTaskResult;

public final class AppEvent {

    public abstract static class ExportCompleteEvent {
        public final ExportTaskResult exportTaskResult;
        public ExportCompleteEvent(final ExportTaskResult exportTaskResult) {
            this.exportTaskResult = exportTaskResult;
        }
    }

    public final static class BmlsExportCompleteEvent extends ExportCompleteEvent {
        public BmlsExportCompleteEvent(final ExportTaskResult exportTaskResult) {
            super(exportTaskResult);
        }
    }

    public final static class SetsExportCompleteEvent extends ExportCompleteEvent {
        public SetsExportCompleteEvent(final ExportTaskResult exportTaskResult) {
            super(exportTaskResult);
        }
    }

    public final static class SaveAllChartConfigsCompleteEvent {}

    public final static class DeleteAllDataCompleteEvent {}

    public final static class ConnectToGoogleFit {}

    public final static class CancelConnectToGoogleFit {}

    public static abstract class SyncSetsToGfAbstractResult {}

    public static final class SyncSetsToGfSuccessResult extends SyncSetsToGfAbstractResult {
        public final int numWorkouts;
        public final int numSets;

        public SyncSetsToGfSuccessResult(final int numWorkouts, final int numSets) {
            this.numWorkouts = numWorkouts;
            this.numSets = numSets;
        }
    }

    public static abstract class SyncBmlsToGfAbstractResult {}

    public static final class SyncBmlsToGfSuccessResult extends SyncBmlsToGfAbstractResult {
        public final int numBmls;

        public SyncBmlsToGfSuccessResult(final int numBmls) {
            this.numBmls = numBmls;
        }
    }

    public static abstract class SyncSetsAndBmlsToGfAbstractResult {}

    public static final class SyncSetsAndBmlsToGfSuccessResult extends SyncSetsAndBmlsToGfAbstractResult {
        public final int numWorkouts;
        public final int numSets;
        public final int numBmls;

        public SyncSetsAndBmlsToGfSuccessResult(final int numWorkouts, final int numSets, final int numBmls) {
            this.numWorkouts = numWorkouts;
            this.numSets = numSets;
            this.numBmls = numBmls;
        }
    }
}
