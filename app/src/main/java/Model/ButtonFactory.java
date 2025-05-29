package Model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ButtonFactory {

    public static TextView createDefaultButton(Context ctx) {
        TextView btn = new TextView(ctx);

        btn.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);
        btn.setFocusable(true);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(28);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(getDefaultButtonDrawable());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        int margin = dpToPx(ctx, 4);
        params.setMargins(margin, margin, margin, margin);
        btn.setLayoutParams(params);

        return btn;
    }

    public static Drawable getDefaultButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#333333"));
        drawable.setCornerRadius(32);
        drawable.setStroke(2, Color.WHITE);
        return drawable;
    }

    public static Drawable getSelectedButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.YELLOW);
        drawable.setCornerRadius(32);
        drawable.setStroke(2, Color.WHITE);
        return drawable;
    }

    public static Drawable getSwapSelectedButtonDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#444444"));
        drawable.setCornerRadius(32);
        drawable.setStroke(4, Color.YELLOW);
        return drawable;
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
