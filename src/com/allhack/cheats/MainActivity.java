package com.allhack.cheats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "PERMISSÃO DE SOBREPOSIÇÃO!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 123);
                return;
            }
        }
        IniciarTudo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    IniciarTudo();
                } else {
                    finish();
                }
            }
        }
    }

    private void IniciarTudo() {
        startService(new Intent(this, FloatingService.class));
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Intent ffNormal = getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
            if (ffNormal != null) {
                startActivity(ffNormal);
            }
        }
        finish();
    }
}
