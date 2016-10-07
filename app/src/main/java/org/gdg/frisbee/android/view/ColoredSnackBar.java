package org.gdg.frisbee.android.view;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import org.gdg.frisbee.android.R;

import butterknife.ButterKnife;

public class ColoredSnackBar {

    private static final int red = 0xffff4444;
    private static final int green = 0xff99cc00;
    private static final int blue = 0xff33b5e5;
    private static final int orange = 0xffffc107;

    private ColoredSnackBar() {
    }

    private static View getSnackBarLayout(Snackbar snackbar) {
        if (snackbar != null) {
            return snackbar.getView();
        }
        return null;
    }

    private static Snackbar colorSnackBar(Snackbar snackbar, int colorId) {
        View snackBarView = getSnackBarLayout(snackbar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(colorId);
            TextView action = ButterKnife.findById(snackBarView, R.id.snackbar_action);
            action.setTextColor(ContextCompat.getColor(snackBarView.getContext(), android.R.color.white));
        }

        return snackbar;
    }

    public static Snackbar info(Snackbar snackbar) {
        return colorSnackBar(snackbar, blue);
    }

    public static Snackbar warning(Snackbar snackbar) {
        return colorSnackBar(snackbar, orange);
    }

    public static Snackbar alert(Snackbar snackbar) {
        return colorSnackBar(snackbar, red);
    }

    public static Snackbar confirm(Snackbar snackbar) {
        return colorSnackBar(snackbar, green);
    }
}
