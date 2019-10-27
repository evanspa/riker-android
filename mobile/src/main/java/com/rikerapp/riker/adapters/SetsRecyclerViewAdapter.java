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
import com.rikerapp.riker.activities.SetViewDetailsActivity;
import com.rikerapp.riker.activities.SetsActivity;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.OriginationDevice;
import com.rikerapp.riker.model.Set;
import com.rikerapp.riker.model.WeightUnit;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public final class SetsRecyclerViewAdapter extends RecyclerView.Adapter<SetsRecyclerViewAdapter.SetItemViewHolder> {

    private static final int VIEW_TYPE_HEADER = 11;
    private static final int VIEW_TYPE_FOOTER = 12;
    private static final int VIEW_TYPE_ITEM = 13;

    public SetsActivity.FetchData fetchData;
    public List loadedSets;
    private final Activity activity;

    public SetsRecyclerViewAdapter(final Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public final SetItemViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            final View itemView = LayoutInflater.from(this.activity).inflate(R.layout.set_list_item, viewGroup, false);
            return new SetItemViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new SetItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.sets_header, viewGroup, false));
        } else {
            return new SetItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.bottom_margin_default_footer, viewGroup, false));
        }
    }

    @Override
    public final void onBindViewHolder(final SetItemViewHolder viewHolder, final int position) {
        if (position == 0) { // the header
            final StringBuilder setsHeaderText = new StringBuilder();
            if (fetchData != null) {
                setsHeaderText.append(String.format("You have %s %s.",
                        NumberFormat.getInstance().format(fetchData.totalNumSets),
                        Utils.pluralize("set", fetchData.totalNumSets)));
                viewHolder.setsHeaderTextView.setText(Utils.fromHtml(setsHeaderText.toString()));
            }
        } else if (this.fetchData != null && loadedSets != null && position < (loadedSets.size() + 1)) {
            final Set set = (Set) loadedSets.get(position - 1);
            viewHolder.itemView.setOnClickListener(view -> {
                this.activity.startActivityForResult(SetViewDetailsActivity.makeIntent(this.activity, set, fetchData.allMovements, fetchData.allMovementVariants), 0);
            });
            viewHolder.loggedAtPrettyTextView.setText(new PrettyTime().format(set.loggedAt));
            if (new DateTime(set.loggedAt).isBefore(DateTime.now().minusDays(1))) {
                viewHolder.loggedAtTextView.setVisibility(View.VISIBLE);
                viewHolder.loggedAtTextView.setText(new SimpleDateFormat("MM/dd/yyyy").format(set.loggedAt));
            } else {
                viewHolder.loggedAtTextView.setVisibility(View.GONE);
            }
            if (fetchData.user.globalIdentifier == null || set.synced) {
                viewHolder.syncNeededTextView.setVisibility(View.GONE);
            }
            final Movement movement = this.fetchData.allMovements.get(set.movementId);
            viewHolder.movementTextView.setText(movement.canonicalName);
            if (set.movementVariantId != null) {
                final MovementVariant movementVariant = this.fetchData.allMovementVariants.get(set.movementVariantId);
                viewHolder.movementVariantTextView.setVisibility(View.VISIBLE);
                viewHolder.movementVariantTextView.setText(movementVariant.name);
            } else {
                viewHolder.movementVariantTextView.setVisibility(View.GONE);
            }
            viewHolder.repsAndWeightTextView.setText(
                    String.format("%d %s of %s %s",
                            set.numReps,
                            Utils.pluralize("rep", set.numReps),
                            Utils.formatWeightSizeValue(set.weight),
                            WeightUnit.weightUnitById(set.weightUom).name));
            final OriginationDevice.Id originationDeviceId = OriginationDevice.Id.originationDeviceIdById(set.originationDeviceId);
            viewHolder.originationDeviceImageView.setImageResource(originationDeviceId.drawableResource);
            viewHolder.importedImageView.setVisibility(set.importedAt != null ? View.VISIBLE : View.GONE);

            // for testing the look and feel of origination device icons
            /*if (count == 0) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_web); count++;}
            else if (count == 1) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_iphone); count++;}
            else if (count == 2) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_ipad); count++;}
            else if (count == 3) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_apple_watch); count++;}
            else if (count == 4) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_android_wear); count++;}
            else if (count == 5) { viewHolder.originationDeviceImageView.setImageResource(R.drawable.orig_device_android); count = 0;}*/
        } else { // the footer
            // nothing to do (footer is just blank whitespace)
        }
    }

    //int count = 0; // for testing the look and feel of origination device icons

    @Override
    public final int getItemCount() {
        if (loadedSets != null) {
            return loadedSets.size() + 2; // + 2 for the header and footer
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

    public final void setFetchData(final SetsActivity.FetchData fetchData) {
        this.fetchData = fetchData;
        if (fetchData == null) {
            if (loadedSets != null) {
                loadedSets.clear();
            }
            notifyDataSetChanged();
        } else {
            if (loadedSets == null) {
                loadedSets = new ArrayList();
            }
            loadedSets.addAll(fetchData.sets);
            notifyDataSetChanged();
        }
    }

    public final static class SetItemViewHolder extends RecyclerView.ViewHolder {

        // header views
        final public TextView setsHeaderTextView;

        // set item views
        final public TextView loggedAtPrettyTextView;
        final public TextView loggedAtTextView;
        final public TextView syncNeededTextView;
        final public TextView movementTextView;
        final public TextView movementVariantTextView;
        final public TextView repsAndWeightTextView;
        final public ImageView originationDeviceImageView;
        final public ImageView importedImageView;

        public SetItemViewHolder(final View itemView) {
            super(itemView);
            // header views
            this.setsHeaderTextView = itemView.findViewById(R.id.setsHeaderTextView);
            // set item views
            this.loggedAtPrettyTextView = itemView.findViewById(R.id.setLoggedAtPrettyTextView);
            this.loggedAtTextView = itemView.findViewById(R.id.setLoggedAtTextView);
            this.syncNeededTextView = itemView.findViewById(R.id.setSyncNeededTextView);
            this.movementTextView = itemView.findViewById(R.id.movementTextView);
            this.movementVariantTextView = itemView.findViewById(R.id.movementVariantTextView);
            this.repsAndWeightTextView = itemView.findViewById(R.id.repsAndWeightTextView);
            this.originationDeviceImageView = itemView.findViewById(R.id.originationDeviceImageView);
            this.importedImageView = itemView.findViewById(R.id.importedImageView);
        }
    }
}
