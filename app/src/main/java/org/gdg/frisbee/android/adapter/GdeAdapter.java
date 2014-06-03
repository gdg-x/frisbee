package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.squareup.picasso.Picasso;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GapiTransportChooser;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ResizableImageView;
import org.gdg.frisbee.android.view.SquaredImageView;
import timber.log.Timber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maui on 29.05.2014.
 */
public class GdeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<Gde> mGdes;

    final HttpTransport mTransport = GapiTransportChooser.newCompatibleTransport();
    final JsonFactory mJsonFactory = new GsonFactory();
    private Plus mClient;

    private HashMap<Integer,Object> mConsumedMap;
    private GoogleApiClient mPlusClient;

    private Pattern mPlusPattern;

    public GdeAdapter(Context ctx, GoogleApiClient client) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mGdes = new ArrayList<Gde>();
        mPlusClient = client;
        mConsumedMap = new HashMap<Integer, Object>();

        mClient = new Plus.Builder(mTransport, mJsonFactory, null).setGoogleClientRequestInitializer(new CommonGoogleJsonClientRequestInitializer(ctx.getString(R.string.ip_simple_api_access_key))).setApplicationName("GDG Frisbee").build();
        mPlusPattern = Pattern.compile("http[s]?:\\/\\/plus\\..*google\\.com.*(\\+[a-zA-Z]+|[0-9]{21}).*");
    }

    public void clear() {
        mGdes.clear();
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if(rowView == null) {
            rowView = mInflater.inflate(R.layout.gde_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnailView = (SquaredImageView) rowView.findViewById(R.id.thumb);
            viewHolder.nameView = (TextView) rowView.findViewById(R.id.name);
            viewHolder.countryView = (TextView) rowView.findViewById(R.id.country);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final Gde gde = (Gde) getItem(i);

        holder.thumbnailView.setImageResource(R.drawable.gde_dummy);
        //holder.thumbnailView.setBackgroundResource(R.drawable.gde_dummy);

        /*App.getInstance().getPicasso()
                .load(show.getHighQualityThumbnail())
                .into(holder.thumbnailView);*/

        holder.nameView.setText(gde.getName());
        holder.countryView.setText(gde.getAddress());
        holder.socialUrl = gde.getSocialUrl();

        CommonAsyncTask<ViewHolder, Person> mFetchAvatarUrl = new Builder<ViewHolder, Person>(ViewHolder.class, Person.class)
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<ViewHolder, Person>() {
                    @Override
                    public Person doInBackground(ViewHolder... params) {
                        Matcher matcher = mPlusPattern.matcher(params[0].socialUrl);

                        Person gde = null;

                        if(matcher.matches()) {
                            String plusId = matcher.group(1);
                            gde = (Person) App.getInstance().getModelCache().get("gde_" + plusId, !Utils.isOnline(mContext));

                            if (gde == null && Utils.isOnline(mContext)) {
                                try {
                                    Plus.People.Get request = mClient.people().get(plusId);
                                    request.setFields("image");
                                    gde = request.execute();
                                    App.getInstance().getModelCache().put("gde_" + plusId, gde);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Timber.e(params[0].socialUrl);
                        }

                        return gde;
                    }
                })
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<ViewHolder, Person>() {
                    @Override
                    public void onPostExecute(ViewHolder[] p, Person person) {
                        if(person != null && p[0].thumbnailView.equals(holder.thumbnailView))
                            Picasso.with(mContext).load(person.getImage().getUrl().replace("sz=50", "sz=196")).into(p[0].thumbnailView);
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
