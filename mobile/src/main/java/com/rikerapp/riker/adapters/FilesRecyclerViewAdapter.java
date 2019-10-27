package com.rikerapp.riker.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;

import java.util.List;

public final class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.FileItemViewHolder> {

    private static final int VIEW_TYPE_HEADER = 11;
    private static final int VIEW_TYPE_FOOTER = 12;
    private static final int VIEW_TYPE_ITEM = 13;

    public final List<String> files;

    private final Activity activity;
    private final Function.FileOnTouch fileOnTouchFn;
    private final Function.IntToString headerTextFn;
    private final boolean suppressSubHeaderTextView;

    public FilesRecyclerViewAdapter(final Activity activity,
                                    final List<String> files,
                                    final Function.FileOnTouch fileOnTouchFn,
                                    final Function.IntToString headerTextFn,
                                    final boolean suppressSubHeaderTextView) {
        super();
        this.activity = activity;
        this.files = files;
        this.fileOnTouchFn = fileOnTouchFn;
        this.headerTextFn = headerTextFn;
        this.suppressSubHeaderTextView = suppressSubHeaderTextView;
    }

    @Override
    public final FileItemViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            final View itemView = LayoutInflater.from(this.activity).inflate(R.layout.file_list_item, viewGroup, false);
            return new FileItemViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new FileItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.export_files_header, viewGroup, false));
        } else {
            return new FileItemViewHolder(LayoutInflater.from(this.activity).
                    inflate(R.layout.bottom_margin_default_footer, viewGroup, false));
        }
    }

    @Override
    public final void onBindViewHolder(final FileItemViewHolder viewHolder, final int position) {
        final int numFiles = files.size();
        if (position == 0) { // the header
            viewHolder.filesHeaderTextView.setText(headerTextFn.invoke(numFiles));
            viewHolder.tapFileDetailInfoTextView.setVisibility(!suppressSubHeaderTextView && numFiles > 0 ? View.VISIBLE : View.GONE);
        } else if (position < (numFiles + 1)) {
            final String fileName = files.get(position - 1);
            viewHolder.itemView.setOnClickListener(view -> fileOnTouchFn.invoke(fileName));
            viewHolder.fileNameTextView.setText(fileName);
        } else { // the footer
            // nothing to do (footer is just blank whitespace)
        }
    }

    @Override
    public final int getItemCount() {
        if (files != null) {
            return files.size() + 2; // + 2 for the header and footer
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

    public final static class FileItemViewHolder extends RecyclerView.ViewHolder {

        // header views
        final public TextView filesHeaderTextView;
        final public TextView tapFileDetailInfoTextView;

        // set item views
        final public TextView fileNameTextView;

        public FileItemViewHolder(final View itemView) {
            super(itemView);
            // header views
            this.filesHeaderTextView = (TextView)itemView.findViewById(R.id.filesHeaderTextView);
            this.tapFileDetailInfoTextView = (TextView)itemView.findViewById(R.id.tapFileDetailInfoTextView);
            // file item views
            this.fileNameTextView = (TextView)itemView.findViewById(R.id.fileNameTextView);
        }
    }
}
