# Android Location Manager Changelog
---
## v3.0.0
The Naurt Android SDK has undergone a restructuring. This repository contains the Naurt's Android Location Manager SDK, formerly known as "naurt-android-sdk". This is due to a new, lightweight SDK (Naurt Lite) for Android being released.

This Location Manager SDK provides automated POI creation and live location tracking and enhancement. If the tracking is not required, we recommend you use the Naurt Lite SDK.

If you're migrating from the "naurt-android-sdk", the integration process has largely remained the same with the excpetion of the location listener and class names. For more information please visit our [documentation](https://docs.naurt.net).

### New Features
- Automatic logging of building entrances and parking spots.
- Wrapper for Naurt's [POI API](https://docs.naurt.net/poi-api).
- Reduced dependencies for smaller build sizes.