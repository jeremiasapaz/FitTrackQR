package com.example.fittrackapp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * Shared styling for programmatic table cells so rows match the app theme and stay readable.
 */
public final class TableUiHelper {

    private TableUiHelper() {
    }

    /**
     * @param dataRowIndex used for zebra striping when {@code isHeader} is false; ignored for headers
     */
    public static TextView createTableCell(
            Context context,
            String text,
            boolean isHeader,
            int dataRowIndex
    ) {
        TextView textView = new TextView(context);
        textView.setText(text == null ? "" : text);
        textView.setPadding(
                dp(context, 18),
                dp(context, 12),
                dp(context, 18),
                dp(context, 12)
        );
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);

        if (isHeader) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.fit_table_header_bg));
            textView.setTextColor(ContextCompat.getColor(context, R.color.fit_table_header_text));
            textView.setTypeface(null, Typeface.BOLD);
        } else {
            int bgRes = (dataRowIndex % 2 == 0)
                    ? R.color.fit_table_row_even
                    : R.color.fit_table_row_odd;
            textView.setBackgroundColor(ContextCompat.getColor(context, bgRes));
            textView.setTextColor(ContextCompat.getColor(context, R.color.fit_table_cell_text));
        }
        return textView;
    }

    /**
     * Compact capsule-shaped buttons used inside {@link android.widget.TableLayout} rows.
     */
    public static void applyCompactPillButtonStyle(Context context, Button button) {
        button.setBackgroundResource(R.drawable.bg_table_pill_button);
        button.setBackgroundTintList(null);
        button.setAllCaps(false);
        button.setIncludeFontPadding(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        button.setTextColor(ContextCompat.getColor(context, R.color.fit_on_primary_container));
        button.setMinimumWidth(0);
        button.setMinimumHeight(dp(context, 28));
        button.setMinHeight(dp(context, 28));
        int padH = dp(context, 12);
        int padV = dp(context, 5);
        button.setPadding(padH, padV, padH, padV);
    }

    /** Keeps table action buttons from stretching to fill the column width. */
    public static TableRow.LayoutParams compactTableButtonParams() {
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER_VERTICAL;
        return lp;
    }

    private static int dp(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
