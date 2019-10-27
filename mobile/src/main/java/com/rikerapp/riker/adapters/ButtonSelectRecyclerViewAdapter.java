package com.rikerapp.riker.adapters;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.activities.ButtonSelectActivity;
import com.rikerapp.riker.model.BodySegment;
import com.rikerapp.riker.model.MasterSupport;
import com.rikerapp.riker.model.Movement;
import com.rikerapp.riker.model.MovementVariant;
import com.rikerapp.riker.model.MuscleGroup;

import java.util.List;

public class ButtonSelectRecyclerViewAdapter extends RecyclerView.Adapter<ButtonSelectRecyclerViewAdapter.ItemViewHolder> {

    private static final int VIEW_TYPE_HEADER = 11;
    private static final int VIEW_TYPE_FOOTER = 12;
    private static final int VIEW_TYPE_ITEM = 13;

    public List<? extends MasterSupport> dataList;
    public final ButtonSelectActivity.ButtonText buttonText;
    public final ButtonSelectActivity.OnClick onClick;
    @LayoutRes public final int footerResource;
    public final Activity activity;
    public final boolean suppressBodySegmentAndMuscleGroupBreadcrumbs;
    public final BodySegment bodySegment;
    public final MuscleGroup muscleGroup;
    public final Movement movement;
    public final MovementVariant movementVariant;

    public ButtonSelectRecyclerViewAdapter(final Activity activity,
                                           final ButtonSelectActivity.ButtonText buttonText,
                                           final ButtonSelectActivity.OnClick onClick,
                                           @LayoutRes final int footerResource,
                                           final boolean suppressBodySegmentAndMuscleGroupBreadcrumbs,
                                           final BodySegment bodySegment,
                                           final MuscleGroup muscleGroup,
                                           final Movement movement,
                                           final MovementVariant movementVariant) {
        this.activity = activity;
        this.buttonText = buttonText;
        this.onClick = onClick;
        this.footerResource = footerResource;
        this.suppressBodySegmentAndMuscleGroupBreadcrumbs = suppressBodySegmentAndMuscleGroupBreadcrumbs;
        this.bodySegment = bodySegment;
        this.muscleGroup = muscleGroup;
        this.movement = movement;
        this.movementVariant = movementVariant;
    }

    @Override
    public final ItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.select_button_list_item, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new ItemViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.breadcrumbs, parent, false));
        } else {
            return new ItemViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(this.footerResource, parent, false));
        }
    }

    @Override
    public final void onBindViewHolder(final ItemViewHolder viewHolder, final int position) {
        if (position == 0) { // the header
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(
                    0, // left
                    Utils.dpToPx(activity, 10), // top
                    0, // right
                    0); // bottom
            viewHolder.breadcrumbs.setLayoutParams(layoutParams);
            final FlexboxLayout breadcrumbsViewGroup = (FlexboxLayout)viewHolder.breadcrumbs.findViewById(
                    R.id.buttonSelectBreadcrumbsFlexboxLayout);
            breadcrumbsViewGroup.removeAllViews();
            final Utils.AddBreadcrumb addBreadcrumb = Utils.makeBreadcrumbAdder(this.activity, breadcrumbsViewGroup);
            Utils.populateBreadcrumbs(this.bodySegment,
                    this.muscleGroup,
                    this.movement,
                    this.movementVariant,
                    this.suppressBodySegmentAndMuscleGroupBreadcrumbs,
                    addBreadcrumb,
                    viewHolder.breadcrumbs.findViewById(R.id.separator));
        } else if (this.dataList != null && position < (this.dataList.size() + 1)) {
            final MasterSupport masterSupport = this.dataList.get(position - 1);
            viewHolder.selectButton.setText(this.buttonText.invoke(masterSupport));
            viewHolder.selectButton.setOnClickListener(view -> {
                this.onClick.invoke(masterSupport);
            });
        }
    }

    public final void setDataList(final List<? extends MasterSupport> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public final int getItemCount() {
        if (dataList != null) {
            return dataList.size() + 2; // + 2 for the header + footer
        }
        return 2; // for the header + footer
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

    public final static class ItemViewHolder extends RecyclerView.ViewHolder {

        // header views
        final public LinearLayout breadcrumbs;

        // button item views
        final public Button selectButton;

        public ItemViewHolder(final View itemView) {
            super(itemView);
            breadcrumbs = itemView.findViewById(R.id.buttonSelectBreadcrumbs);
            selectButton = itemView.findViewById(R.id.selectButton);
        }
    }
}
