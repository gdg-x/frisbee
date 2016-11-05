/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.onboarding;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.view.CircularTransformation;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class FirstStartStep2Fragment extends BaseFragment {

    private static final String KEY_INVITE = "invite";

    @BindView(R.id.invite_sender_container)
    View inviteContainer;
    @BindView(R.id.invite_sender_profile_image)
    ImageView inviteSenderImage;
    @BindView(R.id.invite_sender_message)
    TextView inviteSenderMessage;
    @BindDimen(R.dimen.navdrawer_user_picture_size)
    int profileImageSize;

    private Step2Listener listener = Step2Listener.EMPTY;
    private Invite invite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, R.layout.fragment_welcome_step2, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            invite = savedInstanceState.getParcelable(KEY_INVITE);
            if (invite != null) {
                loadInvite(invite);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_INVITE, invite);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Step2Listener) {
            listener = (Step2Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = Step2Listener.EMPTY;
    }

    @OnClick(R.id.sign_in_button)
    public void onSignedIn() {
        listener.onSignedIn();
    }

    @OnClick(R.id.skipSignin)
    public void onSkippedSignIn() {
        listener.onSkippedSignIn();
    }

    public void loadInvite(Invite inviteSender) {
        this.invite = inviteSender;
        if (!isContextValid()) {
            return;
        }

        if (TextUtils.isEmpty(invite.sender)) {
            displayUnknownSender();
            return;
        }

        App.from(getContext()).getPlusApi()
            .getPerson(inviteSender.sender)
            .enqueue(new Callback<Person>() {
                @Override
                public void onSuccess(Person sender) {
                    if (isContextValid()) {
                        displaySender(sender);
                    }
                }

                @Override
                public void onError() {
                    if (isContextValid()) {
                        displayUnknownSender();
                    }
                }

                @Override
                public void onNetworkFailure(Throwable error) {
                    if (isContextValid()) {
                        displayUnknownSender();
                    }
                }
            });
    }

    private void displayUnknownSender() {
        inviteContainer.setVisibility(View.VISIBLE);
        updateSenderName(getString(R.string.friend));
        inviteSenderImage.setImageResource(R.drawable.ic_no_avatar);
    }

    private void displaySender(Person sender) {
        inviteContainer.setVisibility(View.VISIBLE);
        updateSenderName(sender.getDisplayName());

        if (sender.getImage() != null && sender.getImage().getUrl() != null) {
            App.from(getContext()).getPicasso()
                .load(sender.getImage().getUrl())
                .transform(CircularTransformation.createWithBorder(getContext()))
                .into(inviteSenderImage);
        }
    }

    private void updateSenderName(String senderName) {
        inviteSenderMessage.setText(getString(R.string.invite_congrats, senderName));
    }

    public interface Step2Listener {
        void onSignedIn();

        void onSkippedSignIn();

        Step2Listener EMPTY = new Step2Listener() {
            @Override
            public void onSignedIn() {
            }

            @Override
            public void onSkippedSignIn() {
            }
        };
    }
}
