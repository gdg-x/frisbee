package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {

    private final Context mContext;
    private ArrayList<DrawerItem> mItems;

    public DrawerAdapter(Context context) {
        mContext = context;
        initAdapter();
    }

    private void initAdapter() {
        mItems = new ArrayList<DrawerItem>() {
            {
                add(new DrawerItem(Const.DRAWER_HOME, R.drawable.ic_drawer_home_gdg, R.string.home_gdg));
                add(new DrawerItem(Const.DRAWER_GDE, R.drawable.ic_drawer_gde, R.string.gde));
                add(new DrawerItem(Const.DRAWER_PULSE, R.drawable.ic_drawer_pulse, R.string.pulse));
    
                final ArrayList<TaggedEventSeries> currentEventSeries =
                        App.getInstance().currentTaggedEventSeries();
                for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
                    add(new DrawerItem(Const.DRAWER_SPECIAL,
                            taggedEventSeries.getDrawerIconResId(),
                            taggedEventSeries.getTitleResId()));
                }
                add(new DrawerItem(Const.DRAWER_ACHIEVEMENTS, R.drawable.ic_drawer_achievements, R.string.achievements));
                add(new DrawerItem(Const.DRAWER_ARROW, R.drawable.ic_drawer_arrow, R.string.arrow));
                add(new DrawerItem(Const.DRAWER_SETTINGS, R.drawable.ic_drawer_settings, R.string.settings));
                add(new DrawerItem(Const.DRAWER_HELP, R.drawable.ic_drawer_help, R.string.help));
                add(new DrawerItem(Const.DRAWER_FEEDBACK, android.R.drawable.ic_dialog_email, R.string.feedback));
                add(new DrawerItem(Const.DRAWER_ABOUT, R.drawable.ic_drawer_about, R.string.about));
            }
        };
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_drawer_item, viewGroup, false);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);

        DrawerItem item = (DrawerItem) getItem(i);
        icon.setImageResource(item.getIcon());
        title.setText(item.getTitle());

        return view;
    }

    public class DrawerItem {
        private int mId, mIcon, mTitle;

        public DrawerItem(int id, int icon, int title) {
            mId = id;
            mIcon = icon;
            mTitle = title;
        }

        public int getId() {
            return mId;
        }

        public int getIcon() {
            return mIcon;
        }

        public void setIcon(int icon) {
            mIcon = icon;
        }

        public int getTitle() {
            return mTitle;
        }

        public void setTitle(int title) {
            mTitle = title;
        }
    }
}
