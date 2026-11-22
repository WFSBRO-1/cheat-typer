package com.example.cheattyper;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class CheatTyperService extends AccessibilityService {

    private WindowManager wm;
    private LinearLayout panel;
    private EditText input;
    private AccessibilityNodeInfo lastField;

    @Override
    protected void onServiceConnected() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        panel = new LinearLayout(this);
        panel.setBackgroundColor(0xDD222222);
        panel.setPadding(16, 16, 16, 16);

        input = new EditText(this);
        input.setHint("Введи текст");
        input.setSingleLine(true);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFFAAAAAA);
        panel.addView(input, new LinearLayout.LayoutParams(0, -2, 1f));

        Button done = new Button(this);
        done.setText("Готово");
        done.setOnClickListener(v -> typeText(input.getText().toString()));
        panel.addView(done);

        Button close = new Button(this);
        close.setText("✕");
        close.setOnClickListener(v -> panel.setVisibility(View.GONE));
        panel.addView(close);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                -1, -2,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM;
        wm.addView(panel, lp);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_VIEW_FOCUSED) return;
        AccessibilityNodeInfo src = event.getSource();
        if (src != null && src.isEditable()
                && !getPackageName().equals(String.valueOf(src.getPackageName()))) {
            lastField = src;
            panel.setVisibility(View.VISIBLE);
        }
    }

    private void typeText(String text) {
        AccessibilityNodeInfo target = lastField;
        if (target == null || !target.refresh()) target = findFocused();
        if (target == null) return;
        target.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private AccessibilityNodeInfo findFocused() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        return root == null ? null : root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        if (panel != null && wm != null) wm.removeView(panel);
        super.onDestroy();
    }
}
