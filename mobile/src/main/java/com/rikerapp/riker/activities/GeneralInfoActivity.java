package com.rikerapp.riker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

public final class GeneralInfoActivity extends BaseActivity {

    private boolean howRikerDistsValuesLabelVisible;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_info);
        configureAppBar();
        logScreen(getTitle());
        final TextView howRikerDistributesValuesTextView = (TextView)findViewById(R.id.howRikerDistributesValuesTextView);
        howRikerDistributesValuesTextView.setText(Utils.fromHtml(getResources().getString(R.string.general_info_how_riker_dists_values)));
        howRikerDistributesValuesTextView.setVisibility(View.GONE);
        howRikerDistsValuesLabelVisible = false;
        final TextView howRikerDistributesValuesCaptionTextView = (TextView)findViewById(R.id.howRikerDistributesValuesCaptionTextView);
        final Button howRikerDistributesValuesButton = (Button)findViewById(R.id.howRikerDistributesValuesButton);
        howRikerDistributesValuesButton.setOnClickListener(view -> {
            if (howRikerDistsValuesLabelVisible) {
                howRikerDistributesValuesTextView.setVisibility(View.GONE);
                new Handler().postDelayed(() -> howRikerDistributesValuesCaptionTextView.setVisibility(View.VISIBLE), 75);
            } else {
                howRikerDistributesValuesCaptionTextView.setVisibility(View.GONE);
                new Handler().postDelayed(() -> howRikerDistributesValuesTextView.setVisibility(View.VISIBLE), 75);
            }
            howRikerDistsValuesLabelVisible = !howRikerDistsValuesLabelVisible;
        });
        final Button movementsButton = (Button)findViewById(R.id.movementsButton);
        movementsButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, MuscleGroupsAndMovementsActivity.class);
            intent.putExtra(MuscleGroupsAndMovementsActivity.INTENT_DATA_SHOW_MOVEMENT_DETAILS_ON_MOVEMENT_TAP, true);
            startActivity(intent);
        });
    }
}
