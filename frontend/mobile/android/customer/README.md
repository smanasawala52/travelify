# Travelify Customer (Android)

Kotlin placeholder for the Customer role app.

## Intended build

```bash
./gradlew :customer:assembleDebug
adb install -r customer/build/outputs/apk/debug/customer-debug.apk
```

Point `ApiClient.DEFAULT_BASE_URL` at your machine (`10.0.2.2` for emulator).