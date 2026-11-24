name: Build and Sign APK

on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Extract Zips
      run: |
        if [ -f res.zip ]; then unzip -q res.zip; fi
        if [ -f libs.zip ]; then unzip -q libs.zip; fi

    - name: Compile Java to DEX
      run: |
        BT_DIR=$(ls -d $ANDROID_HOME/build-tools/* | tail -1)
        mkdir -p build/classes build/dex
        javac -source 8 -target 8 -classpath $ANDROID_HOME/platforms/android-34/android.jar -d build/classes $(find src -name "*.java")
        $BT_DIR/d8 --lib $ANDROID_HOME/platforms/android-34/android.jar --output build/dex $(find build/classes -name "*.class")

    - name: Compile Resources and Package APK
      run: |
        BT_DIR=$(ls -d $ANDROID_HOME/build-tools/* | tail -1)
        $BT_DIR/aapt2 compile --dir res -o build/compiled_res.zip
        $BT_DIR/aapt2 link -o build/base.apk -I $ANDROID_HOME/platforms/android-34/android.jar --manifest AndroidManifest.xml build/compiled_res.zip
        
        cd build
        zip -uj base.apk dex/classes.dex
        
        NDK_LIBCPP=$(find $ANDROID_HOME/ndk -name "libc++_shared.so" -path "*aarch64-linux-android*" | head -n 1)
        
        mkdir -p lib/arm64-v8a
        cp ../libs/arm64-v8a/libffhook.so lib/arm64-v8a/
        if [ -f "$NDK_LIBCPP" ]; then
            cp "$NDK_LIBCPP" lib/arm64-v8a/
        fi
        
        zip -ur base.apk lib
        cd ..

    - name: Zipalign and Sign APK
      run: |
        BT_DIR=$(ls -d $ANDROID_HOME/build-tools/* | tail -1)
        $BT_DIR/zipalign -v -p 4 build/base.apk build/aligned.apk
        keytool -genkeypair -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000 -storepass 123456 -keypass 123456 -dname "CN=Ollie, OU=Cheat, O=AllHack, L=SP, S=SP, C=BR"
        $BT_DIR/apksigner sign --ks my-release-key.keystore --ks-pass pass:123456 --key-pass pass:123456 --out build/app-release.apk build/aligned.apk

    - name: Upload APK Artifact
      uses: actions/upload-artifact@v4
      with:
        name: FFcheat-APK
        path: build/app-release.apk
