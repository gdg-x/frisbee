package org.gdg.frisbee.android.arrow;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.BitmapBorderTransformation;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {

    // SparseArray is recommended by most Android Developer Advocates as it has reduced memory usage
    private SparseArrayCompat<Organizer> organizers;
    private Context context;

    public OrganizerAdapter(Context context) {
        organizers = new SparseArrayCompat<>();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_tagged_organizer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Organizer organizer = organizers.get(position);

        App.getInstance().getPicasso()
                .load(organizer.getResolved().getImage().getUrl())
                .transform(new BitmapBorderTransformation(0,
                        context.getResources().getDimensionPixelSize(R.dimen.list_item_avatar) / 2,
                        context.getResources().getColor(R.color.white)))
                .into(holder.avatar);

        holder.name.setText(organizer.getResolved().getDisplayName());
        holder.chapter.setText(organizer.getChapterName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(Utils.createExternalIntent(context,
                    PlusUtils.createProfileUrl(organizer.getPlusId())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    public void add(Organizer organizer) {
        organizers.append(organizers.size(), organizer);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar)
        ImageView avatar;

        @Bind(R.id.organizerName)
        TextView name;

        @Bind(R.id.organizerChapter)
        TextView chapter;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
