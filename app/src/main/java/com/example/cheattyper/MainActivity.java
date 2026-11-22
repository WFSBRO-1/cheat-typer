package com.example.cheattyper;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isServiceEnabled()) {
            Toast.makeText(this, "Включи Cheat Typer в спец. возможностях", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } else {
            Toast.makeText(this, "Панель активна. Открой игру и тапни поле ввода", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isServiceEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String me = getPackageName() + "/" + CheatTyperService.class.getName();
        return enabled != null && enabled.contains(me);
    }
}
