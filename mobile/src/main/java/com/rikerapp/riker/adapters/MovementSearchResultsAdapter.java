package com.rikerapp.riker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.model.MovementSearchResult;

import java.util.List;

public final class MovementSearchResultsAdapter extends ArrayAdapter {

    public List<MovementSearchResult> movementSearchResultList;

    private static class ViewHolder {
        private TextView movementNameTextView;
        private TextView movementAliasesTextView;
    }

    public MovementSearchResultsAdapter(final Context context, final int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public final View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).
                    inflate(R.layout.movement_search_result, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.movementNameTextView =
                    (TextView)convertView.findViewById(R.id.searchResultMovementNameTextView);
            viewHolder.movementAliasesTextView =
                    (TextView)convertView.findViewById(R.id.searchResultMovementAliasesTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MovementSearchResult item = getItem(position);
        if (item != null) {
            viewHolder.movementNameTextView.setText(item.canonicalName);
            if (item.aliases != null && item.aliases.size() > 0) {
                viewHolder.movementAliasesTextView.setVisibility(View.VISIBLE);
                final StringBuilder aliasesText = new StringBuilder("also known as: ");
                final int numAliases = item.aliases.size();
                for (int i = 0; i < numAliases; i++) {
                    aliasesText.append(item.aliases.get(i));
                    if (i + 2 < numAliases) {
                        aliasesText.append(", ");
                    } else if (i + 1 < numAliases){
                        aliasesText.append(" and ");
                    }
                }
                viewHolder.movementAliasesTextView.setText(aliasesText);
            } else {
                viewHolder.movementAliasesTextView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public final MovementSearchResult getItem(final int position) {
        return movementSearchResultList.get(position);
    }

    @Override
    public final int getCount() {
        if (movementSearchResultList != null) {
            return movementSearchResultList.size();
        }
        return 0;
    }
}
