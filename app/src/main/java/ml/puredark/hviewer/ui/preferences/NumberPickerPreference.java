package ml.puredark.hviewer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.FrameLayout.LayoutParams;

import ml.puredark.hviewer.R;

public class NumberPickerPreference extends DialogPreference {
    private static final String TAG = NumberPickerPreference.class.getSimpleName();
    private int minValue = 0;
    private int maxValue = 10;
    private String summaryPattern = "number picked: %s";
    private NumberPicker numPicker;
    private int numValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.numValue = this.minValue;
        this.init(attrs);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.numValue = this.minValue;
        this.init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        this.maxValue = a.getInt(R.styleable.NumberPickerPreference_MaxValue, this.maxValue);
        this.minValue = a.getInt(R.styleable.NumberPickerPreference_MinValue, this.minValue);
        this.summaryPattern = a.getString(R.styleable.NumberPickerPreference_android_summary);
        a.recycle();
    }

    protected View onCreateDialogView() {
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        this.numPicker = new NumberPicker(this.getContext());
        this.numPicker.setLayoutParams(layoutParams);
        this.numPicker.setMinValue(this.minValue);
        this.numPicker.setMaxValue(this.maxValue);
        FrameLayout dialogView = new FrameLayout(this.getContext());
        dialogView.addView(this.numPicker);
        return dialogView;
    }

    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        this.numPicker.setValue(this.getValue());
    }

    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            this.numPicker.getAccessibilityNodeProvider().performAction(2, AccessibilityNodeInfo.ACTION_CLEAR_FOCUS, null);
            int pickerValue = this.numPicker.getValue();
            this.updateSummary(pickerValue);
            this.setValue(pickerValue);
            Log.d(TAG, "number picked = " + pickerValue);
        }

    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, this.minValue));
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if(restorePersistedValue) {
            this.setValue(this.getPersistedInt(this.minValue));
        } else {
            this.setValue(((Integer)defaultValue).intValue());
            if(((Integer)defaultValue).intValue() > this.maxValue) {
                Log.w(TAG, "default value is bigger than maxValue!");
            } else if(((Integer)defaultValue).intValue() < this.minValue) {
                Log.w(TAG, "default value is smaller than minValue!");
            }
        }

        this.updateSummary(this.getValue());
    }

    private void setValue(int value) {
        this.numValue = value;
        this.persistInt(this.numValue);
    }

    public int getValue() {
        return this.numValue;
    }

    private String getSummaryPattern() {
        return this.summaryPattern;
    }

    private void updateSummary(int val) {
        this.setSummary(String.format(this.getSummaryPattern(), new Object[]{Integer.toString(val)}));
    }
}