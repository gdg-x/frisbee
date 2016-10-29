package org.gdg.frisbee.android.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.PlusApi;
import org.gdg.frisbee.android.api.model.plus.Cover;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.view.CircleTransform;
import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;

class DrawerHeaderDisplayer {

    private final PlusApi plusApi;
    private final ModelCache modelCache;
    private final Picasso picasso;

    @BindView(R.id.navdrawer_image) ImageView mDrawerImage;
    @BindView(R.id.navdrawer_user_picture) ImageView mDrawerUserPicture;
    @BindView(R.id.navdrawer_user_name) TextView mDrawerUserName;

    private String mStoredHomeChapterId;

    DrawerHeaderDisplayer(View headerView, View.OnClickListener onClickListener) {
        ButterKnife.bind(this, headerView);
        headerView.setOnClickListener(onClickListener);
        App app = App.from(headerView.getContext());
        plusApi = app.getPlusApi();
        modelCache = app.getModelCache();
        picasso = app.getPicasso();
    }

    void updateUserDetails(@Nullable GoogleSignInAccount account) {
        if (account == null) {
            mDrawerUserPicture.setImageDrawable(null);
            mDrawerUserName.setText(R.string.login_register);
            return;
        }
        if (account.getPhotoUrl() != null) {
            picasso.load(account.getPhotoUrl())
                .transform(new CircleTransform())
                .into(mDrawerUserPicture);
        }
        String displayName = account.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            mDrawerUserName.setText(displayName);
        }
    }

    void maybeUpdateChapterImage(String newHomeChapterId) {
        if (isHomeChapterOutdated(newHomeChapterId)) {
            updateChapterImage(newHomeChapterId);
        }
    }

    private boolean isHomeChapterOutdated(final String currentHomeChapterId) {
        return currentHomeChapterId != null
            && (mStoredHomeChapterId == null || !mStoredHomeChapterId.equals(currentHomeChapterId));
    }

    private void updateChapterImage(final String homeChapterId) {
        modelCache.getAsync(ModelCache.KEY_PERSON + homeChapterId,
            true, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object person) {
                    mStoredHomeChapterId = homeChapterId;
                    updateChapterCover(((Person) person).getCover());
                }

                @Override
                public void onNotFound(final String key) {
                    plusApi.getPerson(homeChapterId).enqueue(new Callback<Person>() {
                        @Override
                        public void onSuccess(Person person) {
                            if (person != null) {
                                modelCache.putAsync(key, person,
                                    DateTime.now().plusDays(1), null);
                                mStoredHomeChapterId = homeChapterId;
                                updateChapterCover(person.getCover());
                            }
                        }
                    });
                }
            });
    }

    private void updateChapterCover(@Nullable Cover cover) {
        if (cover != null) {
            picasso.load(cover.getCoverPhoto().getUrl())
                .into(mDrawerImage);
        }
    }
}
