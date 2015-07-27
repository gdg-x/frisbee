package org.gdg.frisbee.android.gde;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.squareup.picasso.Picasso;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdgPerson;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.widget.SquaredImageView;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

class PeopleAdapter extends ArrayAdapter<GdgPerson> {

    private Context mContext;

    private Plus mClient;

    private HashMap<Integer, Object> mConsumedMap;

    private Pattern mPlusPattern;

    public PeopleAdapter(Context ctx) {
        super(ctx, R.layout.gde_item);
        mContext = ctx;
        mConsumedMap = new HashMap<>();

        mClient = App.getInstance().getPlusClient();
        mPlusPattern = Pattern.compile("http[s]?:\\/\\/plus\\..*google\\.com.*(\\+[a-zA-Z] +|[0-9]{21}).*");
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(getItem(i).getUrl());
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = LayoutInflater.from(mContext).inflate(R.layout.gde_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final GdgPerson gde = getItem(i);

        holder.thumbnailView.setImageResource(R.drawable.gde_dummy);

        holder.nameView.setText(gde.getName());
        holder.countryView.setText(gde.getAddress());
        final String socialUrl = gde.getUrl();

        if (gde.getImageUrl() != null) {
            Picasso.with(mContext)
                    .load(gde.getImageUrl())
                    .into(holder.thumbnailView);
        } else if (socialUrl != null) {

            CommonAsyncTask<ViewHolder, Person> mFetchAvatarUrl = new Builder<>(ViewHolder.class, Person.class)
                    .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<ViewHolder, Person>() {
                        @Override
                        public Person doInBackground(ViewHolder... params) {

                            Matcher matcher = mPlusPattern.matcher(socialUrl);

                            Person gde = null;

                            if (matcher.matches()) {
                                String gplusId = matcher.group(1);
                                gde = GdgNavDrawerActivity.getPersonSync(mClient, gplusId);
                            } else {
                                Timber.e("Social URL mismatch" + socialUrl);
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
        }

        if (!mConsumedMap.containsKey(i)) {
            mConsumedMap.put(i, null);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            rowView.startAnimation(animation);
        }

        return rowView;
    }

    static class ViewHolder {
        @Bind(R.id.name) public TextView nameView;
        @Bind(R.id.country) public TextView countryView;
        @Bind(R.id.thumb) public SquaredImageView thumbnailView;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
