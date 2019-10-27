package com.rikerapp.riker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.activities.BmlViewDetailsActivity;
import com.rikerapp.riker.activities.BmlsActivity;
import com.rikerapp.riker.model.BodyMeasurementLog;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.SizeUnit;
import com.rikerapp.riker.model.WeightUnit;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public final class BmlsRecyclerViewAdapter extends RecyclerView.Adapter<BmlsRecyclerViewAdapter.BmlItemViewHolder> {

    private static final int VIEW_TYPE_HEADER = 11;
    private static final int VIEW_TYPE_FOOTER = 12;
    private static final int VIEW_TYPE_ITEM = 13;

    public BmlsActivity.FetchData fetchData;
    public List loadedBmls;
    private final Activity activity;

    public BmlsRecyclerViewAdapter(final Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public final BmlItemViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            final View itemView = LayoutInflater.from(this.activity).inflate(R.layout.bml_list_item, viewGroup, false);
            return new BmlItemViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new BmlItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.bmls_header, viewGroup, false));
        } else {
            return new BmlItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.bottom_margin_default_footer, viewGroup, false));
        }
    }

    @Override
    public final void onBindViewHolder(final BmlItemViewHolder viewHolder, final int position) {
        if (position == 0) { // the header
            final StringBuilder bmlsHeaderText = new StringBuilder();
            if (fetchData != null) {
                bmlsHeaderText.append(String.format("You have %s %s.",
                        NumberFormat.getInstance().format(fetchData.totalNumBmls),
                        Utils.pluralize("body log", fetchData.totalNumBmls)));
                viewHolder.bmlsHeaderTextView.setText(Utils.fromHtml(bmlsHeaderText.toString()));
            }
        } else if (this.fetchData != null && this.loadedBmls != null && position < (this.loadedBmls.size() + 1)) {
            final BodyMeasurementLog bml = (BodyMeasurementLog) loadedBmls.get(position - 1);
            viewHolder.itemView.setOnClickListener(view -> {
                this.activity.startActivityForResult(BmlViewDetailsActivity.makeIntent(this.activity, bml), 0);
            });
            viewHolder.loggedAtPrettyTextView.setText(new PrettyTime().format(bml.loggedAt));
            if (new DateTime(bml.loggedAt).isBefore(DateTime.now().minusDays(1))) {
                viewHolder.loggedAtTextView.setVisibility(View.VISIBLE);
                viewHolder.loggedAtTextView.setText(new SimpleDateFormat("MM/dd/yyyy").format(bml.loggedAt));
            } else {
                viewHolder.loggedAtTextView.setVisibility(View.GONE);
            }
            if (fetchData.user.globalIdentifier == null || bml.synced) {
                viewHolder.syncNeededTextView.setVisibility(View.GONE);
            }
            final List<String> bmlTextList = new ArrayList<>();
            if (bml.bodyWeight != null) {
                bmlTextList.add(String.format("Body weight: %s %s", Utils.formatWeightSizeValue(bml.bodyWeight), WeightUnit.weightUnitById(bml.bodyWeightUom).name));
            }
            SizeUnit sizeUnit;
            if (bml.sizeUom != null) {
                sizeUnit = SizeUnit.sizeUnitById(bml.sizeUom);
                if (bml.armSize != null) { bmlTextList.add(String.format("Arm size: %s %s", Utils.formatWeightSizeValue(bml.armSize), sizeUnit.name)); }
                if (bml.chestSize != null) { bmlTextList.add(String.format("Chest size: %s %s", Utils.formatWeightSizeValue(bml.chestSize), sizeUnit.name)); }
                if (bml.calfSize != null) { bmlTextList.add(String.format("Calve size: %s %s", Utils.formatWeightSizeValue(bml.calfSize), sizeUnit.name)); }
                if (bml.neckSize != null) { bmlTextList.add(String.format("Neck size: %s %s", Utils.formatWeightSizeValue(bml.neckSize), sizeUnit.name)); }
                if (bml.waistSize != null) { bmlTextList.add(String.format("Waist size: %s %s", Utils.formatWeightSizeValue(bml.waistSize), sizeUnit.name)); }
                if (bml.thighSize != null) { bmlTextList.add(String.format("Thigh size: %s %s", Utils.formatWeightSizeValue(bml.thighSize), sizeUnit.name)); }
                if (bml.forearmSize != null) { bmlTextList.add(String.format("Forearm size: %s %s", Utils.formatWeightSizeValue(bml.forearmSize), sizeUnit.name)); }
            }
            if (bmlTextList.size() > 1) {
                viewHolder.bmlTextView.setText("Multiple values set");
            } else {
                viewHolder.bmlTextView.setText(bmlTextList.get(0));
            }
            final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(bml.originationDeviceId);
            viewHolder.originationDeviceImageView.setImageResource(originationDeviceId.drawableResource);
            viewHolder.importedImageView.setVisibility(bml.importedAt != null ? View.VISIBLE : View.GONE);
        } else { // the footer
            // nothing to do (footer is just blank whitespace)
        }
    }

    //int count = 0; // for testing the look and feel of origination device icons

    @Override
    public final int getItemCount() {
        if (loadedBmls != null) {
            return loadedBmls.size() + 2; // + 2 for the header and footer
        }
        return 2; // for the header and footer
    }

    @Override
    public final int getItemViewType(final int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        if (position == (getItemCount() - 1)) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    public final void setFetchData(final BmlsActivity.FetchData fetchData) {
        this.fetchData = fetchData;
        if (fetchData == null) {
            if (loadedBmls != null) {
                loadedBmls.clear();
            }
            notifyDataSetChanged();
        } else {
            if (loadedBmls == null) {
                loadedBmls = new ArrayList();
            }
            loadedBmls.addAll(fetchData.bmls);
            notifyDataSetChanged();
        }
    }

    public final static class BmlItemViewHolder extends RecyclerView.ViewHolder {

        // header views
        final public TextView bmlsHeaderTextView;

        // bml item views
        final public TextView loggedAtPrettyTextView;
        final public TextView loggedAtTextView;
        final public TextView syncNeededTextView;
        final public TextView bmlTextView;
        final public ImageView originationDeviceImageView;
        final public ImageView importedImageView;

        public BmlItemViewHolder(final View itemView) {
            super(itemView);
            // header views
            this.bmlsHeaderTextView = itemView.findViewById(R.id.bmlsHeaderTextView);
            // set item views
            this.loggedAtPrettyTextView = itemView.findViewById(R.id.bmlLoggedAtPrettyTextView);
            this.loggedAtTextView = itemView.findViewById(R.id.bmlLoggedAtTextView);
            this.syncNeededTextView = itemView.findViewById(R.id.bmlSyncNeededTextView);
            this.bmlTextView = itemView.findViewById(R.id.bmlTextView);
            this.originationDeviceImageView = itemView.findViewById(R.id.originationDeviceImageView);
            this.importedImageView = itemView.findViewById(R.id.importedImageView);
        }
    }
}
