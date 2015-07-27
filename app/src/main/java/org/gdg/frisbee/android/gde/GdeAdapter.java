package org.gdg.frisbee.android.gde;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.squareup.picasso.Picasso;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.widget.SquaredImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

class GdeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<Gde> mGdes;

    private Plus mClient;

    private HashMap<Integer, Object> mConsumedMap;

    private Pattern mPlusPattern;

    public GdeAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mGdes = new ArrayList<>();
        mConsumedMap = new HashMap<>();

        mClient = App.getInstance().getPlusClient();
        mPlusPattern = Pattern.compile("http[s]?:\\/\\/plus\\..*google\\.com.*(\\+[a-zA-Z] +|[0-9]{21}).*");
    }

    public void addAll(ArrayList<? extends Gde> list) {
        mGdes.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(mGdes.get(i).getEmail());
    }

    @Override
    public Object getItem(int i) {
        return mGdes.get(i);
    }

    @Override
    public int getCount() {
        return mGdes.size();
    }


    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.gde_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnailView = (SquaredImageView) rowView.findViewById(R.id.thumb);
            viewHolder.nameView = (TextView) rowView.findViewById(R.id.name);
            viewHolder.countryView = (TextView) rowView.findViewById(R.id.country);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final Gde gde = (Gde) getItem(i);

        holder.thumbnailView.setImageResource(R.drawable.gde_dummy);

        holder.nameView.setText(gde.getName());
        holder.countryView.setText(gde.getAddress());
        holder.socialUrl = gde.getSocialUrl();

        CommonAsyncTask<ViewHolder, Person> mFetchAvatarUrl = new Builder<>(ViewHolder.class, Person.class)
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<ViewHolder, Person>() {
                    @Override
                    public Person doInBackground(ViewHolder... params) {
                        if (params[0] == null || params[0].socialUrl == null) {
                            return null;
                        }
                        Matcher matcher = mPlusPattern.matcher(params[0].socialUrl);

                        Person gde = null;

                        if (matcher.matches()) {
                            String gplusId = matcher.group(1);
                            gde = GdgNavDrawerActivity.getPersonSync(mClient, gplusId);
                        } else {
                            Timber.e("Social URL mismatch" + params[0].socialUrl);
                        }

                        return gde;
                    }
                })
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<ViewHolder, Person>() {
                    @Override
                    public void onPostExecute(ViewHolder[] p, Person person) {
                        if (person != null
                                && person.getImage() != null
                                && person.getImage().getUrl() != null
                                && p[0].thumbnailView.equals(holder.thumbnailView)) {
                            Picasso.with(mContext)
                                    .load(person.getImage().getUrl().replace("sz=50", "sz=196"))
                                    .into(p[0].thumbnailView);
                        }
                    }
                }).build();

        mFetchAvatarUrl.execute(holder);

        if (!mConsumedMap.containsKey(i)) {
            mConsumedMap.put(i, null);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            rowView.startAnimation(animation);
        }

        return rowView;
    }

    static class ViewHolder {
        public TextView nameView;
        public TextView countryView;
        public SquaredImageView thumbnailView;
        public String socialUrl;
    }
}
