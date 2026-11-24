package com.allhack.cheats;

import android.content.Context;

public class NativeBridge {

    // Carrega a sua biblioteca C++
    static {
        System.loadLibrary("ffhook");
    }

    public interface BoolCallback { void onResult(boolean result); }

    public static void setEspLine(boolean e) { run("ff_esp_line", e); }
    public static void setEspPlayer(boolean e) { run("ff_esp_player", e); }
    public static void setEspHealth(boolean e) { run("ff_esp_health", e); }
    public static void setEspSkeleton(boolean e) { run("ff_esp_skeleton", e); }
    public static void setEspBox(boolean e) { run("ff_esp_box", e); }
    public static void setAimCollider(boolean e) { run("ff_aim_collider", e); }
    public static void setAimSilent(boolean e) { run("ff_aim_silent", e); }

    public static void setEspColor(int colorIndex) {
        runCommand("echo " + colorIndex + " > /data/local/tmp/ff_esp_color");
    }

    private static void run(final String key, final boolean val) {
        runCommand("echo " + (val ? "1" : "0") + " > /data/local/tmp/" + key);
    }

    private static void runCommand(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec("su");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes(cmd + "\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // ==========================================
    // FUNÇÕES NATIVAS (C++)
    // ==========================================
    
    public static native void injectCheat(String processName, String soPath);
    public static native void updateESP(int width, int height);

    public static void injectStealth(Context context) {
        String libPath = context.getApplicationInfo().nativeLibraryDir + "/libffhook.so";
        String hiddenPath = "/data/local/tmp/.sys_ollie"; 
        
        runCommand("cp " + libPath + " " + hiddenPath);
        runCommand("chmod 777 " + hiddenPath);
        injectCheat("com.dts.freefiremax", hiddenPath);
        runCommand("rm -f " + hiddenPath);
    }
}
