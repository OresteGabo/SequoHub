# Live scoped Hub operations

SequoHub has a live authenticated workspace and a separate labelled sample-data demo. Real sessions never enter the fake repository. This work does not change the six previously edited files (README, STORAGE_PENALTIES, HubRepository, SequoStrings, HubDashboard and LegalScreen).

The live workspace reads the operator’s actual assigned relay points, real logical slots, persisted parcels/returns and custody history. It supports manual numeric or QR-value pickup with explicit identity confirmation, one-time server release, problem reports, return ID/PIN intake, own notification inbox actions, opening hours and dated exceptions, server-saved appearance/language preferences, logical maintenance, sign-out and an account-deletion **request**. Deletion requests await review; submitting one does not erase an account or its operational records. No identity image is collected by default.

All operational data and scope come from authenticated APIs. Failed reads clear the displayed snapshot; they do not create sample lockers. Pending uncertain writes retain an identical in-memory retry intent, and no local success is assumed. Leaving the workspace discards that retry; read current state before a new action. Session generation and repository identity barriers reject in-flight results after logout/account changes. Access expiry uses normal device-bound refresh; revoked sessions fail closed.

Camera barcode scanning, physical door control, batch collection manifests and push SDK device registration are not implemented here. Manual QR/PIN and individual persisted operations use the normal server endpoints. Notification messages are scoped to the signed-in account. Monetary assessments and refund/provider decisions remain in authorized SEQUO operations; Hub does not confirm payments.

## Local acceptance build

Cached tooling is required for the offline helper. Android minSDK 24, compile/target SDK 36 and iOS deployment target 18.2 are the existing project settings. Build identifiers and origins are explicit and shared by all clients:

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' python3 scripts/build-acceptance.py --platform android --api-base-url http://10.0.2.2:55840 --release-id sequo-native-review-20261108 --environment LOCAL_FIXTURE --output-dir /tmp/sequo-native-review/hub-android
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' python3 scripts/build-acceptance.py --platform ios --api-base-url http://127.0.0.1:55840 --release-id sequo-native-review-20261108 --environment LOCAL_FIXTURE --output-dir /tmp/sequo-native-review/hub-ios
```

The helper builds only; it never installs/launches apps. Android uses a distinct `.fixture` application ID and cleartext is permitted only in a debug LOCAL_FIXTURE build. By default iOS emits an unsigned simulator app with a derived local-only Info.plist; this mode is for launch checks and does not establish that protected Keychain sessions work. Ordinary app configuration is not broadened. Nonlocal staging must use HTTPS. All three Gradle properties (`sequoApiBaseUrl`, `sequoReleaseId`, `sequoEnvironment`) must be supplied together. Defaults retain the normal production origin and never enable fixture login.

For protected simulator sign-in checks, add `--development-signing-reference /absolute/path/to/private-development-signing.json` to the iOS command. This opt-in accepts only a private reference to a validated, installed Apple Development certificate and its actual team, then uses normal Automatic Xcode development signing. It does not create an identity or a custom entitlements file. The reference stays outside source control. The helper records the complete app payload hash and debug dylib hash, because the debug executable alone does not identify the running Kotlin code. An Apple signing account/certificate remains a local setup dependency; no signing credentials are included in this repository.

Local fixture account buttons appear only in an explicit LOCAL_FIXTURE build after a positive capability response from the independently guarded local backend. They use the ordinary server-issued session response and normal role/site authorization, never a demo token or production impersonation path. No account credentials are embedded in source.

Host regression command:

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew --offline :shared:testAndroidHostTest :androidApp:assembleDebug -PsequoApiBaseUrl=http://10.0.2.2:55840 -PsequoReleaseId=sequo-native-review-20261108 -PsequoEnvironment=LOCAL_FIXTURE
```

The iOS session store uses device-only Keychain storage with checked Security results. Supported legacy serialized UserDefaults tokens migrate only after confirmed Keychain write/readback. There is no plaintext fallback on failure. Android backup/data-transfer excludes the device-key-encrypted auth preference without changing current sessions.

Native UI, camera/provider OAuth, older-device PDF export and push/hardware execution must be verified separately. Existing legal PDF platform limitations are not represented as successful exports. New live operational copy is English/French; existing Éwé/Mina preference is retained, with English fallback for new operational text.
