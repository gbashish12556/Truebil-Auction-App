# Truebil Auction App (Android)

Code repository for the Truebil auction android app. 

## Installation Process

1. Use Android Studio to run and create a deployable build for the app. The app's builds have 3 variants: 

    * Debug - Accesses api.oldhonk.co.in
    * DebugProd - Accesses api.truebil.com
    * Release - Accesses api.truebil.com and also creates a minified, non-debuggable & unsigned release version of the app
    
2. Create a signed apk for an unsigned apk. Details: https://developer.android.com/studio/ publish/app-signing#sign-manually

    * Step-1: Align the release unsigned APK using zipalign

        ```
        Run in terminal:
        ./zipalign -v -p 4 auction-app-release-unsigned.apk auction-app-release-unsigned-aligned.apk
        ```    
        
    * Step-2: Sign aligned apk using apksigner and the pre-saved keystore file
    
        ```
        Run in terminal:
        ./apksigner sign --ks auction-app-release-keystore --out auction-app-release-signed.apk auction-app-release-unsigned-aligned.apk
        ```
        
    * Step-3: Verify the generated signed apk
    
        ```
        Run in terminal:
        ./apksigner verify auction-app-release-signed.apk
        ```
        
3. Notes:
    * The zipalign and apksigner are provided in the same directory as the README.
    * You will have to enter the keystore password upon running the second command. 
    * Please note that alignment has to be done before signing the APK. Aligning the APK after signing it will corrupt the signatures.

