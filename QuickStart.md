#Samsung ARTIK Quick Start

This document provides information on how to adapt the source code for your own application.

## Pre-requisites

1. ARTIK Cloud Application - You must have already created your own ARTIK Cloud application and note down the Client Id and Redirect URL. The application created must be using "Authorization Code with PKCE" as Authentication method.

More info at https://developer.artik.cloud/documentation/tools/web-tools.html#creating-an-application

2. Google Analytics - Get the google-services.json and put it in the directory app/

More info at https://firebase.google.com/docs/android/setup

3. Crashlytics (Optional) - If you need information about the crashes in real time, get your fabric key.
More info at https://try.crashlytics.com/


## General Design

The application uses MVC model. ARTIKCloudLibrary contains the functions that interface with ARTIK Cloud through REST API. ARTIKModuleOnboardingLibrary contains the functions that interface with ARTIK modules during onboarding.

The application provides the following features -

1) Login/SignUp ARTIK Cloud Account - LoginActivity.java, CloudAuth.java (OAuth), CloudAuthRedirectActivity.java (app redirect)
2) Device Dashboard - HomeScreenActivity.java
3) Device Control - DeviceActivity.java
4) Onboarding -
	a) QR code scan - QRCodeScanActivity.java
	b) Module Identification - PlugInModuleActivity.java
	c) Onboarding flow according to device

ARTIK 520,710,530 - Gateway Onboarding using BLE - ControlBluetoothConnection.java, WiFiListActivity.java
020,030 - Edge Node Onboarding - EdgeNodeOnBoardingManager.java, EdgeNodeOnBoardingActivity.java
05X modules - Soft Access Point onboarding - SoftAPOnboardingManager.java, SoftAPOnboarding.java

5) Cloud Rules - RulesActivity.java, NewRuleActivity.java


## Transform it to your own app

The following steps are necessary to convert this app to your own app.

* You must replace the ARTIK Cloud application (client) Id and redirect URL in the AndroidManifest.xml

* Add your assets at res/drawable

* res/layout - Change your design. ARTIK Logo, device icons etc.

* Add google-services.json for google analytics in the app/ directory
* Update crashlytics fabric key in the AndroidManifest.xml (optional)

## APK key signing
If you want to compile in Release mode, you have two options
(a) Disable the APK signing during the release build by removing the 'signingConfigs' section in gradle.properties

OR

(b) Generate your key using this guide - https://developer.android.com/studio/publish/app-signing

