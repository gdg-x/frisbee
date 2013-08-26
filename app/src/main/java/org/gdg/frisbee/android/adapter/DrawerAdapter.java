package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import org.gdg.frisbee.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 03.07.13
 * Time: 02:50
 * To change this template use File | Settings | File Templates.
 */
public class DrawerAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<DrawerItem> mItems = new ArrayList<DrawerItem>() {{
        add(new DrawerItem(R.drawable.ic_home_gdg, R.string.home_gdg));
        add(new DrawerItem(R.drawable.drw_ic_achievements, R.string.achievements));
        add(new DrawerItem(R.drawable.drw_ic_gdl, R.string.gdl));
        add(new DrawerItem(R.drawable.drw_ic_pulse, R.string.pulse));
        add(new DrawerItem(R.drawable.drw_ic_settings, R.string.settings));
        add(new DrawerItem(R.drawable.drw_ic_about, R.string.about));
    }};

    public DrawerAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
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
        private int mIcon, mTitle;

        public DrawerItem(int icon, int title) {
            mIcon = icon;
            mTitle = title;
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
