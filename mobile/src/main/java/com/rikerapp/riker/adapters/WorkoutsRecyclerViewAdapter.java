package com.rikerapp.riker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.model.MuscleGroupTuple;
import com.rikerapp.riker.model.Workout;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WorkoutsRecyclerViewAdapter extends  RecyclerView.Adapter<WorkoutsRecyclerViewAdapter.WorkoutItemViewHolder> {

    private static final int VIEW_TYPE_HEADER = 11;
    private static final int VIEW_TYPE_FOOTER = 12;
    private static final int VIEW_TYPE_ITEM = 13;

    public List fetchDataWorkouts;
    public List loadedWorkouts;
    private final Activity activity;

    public WorkoutsRecyclerViewAdapter(final Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public final WorkoutItemViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            final View itemView = LayoutInflater.from(this.activity).inflate(R.layout.workout_list_item, viewGroup, false);
            return new WorkoutItemViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new WorkoutItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.workouts_header, viewGroup, false));
        } else {
            return new WorkoutItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.bottom_margin_default_footer, viewGroup, false));
        }
    }

    @Override
    public final void onBindViewHolder(final WorkoutItemViewHolder viewHolder, final int position) {
        if (position == 0) { // the header
            // nothing to do (header is just static text)
        } else if (this.fetchDataWorkouts != null && loadedWorkouts != null && position < (loadedWorkouts.size() + 1)) {
            final Workout workout = (Workout)loadedWorkouts.get(position - 1);
            viewHolder.workoutTimeAgoTextView.setText(new PrettyTime().format(workout.startDate));
            viewHolder.workoutDateTextView.setText(new SimpleDateFormat("MM/dd/yyyy").format(workout.startDate));
            if (workout.caloriesBurned != null) {
                viewHolder.workoutCaloriesTextView.setVisibility(View.VISIBLE);
                viewHolder.workoutCaloriesTextView.setText(String.format("%.1f kcal", workout.caloriesBurned.floatValue()));
            } else {
                viewHolder.workoutCaloriesTextView.setVisibility(View.GONE);
            }
            final String durationInMinStr = String.format("%.1f", workout.workoutDurationInSeconds / 60.0);
            viewHolder.workoutDurationTextView.setText(String.format("%s minute%s", durationInMinStr, durationInMinStr.equals("1") ? "" : "s"));

            final List<MuscleGroupTuple> muscleGroupTuples = workout.impactedMuscleGroupTuples;
            final MuscleGroupTuple primaryMuscleGroup = muscleGroupTuples.get(0);
            viewHolder.impactedMuscleGroupsContainer.removeAllViews();
            final TextView primaryImpactedMuscleGroupTextView = (TextView)LayoutInflater.from(this.activity).inflate(
                    R.layout.impacted_muscle_primary_text_view,
                    viewHolder.impactedMuscleGroupsContainer,
                    false);
            primaryImpactedMuscleGroupTextView.setText(String.format("%s - %.0f%%", primaryMuscleGroup.muscleGroupName, primaryMuscleGroup.percentageOfTotalWorkout.floatValue() * 100));
            viewHolder.impactedMuscleGroupsContainer.addView(primaryImpactedMuscleGroupTextView);
            if (muscleGroupTuples.size() > 1) {
                for (int i = 1; i < muscleGroupTuples.size(); i++) {
                    final MuscleGroupTuple muscleGroupTuple = muscleGroupTuples.get(i);
                    final TextView impactedMuscleGroupTextView = (TextView)LayoutInflater.from(this.activity).inflate(
                            R.layout.impacted_muscle_secondary_text_view,
                            viewHolder.impactedMuscleGroupsContainer,
                            false);
                    impactedMuscleGroupTextView.setText(String.format("%s - %.0f%%", muscleGroupTuple.muscleGroupName, muscleGroupTuple.percentageOfTotalWorkout.floatValue() * 100));
                    viewHolder.impactedMuscleGroupsContainer.addView(impactedMuscleGroupTextView);
                }
            }

        } else { // the footer
            // nothing to do (footer is just blank whitespace)
        }
    }

    @Override
    public final int getItemCount() {
        if (loadedWorkouts != null) {
            return loadedWorkouts.size() + 2; // + 2 for the header and footer
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

    public final void setFetchData(final List<Workout> fetchDataWorkouts) {
        this.fetchDataWorkouts = fetchDataWorkouts;
        if (fetchDataWorkouts == null) {
            if (loadedWorkouts!= null) {
                loadedWorkouts.clear();
            }
            notifyDataSetChanged();
        } else {
            if (loadedWorkouts == null) {
                loadedWorkouts = new ArrayList();
            }
            loadedWorkouts.addAll(fetchDataWorkouts);
            notifyDataSetChanged();
        }
    }

    public final static class WorkoutItemViewHolder extends RecyclerView.ViewHolder {

        final public TextView workoutTimeAgoTextView;
        final public TextView workoutDateTextView;
        final public TextView workoutDurationTextView;
        final public TextView workoutCaloriesTextView;
        final public ViewGroup impactedMuscleGroupsContainer;

        public WorkoutItemViewHolder(final View itemView) {
            super(itemView);
            this.workoutTimeAgoTextView = itemView.findViewById(R.id.workoutTimeAgoTextView);
            this.workoutDateTextView = itemView.findViewById(R.id.workoutDateTextView);
            this.workoutDurationTextView = itemView.findViewById(R.id.workoutDurationTextView);
            this.workoutCaloriesTextView = itemView.findViewById(R.id.workoutCaloriesTextView);
            this.impactedMuscleGroupsContainer = itemView.findViewById(R.id.impactedMuscleGroupsContainer);
        }
    }
}
