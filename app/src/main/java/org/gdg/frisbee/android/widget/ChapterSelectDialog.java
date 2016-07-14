package org.gdg.frisbee.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.view.FilterListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChapterSelectDialog extends AppCompatDialogFragment
    implements AdapterView.OnItemClickListener {

    private static final String EXTRA_CHAPTERS = "EXTRA_CHAPTERS";

    @BindView(R.id.filter) SearchView cityNameSearchView;
    @BindView(android.R.id.list) FilterListView listView;

    private List<Chapter> chapters;

    private Listener listener = Listener.EMPTY;
    private Chapter selectedChapter;

    public static ChapterSelectDialog newInstance(ArrayList<Chapter> chapters) {
        ChapterSelectDialog fragment = new ChapterSelectDialog();
        Bundle args = new Bundle(1);
        args.putParcelableArrayList(EXTRA_CHAPTERS, chapters);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chapters = getArguments().getParcelableArrayList(EXTRA_CHAPTERS);
        selectedChapter = chapters.get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_location_list_preference, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(this);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final CheckedItemAdapter adapter = new CheckedItemAdapter(
            getContext(),
            android.R.layout.simple_list_item_single_choice,
            android.R.id.text1,
            chapters
        );
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true); // First item is always the home chapter.
        final Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                int index = findIndexByValueInFilteredListView(selectedChapter);
                listView.setItemChecked(index, true);
            }
        };

        cityNameSearchView.setOnQueryTextListener(
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText, filterListener);
                    return true;
                }
            }
        );
        cityNameSearchView.requestFocus();
    }

    private int findIndexByValueInFilteredListView(Chapter value) {
        if (value == null || listView == null) {
            return -1;
        }
        Adapter adapter = listView.getAdapter();
        for (int i = adapter.getCount() - 1; i >= 0; i--) {
            Chapter item = (Chapter) adapter.getItem(i);
            if (item.equals(value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        listener = Listener.EMPTY;
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getDialog().isShowing()) {
            selectedChapter = (Chapter) parent.getItemAtPosition(position);
            listener.onChapterSelected(selectedChapter);
            getDialog().dismiss();
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<Chapter> {
        CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                           List<Chapter> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    interface Listener {
        void onChapterSelected(Chapter selectedChapter);

        Listener EMPTY = new Listener() {
            @Override
            public void onChapterSelected(Chapter selectedChapter) {
                // no-op
            }
        };
    }
}
