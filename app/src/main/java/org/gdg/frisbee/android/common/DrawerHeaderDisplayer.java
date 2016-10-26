package org.gdg.frisbee.android.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.plus.Cover;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.view.CircleTransform;
import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;

class DrawerHeaderDisplayer {
    @BindView(R.id.navdrawer_image) ImageView mDrawerImage;
    @BindView(R.id.navdrawer_user_picture) ImageView mDrawerUserPicture;
    @BindView(R.id.navdrawer_user_name) TextView mDrawerUserName;

    private String mStoredHomeChapterId;

    DrawerHeaderDisplayer(View headerView, View.OnClickListener onClickListener) {
        ButterKnife.bind(this, headerView);
        headerView.setOnClickListener(onClickListener);
    }

    void updateUserDetails(@Nullable GoogleSignInAccount account) {
        if (account == null) {
            mDrawerUserPicture.setImageDrawable(null);
            mDrawerUserName.setText(R.string.login_register);
            return;
        }
        if (account.getPhotoUrl() != null) {
            App.getInstance().getPicasso().load(account.getPhotoUrl())
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
        App.getInstance().getModelCache().getAsync(ModelCache.KEY_PERSON + homeChapterId,
            true, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object person) {
                    mStoredHomeChapterId = homeChapterId;
                    updateChapterCover(((Person) person).getCover());
                }

                @Override
                public void onNotFound(final String key) {
                    App.getInstance().getPlusApi().getPerson(homeChapterId).enqueue(new Callback<Person>() {
                        @Override
                        public void success(Person person) {
                            if (person != null) {
                                App.getInstance().getModelCache().putAsync(key, person,
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
            App.getInstance().getPicasso()
                .load(cover.getCoverPhoto().getUrl())
                .into(mDrawerImage);
        }
    }
}
