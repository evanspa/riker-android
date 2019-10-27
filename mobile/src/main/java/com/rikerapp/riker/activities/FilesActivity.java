package com.rikerapp.riker.activities;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.rikerapp.riker.Function;
import com.rikerapp.riker.R;
import com.rikerapp.riker.adapters.FilesRecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FilesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private FilesRecyclerViewAdapter exportFilesRecyclerViewAdapter;

    public abstract File filesDirectory();

    public abstract String fileNameFilter();

    public abstract boolean deleteOnSwipe();

    public abstract Function.FileOnTouch fileOnTouchFn();

    public abstract Function.IntToString headerTextFn();

    public abstract boolean suppressSubHeaderTextView();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        configureAppBar();
        logScreen(getTitle());
        this.recyclerView = findViewById(R.id.filesRecyclerView);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(linearLayoutManager);
        final File filesDirectory = filesDirectory();
        final File files[] = filesDirectory.listFiles();
        final List<String> fileNameList = new ArrayList<>();
        final String fileNameFilter = fileNameFilter();
        for (final File file : files) {
            final String fileName = file.getName();
            if (fileNameFilter == null || fileName.contains(fileNameFilter)) {
                fileNameList.add(fileName);
            }
        }
        exportFilesRecyclerViewAdapter = new FilesRecyclerViewAdapter(this,
                fileNameList,
                fileOnTouchFn(),
                headerTextFn(),
                suppressSubHeaderTextView());
        recyclerView.setAdapter(exportFilesRecyclerViewAdapter);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public final boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public final int getSwipeDirs(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
                if (deleteOnSwipe()) {
                    final int position = viewHolder.getAdapterPosition();
                    if (position == 0) { // prevent swipe of header
                        return 0;
                    }
                    if (position == (exportFilesRecyclerViewAdapter.files.size() + 1)) { // prevent swipe of footer
                        return 0;
                    }
                    return super.getSwipeDirs(recyclerView, viewHolder);
                } else {
                    return 0;
                }
            }

            @Override
            public final void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                if (deleteOnSwipe()) {
                    final int position = viewHolder.getAdapterPosition();
                    if (position > 0) {
                        final int filePosition = position - 1; // -1 because header is first position in adapter
                        final String fileNameToDelete = exportFilesRecyclerViewAdapter.files.get(filePosition);
                        new File(getFilesDir(), fileNameToDelete).delete();
                        exportFilesRecyclerViewAdapter.files.remove(filePosition);
                        Toast.makeText(FilesActivity.this, "Export file deleted.", Toast.LENGTH_SHORT).show();
                        exportFilesRecyclerViewAdapter.notifyItemRemoved(position);
                        exportFilesRecyclerViewAdapter.notifyItemChanged(0); // so header updates itself
                    }
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}
