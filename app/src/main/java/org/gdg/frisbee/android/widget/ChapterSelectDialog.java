package org.gdg.frisbee.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import org.gdg.frisbee.android.view.FilterListView;
import org.gdg.frisbee.android.view.LocationListPreference;

public class ChapterSelectDialog extends AppCompatDialogFragment
    implements
    AdapterView.OnItemClickListener {

    private SearchView cityNameSearchView;
    private FilterListView listView;
    private int clickedItemIndex;
    private String mValue;
    private boolean mValueSet;
    private String[] mEntries;
    private String[] mEntryValues;
    private Listener listener;

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
        cityNameSearchView = (SearchView) view.findViewById(R.id.filter);
        listView = (FilterListView) view.findViewById(android.R.id.list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        clickedItemIndex = findIndexOfValue(mValue);
        listView.setOnItemClickListener(this);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final CheckedItemAdapter adapter = new CheckedItemAdapter(
            getContext(), android.R.layout.simple_list_item_single_choice,
            android.R.id.text1, getEntries()
        );
        listView.setAdapter(adapter);
        clickedItemIndex = findIndexOfValue(getPersistedString(null));
        listView.setSelection(clickedItemIndex);
        listView.setItemChecked(clickedItemIndex, true);
        final Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                int index = findIndexByLabelInFilteredListView(getPersistedString(null));
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (clickedItemIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[clickedItemIndex];
            listView.setItemChecked(clickedItemIndex, true);
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
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

    public String[] getEntries() {
        return mEntries;
    }

    public void setEntries(String[] entries) {
        mEntries = entries;
    }

    public String[] getEntryValues() {
        return mEntryValues;
    }

    public void setEntryValues(String[] entryValues) {
        mEntryValues = entryValues;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mValue, value);
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }
    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findIndexByLabelInFilteredListView(String value) {
        if (value != null && listView != null) {
            Adapter adapter = listView.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                String label = (String) adapter.getItem(i);
                int index = findIndexByLabel(label);
                if (mEntryValues[index].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findIndexByLabel(String value) {
        if (value != null && mEntries != null) {
            for (int i = mEntries.length - 1; i >= 0; i--) {
                if (mEntries[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getDialog().isShowing()) {
            clickedItemIndex = findIndexByLabel((String) parent.getItemAtPosition(position));
            LocationListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
            getDialog().dismiss();
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                                  CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    interface Listener {
        void onChapterSelected(String chapterId);

        Listener EMPTY = new Listener() {
            @Override
            public void onChapterSelected(String chapterId) {
                // no-op
            }
        };
    }
}
