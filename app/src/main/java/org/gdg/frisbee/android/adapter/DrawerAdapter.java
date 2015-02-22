package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.special.SpecialEvents;

public class DrawerAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<DrawerItem> mItems;

    public DrawerAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        initAdapter();
    }

    private void initAdapter() {
        mItems = new ArrayList<DrawerItem>() {{
            add(new DrawerItem(Const.DRAWER_HOME, R.drawable.drw_ic_home_gdg, R.string.home_gdg));
            add(new DrawerItem(Const.DRAWER_GDE, R.drawable.drw_ic_gde, R.string.gde));
            add(new DrawerItem(Const.DRAWER_PULSE, R.drawable.drw_ic_pulse, R.string.pulse));
            SpecialEvents specialEvents = SpecialEvents.getCurrent();
            add(new DrawerItem(Const.DRAWER_SPECIAL, specialEvents.getDrawerIconResId(), specialEvents.getTitleResId()));
            add(new DrawerItem(Const.DRAWER_ACHIEVEMENTS, R.drawable.drw_ic_achievements, R.string.achievements));
            add(new DrawerItem(Const.DRAWER_ARROW, R.drawable.drw_ic_arrow, R.string.arrow));
        }};
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
        if(view == null)
            view = mInflater.inflate(R.layout.list_drawer_item, null);

        ImageView mIcon = (ImageView)view.findViewById(R.id.icon);
        TextView mTitle = (TextView)view.findViewById(R.id.title);

        DrawerItem item = (DrawerItem) getItem(i);
        mIcon.setImageResource(item.getIcon());
        mTitle.setText(item.getTitle());

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

        public void setIcon(int mIcon) {
            this.mIcon = mIcon;
        }

        public int getTitle() {
            return mTitle;
        }

        public void setTitle(int mTitle) {
            this.mTitle = mTitle;
        }
    }
}
