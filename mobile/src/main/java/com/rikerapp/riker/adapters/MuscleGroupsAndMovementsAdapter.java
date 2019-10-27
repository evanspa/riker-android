package com.rikerapp.riker.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.activities.MovementDetailActivity;
import com.rikerapp.riker.activities.MuscleGroupsAndMovementsActivity;
import com.rikerapp.riker.model.Movement;

public class MuscleGroupsAndMovementsAdapter extends RecyclerView.Adapter<MuscleGroupsAndMovementsAdapter.MuscleGroupsAndMovementsViewHolder> {

    public static final String INTENT_DATA_TAPPED_MOVEMENT_ID = "INTENT_DATA_TAPPED_MOVEMENT_ID";

    private static final int VIEW_TYPE_MUSCLE_GROUP = 11;
    private static final int VIEW_TYPE_MOVEMENT = 12;

    private final Activity activity;
    private final Integer selectedMovementId;
    private MuscleGroupsAndMovementsActivity.FetchData fetchData;
    private final boolean finishOnMovementTap;
    private final boolean showMovementDetailsOnMovementTap;

    public MuscleGroupsAndMovementsAdapter(final Activity activity,
                                           final Integer selectedMovementId,
                                           final boolean finishOnMovementTap,
                                           final boolean showMovementDetailsOnMovementTap) {
        super();
        this.activity = activity;
        this.selectedMovementId = selectedMovementId;
        this.finishOnMovementTap = finishOnMovementTap;
        this.showMovementDetailsOnMovementTap = showMovementDetailsOnMovementTap;
    }

    @Override
    public final MuscleGroupsAndMovementsViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        @LayoutRes  int resource;
        if (viewType == VIEW_TYPE_MUSCLE_GROUP) {
            resource = R.layout.muscle_group_list_item;
        } else {
            resource = R.layout.movement_list_item;
        }
        return new MuscleGroupsAndMovementsViewHolder(LayoutInflater.from(this.activity).inflate(
                resource, viewGroup, false));
    }

    @Override
    public final void onBindViewHolder(final MuscleGroupsAndMovementsViewHolder viewHolder, final int position) {
        final Object muscleGroupOrMovement = fetchData.muscleGroupsAndMovements.get(position);
        String labelText;
        if (muscleGroupOrMovement instanceof String) {
            labelText = (String)muscleGroupOrMovement;
        } else {
            final Movement movement = (Movement)muscleGroupOrMovement;
            labelText = movement.canonicalName;
            if (selectedMovementId != null && selectedMovementId.equals(movement.localIdentifier)) {
                viewHolder.checkmarkImageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkmarkImageView.setVisibility(View.GONE);
            }
            if (this.finishOnMovementTap) {
                viewHolder.itemView.setOnClickListener(view -> {
                    final Intent intent = new Intent();
                    intent.putExtra(INTENT_DATA_TAPPED_MOVEMENT_ID, movement.localIdentifier);
                    this.activity.setResult(0, intent);
                    this.activity.finish();
                });
            } else if (this.showMovementDetailsOnMovementTap) {
                viewHolder.itemView.setOnClickListener(view -> this.activity.startActivity(MovementDetailActivity.makeIntent(this.activity, movement, true)));
            }
        }
        viewHolder.labelTextView.setText(labelText);
        if (position < (fetchData.muscleGroupsAndMovements.size() - 1)) {
            if (viewHolder.footerSpace != null) {
                viewHolder.footerSpace.setVisibility(View.GONE);
            }
        } else {
            viewHolder.footerSpace.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final int getItemCount() {
        if (fetchData != null && fetchData.muscleGroupsAndMovements != null) {
            return fetchData.muscleGroupsAndMovements.size();
        }
        return 0;
    }

    @Override
    public final int getItemViewType(final int position) {
        final Object muscleGroupOrMovement = fetchData.muscleGroupsAndMovements.get(position);
        if (muscleGroupOrMovement instanceof String) {
            return VIEW_TYPE_MUSCLE_GROUP;
        }
        return VIEW_TYPE_MOVEMENT;
    }

    public final void setFetchData(final MuscleGroupsAndMovementsActivity.FetchData fetchData) {
        this.fetchData = fetchData;
        notifyDataSetChanged();
    }

    public final static class MuscleGroupsAndMovementsViewHolder extends RecyclerView.ViewHolder {

        public final TextView labelTextView;
        public final ImageView checkmarkImageView;
        public final View footerSpace;

        public MuscleGroupsAndMovementsViewHolder(final View itemView) {
            super(itemView);
            this.checkmarkImageView = (ImageView)itemView.findViewById(R.id.checkmarkImageView);
            this.labelTextView = (TextView)itemView.findViewById(R.id.labelTextView);
            this.footerSpace = itemView.findViewById(R.id.movementFooterSpace);
        }
    }
}
