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

        // Verifica se tem permissão pra desenhar por cima dos outros apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "DÊ PERMISSÃO DE SOBREPOSIÇÃO!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 123);
                return; // Para aqui e espera o usuário dar a permissão
            }
        }

        // Se já tem permissão, inicia o serviço e o jogo
        IniciarTudo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    IniciarTudo(); // Permissão concedida, inicia
                } else {
                    Toast.makeText(this, "SEM PERMISSÃO, O MENU NÃO VAI ABRIR!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void IniciarTudo() {
        // 1. Inicia o painel flutuante
        startService(new Intent(this, FloatingService.class));

        // 2. Tenta abrir o Free Fire Max
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
        
        if (launchIntent != null) {
            Toast.makeText(this, "ABRINDO FREE FIRE MAX...", Toast.LENGTH_SHORT).show();
            startActivity(launchIntent);
        } else {
            // Se não achou o FF Max, tenta o FF normal (com.dts.freefireth)
            Intent ffNormal = getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
            if (ffNormal != null) {
                Toast.makeText(this, "ABRINDO FREE FIRE NORMAL...", Toast.LENGTH_SHORT).show();
                startActivity(ffNormal);
            } else {
                Toast.makeText(this, "FREE FIRE NÃO INSTALADO!", Toast.LENGTH_LONG).show();
            }
        }

        // Fecha o app (vai ficar só o jogo e o painel flutuante)
        finish();
    }
}
