package com.example.cheattyper;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class CheatTyperService extends AccessibilityService {

    private WindowManager wm;
    private LinearLayout panel;
    private EditText input;
    private AccessibilityNodeInfo lastField;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildPanel();
    }

    private void buildPanel() {
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setBackgroundColor(0xDD222222);
        panel.setPadding(16, 16, 16, 16);

        input = new EditText(this);
        input.setHint("Введи текст");
        input.setSingleLine(true);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFFAAAAAA);
        panel.addView(input, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button done = new Button(this);
        done.setText("Готово");
        done.setOnClickListener(v -> typeText(input.getText().toString()));
        panel.addView(done);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
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
        if (src == null) return;
        // запоминаем последнее редактируемое поле не из нашей панели
        if (src.isEditable() && !getPackageName().equals(String.valueOf(src.getPackageName()))) {
            lastField = src;
        }
    }

    private void typeText(String text) {
        AccessibilityNodeInfo target = findTarget();
        if (target == null) {
            Toast.makeText(this, "Сначала тапни поле ввода в игре", Toast.LENGTH_SHORT).show();
            return;
        }
        target.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        Bundle args = new Bundle();
        args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private AccessibilityNodeInfo findTarget() {
        if (lastField != null && lastField.refresh()) return lastField;

        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return null;
        for (AccessibilityWindowInfo w : windows) {
            if (w.getType() == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) continue;
            AccessibilityNodeInfo root = w.getRoot();
            if (root == null) continue;
            AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (focused != null && focused.isEditable()) return focused;
        }
        return null;
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        if (panel != null && wm
