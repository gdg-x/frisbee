package org.gdg.frisbee.android.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;

import org.gdg.frisbee.android.R;

public class LocationListPreference extends DialogPreference implements AdapterView.OnItemClickListener {

    private FilterListView listView;
    private int clickedItemIndex;
    private String mValue;
    private boolean mValueSet;
    private String[] mEntries;
    private String[] mEntryValues;

    public LocationListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.view_location_list_preference);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        listView = (FilterListView) view.findViewById(android.R.id.list);
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

        SearchView cityNameSearchView = (SearchView) view.findViewById(R.id.filter);
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
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && clickedItemIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[clickedItemIndex];
            listView.setItemChecked(clickedItemIndex, true);
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    public void setEntries(String[] entries) {
        mEntries = entries;
    }

    public String[] getEntries() {
        return mEntries;
    }

    public void setEntryValues(String[] entryValues) {
        mEntryValues = entryValues;
    }

    public String[] getEntryValues() {
        return mEntryValues;
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

    public int findIndexByLabelInFilteredListView(String value) {
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

    public int findIndexByLabel(String value) {
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
    public CharSequence getSummary() {
        String persistedString = getPersistedString(null);
        if (persistedString == null) {
            return super.getSummary();
        } else {
            int index = findIndexOfValue(persistedString);
            if (index >= 0) {
                return mEntries[index];
            } else {
                return super.getSummary();
            }
        }
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        myState.entries = getEntries();
        myState.entryValues = getEntryValues();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        setEntries(myState.entries);
        setEntryValues(myState.entryValues);
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
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

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class SavedState extends BaseSavedState {
        String value;
        String[] entries;
        String[] entryValues;

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
            entries = source.createStringArray();
            entryValues = source.createStringArray();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
            dest.writeStringArray(entries);
            dest.writeStringArray(entryValues);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
            new Parcelable.Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
    }
}
