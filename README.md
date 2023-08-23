# Android Location Manager SDK

<br>

Naurt's Location Manager SDK is a development kit that integrates our location tracking technology into your Android apps.

It offers enhanced location tracking both indoors and outdoors, spoofing prevention, automatic door and parking detection, as well as easy to use wrappers for our POI API.



<br>

---
## Quickstart

<br>

Naurt is a replacement for the standard Android location listener. Though before you begin, ensure you have a valid API key. If you don't, no worries. You can sign up for a free account on our [dashboard](https://dashboard.naurt.net/).

<br>


### Project Configuration
Naurt's minimum supported API level is 16 which is Android 4.1 (Jelly Bean). Your project's build.gradle file should contain a "minSdkVersion" of 16 or above.
```groovy
android {
    defaultConfig {
        minSdkVersion 16
    }
}
```



<br>

While you're in the build.gradle file, Naurt also needs to be added as a dependency. First add mavenCentral() to your repositories 

```groovy
repositories {
    mavenCentral()
}
```
And then the Naurt Location Manager can be added as a dependency.

```groovy
dependencies {
    implementation "com.naurt.sdk:location-manager:3.0.0"
}
```

To view change logs or manually include the Naurt Location Manager in your project, visit our [Github](https://github.com/Naurt-Ltd-Public/).

<br>

### App permissions

As Naurt accesses the phone's Network and GPS location services, you'll need to add the corresponding permissions to your AndroidManifest.xml file to ensure the Location Manager works as expected. The foreground service permission is only required if you want the Naurt Location Manager to run a foreground service.

```xml
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

**You will also need the user to grant location permissions before starting Naurt.** If you do not already have this logic in your application, you can use the following example to check for the granted permission and only initialise Naurt if it has been granted.

```
private val LOCATION_PERMISSION_REQUEST_CODE = 1001

if (!this.hasLocationPermission()) {
    this.requestLocationPermission()
} else{
    // Start Naurt!
}


// Check if the app has permission to access location
private fun hasLocationPermission(): Boolean {
    val permissionStatus = ContextCompat.checkSelfPermission(
        this,
        ACCESS_FINE_LOCATION
    )
    return permissionStatus == PackageManager.PERMISSION_GRANTED
}

// Request location permission from the user
private fun requestLocationPermission() {
    ActivityCompat.requestPermissions(
        this, arrayOf(ACCESS_FINE_LOCATION),
        LOCATION_PERMISSION_REQUEST_CODE
    )
}

// Handle the result of the permission request
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Naurt", "I have the permissions!")
            // Start Naurt
        } else {
            Log.d("Naurt", "I haven't got the permissions!")
            // Don't start Naurt
        }
    }
}
```
<br>

### Instantiating Naurt

First, begin by importing the NaurtLocationManager class and the NaurtEngineType enumeration.

```kotlin
import com.naurt.sdk.NaurtLocationManager
import com.naurt.sdk.enums.NaurtEngineType
```

To start the Location Manager you'll first choose an engine type and include your API key. The engine type enables the Location Manager to run its own foreground service, using the SERVICE mode, or run within a pre-existing foreground service (or any configuration you wish) using the STANDALONE mode. This is described in detail in the [background tracking](#backgroundtracking) section.

The Location Manager also requires the application's context.

```kotlin
val naurtLocationManager = NaurtLocationManager(
    <YOUR NAURT API KEY HERE> as String,
    applicationContext as Context,
    NaurtEngineType.STANDALONE
)
```

Instantiation should be carried out within the onCreate() method of the app, though it's important the user has granted location permissions first otherwise the Location Manager will throw an exception.

<br>

### Register for location updates

Once the Location Manager has been created you can then register for location updates by creating a class that conforms to the 
[NaurtLocationListener](#naurtlocationlistener) interface and calling the [requestLocationUpdates](#requestlocationupdates) method.
```kotlin
import android.util.Log
import com.naurt.sdk.location.NaurtLocationListener

class NaurtLocationUpdates(): NaurtLocationListener {
    override fun onLocationChanged(location: NaurtLocation) {
        Log.d("Naurt", "New Naurt Location: $location")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Naurt", "Location Services Disabled: $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Naurt", "Location Services Enabled: $provider")
    }

    override fun onValidationStatusChanged(validationStatus: NaurtValidationStatus) {
        Log.d("Naurt", "Naurt validation status:  $validationStatus")
    }
}

naurtLocationManager.requestLocationUpdates(NaurtLocationUpdates())

```
By default the `onLocationChanged` method will be triggered once every second. When testing this, ensure the phone can receive Network or GPS location updates.

To remove the listener simply call [removeLocationUpdates](#removelocationupdates).

```kotlin
naurtLocationManager.removeLocationUpdates()
```


<br>


### Disabling Naurt

When you're finished using the Location manager and want to close the app, call the onDestroy method. This will clean up any internal processes and listeners created by the Location Manager. We recommend doing this within the onDestroy method of your app.

```kotlin
naurtLocationManager.onDestroy()
```
And that's all there is to getting enhanced location fixes from Naurt!

This quickstart has hopefully got you up and running with the Location Manager; however, most apps will require more than just this basic setup. So read on to learn more about how Naurt can help. 


<br>




---
## Example Application
We also have an example application which combines the above concepts into a full app.

It can be found on [our GitHub here](https://github.com/Naurt-Ltd-Public/android-location-manager-sdk).


---

## Background tracking

The Naurt Location manager can be used to provide location fixes when the app is running in the background. 

To avoid adding the background tracking permission directly in the SDK (which would mean everyone would have to add it), we use different [engine types](#naurtenginetype).

### Service type

The Location Manager can be used to create a foreground service which will automatically track in the background by using the **SERVICE** engine type. 

```kotlin
import com.naurt.sdk.NaurtLocationManager
import com.naurt.sdk.enums.NaurtEngineType

val naurtLocationManager = NaurtLocationManager(
    "<YOUR NAURT API KEY HERE>",
    applicationContext as Context,
    NaurtEngineType.SERVICE
)
```

If you run Naurt in this way, you'll have to add a service definition and corresponding permission within the manifest tags of you AndroidManifest.xml as follows.


```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application
    <service
        android:name="com.naurt.sdk.services.NaurtService"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.ACCESS_FINE_LOCATION"
        android:foregroundServiceType="location|dataSync"
        />

    <receiver
        android:name="com.naurt.sdk.services.StopNaurtBroadcastReceiver"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.ACCESS_FINE_LOCATION">
        <intent-filter>
            <action android:name="stopnaurtservice" />
        </intent-filter>
    </receiver>
</application>
```

<div class="callout-block callout-block-danger">
    <div class="content">
        <h4 class="callout-title">
            <span class="callout-icon-holder me-1">
                <i class="fas fa-info-circle"></i>
            </span>
            <!--//icon-holder-->
            Warning
        </h4>
        <p>Please do not use the SERVICE engine if your app already has a foreground service. An Android app can only contain one foreground service and will therefore crash.
        </p>
    </div>
    <!--//content-->
</div>




### Standalone type

Background tracking is also possible through the **STANDALONE** engine type. For this the background Android location permission will be needed, or you can run the Location Manager from within your own foreground service.

```kotlin
import com.naurt.sdk.NaurtLocationManager
import com.naurt.sdk.enums.NaurtEngineType

val naurtLocationManager = NaurtLocationManager(
    "<YOUR NAURT API KEY HERE>",
    applicationContext as Context,
    NaurtEngineType.STANDALONE
)
```
---

<br>

## API key validation

<br>

When the Location Manager is initialised, it will attempt to validate the API key. If a key cannot be successfully validated, Naurt's output will be replaced with Google's fused location. This ensures continuous tracking when internet is not available, a key has expired, or the key does not have enough remaining users.

The easiest way to check whether Naurt is validated yet is by using the [getValidated](#getisvalidated) method.


```kotlin
import com.naurt.sdk.enums.NaurtValidationStatus


val isMyApiKeyValidated = naurtLocationManager.getIsValidated()

when(isMyApiKeyValidated) {
    NaurtValidationStatus.Valid -> Log.d("Naurt", "API key is valid.")
    NaurtValidationStatus.ValidNoDataTransfer -> Log.d("Naurt", "API key is valid, but no data is to be uploaded.")
    NaurtValidationStatus.Invalid -> Log.d("Naurt", "API key is invalid. GPS being passed through.")
    NaurtValidationStatus.NotYetValidated -> Log.d("Naurt", "Naurt is currently attempting to validate.")
}
```

Alternatively, you can use the [NaurtLocationListener](#naurtlocationlistener) if registered directly after initialisation. 

```kotlin

import android.util.Log
import com.naurt.sdk.location.NaurtLocationListener

naurtLocationManager.requestLocationUpdates(NaurtLocationUpdates())

class NaurtLocationUpdates(): NaurtLocationListener {
    override fun onLocationChanged(location: NaurtLocation) {
        Log.d("Naurt", "New Naurt Location: $location")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Naurt", "Location Services Disabled: $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Naurt", "Location Services Enabled: $provider")
    }

    override fun onValidationStatusChanged(validationStatus: NaurtValidationStatus) {
        when(validationStatus) {
            NaurtValidationStatus.Valid -> Log.d("Naurt", "API key is valid.")
            NaurtValidationStatus.ValidNoDataTransfer -> Log.d("Naurt", "API key is valid, but no data is to be uploaded.")
            NaurtValidationStatus.Invalid -> Log.d("Naurt", "API key is invalid. GPS being passed through.")
            NaurtValidationStatus.NotYetValidated -> Log.d("Naurt", "Naurt is currently attempting to validate.")
        }
    }
}
```


<div class="callout-block callout-block-info">
    <div class="content">
        <h4 class="callout-title">
            <span class="callout-icon-holder me-1">
                <i class="fas fa-info-circle"></i>
            </span>
            <!--//icon-holder-->
            Important
        </h4>
        <p>Though users may not have internet to validate, Naurt will always provide location fixes; though, they will be unprocessed by us and not count towards your key's total users.
        </p>
    </div>
    <!--//content-->
</div>




---


<br>

## Points Of Interest

<br>

Naurt collects anonymised location data from the SDK which enables us to create automatic Points of Interest, currently in the form of building entrances and parking spots. We achieve this through the collection of data about the destination helping us link useful data, such as the current delivery address, to events that we detect on the phone, such as the user entering a building.

When you instantiate NaurtLocationManager, you have the ability to provide data about your destination as an optional **JSONObject**. If you don't have any data about the destination quite yet, that's fine. Later down the line if you wish to add data or update the current data, you can use the method [newDestination](#newDestination). This again takes an optional **JSONObject**. If it is null, this will remove any previous data and there will be no data associated with the subsequent location fixes and destination.

```kotlin
import org.json.JSONObject
import com.naurt.sdk.NaurtLocationManager
import com.naurt.sdk.enums.NaurtEngineType



val originalDestination = JSONObject() 
originalDestination.put("address", "main road")


val naurtLocationManager = NaurtLocationManager(
    "<YOUR NAURT API KEY HERE>",
    applicationContext as Context,
    destinationData = originalDestination
)


val updatedDestination = JSONObject() 
updatedDestination.put("address", "london road")


naurtLocationManager.newDestination(updatedDestination)
```


<div class="callout-block callout-block-danger">
    <div class="content">
        <h4 class="callout-title">
            <span class="callout-icon-holder me-1">
                <i class="fas fa-info-circle"></i>
            </span>
            <!--//icon-holder-->
            Important
        </h4>
        <p>Please do not send any personal information or data that directly identifies the user.
        </p>
    </div>
    <!--//content-->
</div>


Points of interest which have been created are then accessible via [Naurt's POI API](/poi-api) and can be searched via the data provided or spatially filtered. 

Naurt Lite offers an easy to use wrapper for this API which will be described in the next section, though it's still worth familiarising yourself with the [POI API documentation](/poi-api) first.

Naurt's POI system can be used in many different scenarios and increase the value Naurt brings to your company. If you're still unsure about how the POI system could play a part in your use case, contact our [sales team](https://www.naurt.com/contact-us) to have a chat.

---
## PoiInsert Class

When using Naurt's POI system, it is possible to insert custom POIs. These POIs could be used to keep track of anything; for example, a bench or a lamppost. This data can be sent directly to Naurt's [POI API](/poi-api) via a PUT request, or if you're using an Android SDK, via the PoiInsert class.

To create an instance of this class, it is required that you use its builder.

#### Import

```kotlin
import com.naurt.sdk.poi.PoiInsert
import com.naurt.sdk.poi.PoiInsert.Builder
```


#### Constructor 

The PoiInsert class cannot be directly made with it's constructor. You must build the class via it's Builder. A latitude and longitude must be provided alongside the POI's type (any alphanumerical text) and a valid Naurt key. 


```kotlin
class Builder(
    apiKey: String,
    poiType: String,
    latitude: Double,
    longitude: Double,
)
```

The first basic example demonstrates inserting a restaurant location as a POI. 


```kotlin
val poiInsert = PoiInsert.Builder(
    "<API_KEY_HERE>",
    "restaurant",
    51.0,
    51.0
).build()
```
This next example demonstrates creating a POI which will later be filterable by the type of restaurant.
```kotlin
val poiInsertWithMeta = PoiInsert.Builder(
    "<API_KEY_HERE>",
    "restaurant",
    51.0,
    51.0
).setMetadata(JSONObject(mapOf("type" to "italian"))).build()
```
And now the POI will be filterable by city.
```kotlin
val poiInsertWithDetailedMeta = PoiInsert.Builder(
    "<API_KEY_HERE>",
    "restaurant",
    51.0,
    51.0
).setMetadata(JSONObject(mapOf("type" to "italian", "city" to "London"))).build()
```

This class conforms to the [POI API specification](/poi-api). Please visit the [POI API](/poi-api) docs for a more in-depth guide.


Once built, the request to insert the POI can be carried out using the [`.send()`](#send) method.

---

### send
This method runs a web request and triggers a callback, so ensure it is correctly spawned off to avoid blocking the main thread. 

#### Signature
```kotlin
poiInsert.send(callback: PoiCallback<JSONObject>)
```

#### Parameters

- `callback`: A Naurt [PoiCallback](#poicallback_interface) which will receive the result of the web request. If successful a JSON response 
will be available. If unsuccessful a status code and a JSON containing the error will be present.



#### Returns 

None, but will trigger the callback once the request is done.


#### Throws

Does not throw.


## PoiQuery Class

Once POIs have been generated, you'll be able to query them using powerful location and information based filters.

#### Import

```kotlin
import com.naurt.sdk.poi.PoiQuery
import com.naurt.sdk.poi.PoiQuery.Builder
```


#### Constructor 

The PoiQuery class cannot be directly made with it's constructor. You must build the class via it's Builder. There are two ways to query POI data - with a valid latitude and longitude and/or with the metadata search.

```kotlin

class Builder(
    apiKey: String,
    poiTypes: List<String>,
    latitude: Double,
    longitude: Double
)

class Builder(
    apiKey: String,
    poiTypes: List<String>,
    metadata: JSONObject
)
```

When building a query based around a location, the response will return the closest POIs. When building a query around metadata, the most recently created POIs with an exact match will be returned. For more information please visit the [POI API documentation](/poi-api).
The following example would return the closest 25 restaurants to the location 10.0, 10.0, that you have inserted into the POI system.

```kotlin
val poiQuery = PoiQuery.Builder(
    "API_KEY_HERE",
    listOf("restaurant"),
    10.0,
    10.0,
).build()
```
You could then build on this by querying the closest italian restaurants. 
```kotlin
val poiQuery = PoiQuery.Builder(
    BuildConfig.API_KEY,
    listOf("restaurant"),
    JSONObject(mapOf("type" to "italian"))
).setDistanceFilter(1e10, 10.23423, 10.23123).build()
```

This class conforms to the [POI API specification](/poi-api). For more information on how to search POIs, please visit that page.


Once built, the request to query the POI can be carried out using the `.send()` method.

---

### send
This method runs a web request and triggers a callback, so ensure it is correctly spawned off to avoid blocking the main thread. 

#### Signature
```kotlin
poiInsert.send(callback: PoiCallback<JSONObject>)
```

#### Parameters

- `callback`: A Naurt [PoiCallback](#poicallback_interface) which will receive the result of the web request. If successful a JSON response 
will be available. If unsuccessful a status code and a JSON containing the error will be present.


#### Returns 

None, but will trigger the callback once the request is done.


#### Throws

Does not throw.

---


## PoiCallback Interface

The interface for creating a callback to receive Naurt POIs. 
#### Import

```kotlin
import com.naurt.sdk.poi.PoiCallback
```
#### Signature
```kotlin
interface PoiCallback<JSONObject> {
    /**
     * Callback for API response
     *
     * This function will be called when a Naurt API has responded.
     *
     */
    fun onComplete(result: NaurtResult<JSONObject>)
}
```
#### Parameters

- `result`: A Naurt [Result](#naurtresult_sealed_class) which can either be a success or a failure depending on the result of the web request. A success will contain a JSON response. A failure will contain a status code and a JSON response.

#### Example
```kotlin
import com.naurt.sdk.poi.PoiCallback
import com.naurt.sdk.enums.NaurtResult
class MyNaurtCallback() : PoiCallback<JSONObject> {
    override fun onComplete(result: NaurtResult<JSONObject>) {
        when (result) {
            is NaurtResult.Success -> {
                println("Success!: ${result.value}")
            }
            is NaurtResult.Failure -> {
                println("Oh no, an Error! Code: ${result.code}, Message: ${result.message}")
            }
        }
    }
}
```





---

## NaurtResult Sealed Class

A sealed class that contains either the JSON result of a successful request, or the error code and response of a failed request.

### Import 
```kotlin
import com.naurt.sdk.enums.NaurtResult
```
### Signature
```kotlin
sealed class NaurtResult<out T>{
    data class Success<out R>(val value: R): NaurtResult<R>()

    data class Failure(val code: Int, val message: JSONObject): NaurtResult<Nothing>()

    fun isSuccess(): Boolean{
        return when(this){
            is Success -> true
            is Failure -> false
        }
    }
}
```

#### Parameters

- `value`: A generic type that is used to return a successful result. This will usually by of type JSONObject.
- `code`: The error code of the failed request.
- `message`: The error message of the failed request.

#### Example
```kotlin
import com.naurt.sdk.poi.PoiCallback
import com.naurt.sdk.enums.NaurtResult
class MyNaurtCallback() : PoiCallback<JSONObject> {
    override fun onComplete(result: NaurtResult<JSONObject>) {
        when (result) {
            is NaurtResult.Success -> {
                println("Success!: ${result.value}")
            }
            is NaurtResult.Failure -> {
                println("Oh no, an Error! Code: ${result.code}, Message: ${result.message}")
            }
        }
    }
}
```

---
<br>

## Spoofing toolkit

<br>

### Mocked location
Users mocking, spoofing, or providing false locations can enables them to access services they wouldn't otherwise be able to.

When this occurs, the NaurtLocation object provides a simple [isMocked](#naurtlocation) property that can be used to check if the location fix is legitimate or not. For example,
```kotlin
override fun onLocationChanged(location: NaurtLocation) {
    Log.d("Naurt", "New Naurt Location: $location")
    Log.d("Naurt", "Is this location mocked? ${location.isMocked}")
}
```

Naurt also mitigates mocked locations, when the device is running API level 30 or higher, has internet, and is outside. Though the user is mocking Naurt is able to provide authentic location fixes, though at a slightly degraded accuracy. In this case, the [isMocked](#naurtlocation) will be false, but the [isMockedPrevented](#naurtlocation) property will be true. Make sure to check both booleans for an accurate gauge of how many devices are spoofing locations.

```kotlin
override fun onLocationChanged(location: NaurtLocation) {
    Log.d("Naurt", "New Naurt Location: $location")
    Log.d("Naurt", "Has location mocking been prevented?? ${location.isMockedPrevented}")
}
```

It's also worthing noting that when these events happen, Naurt will be sent a notification and can later provide reports.

### Phone state

Naurt also provides handy methods to check the phone for states that would suggest advanced spoofing.

To check whether the phone is in developer mode, which is needed for spoofing apps to work, the naurt method [isDeveloper](#isdeveloper) can be used. Similarly, the method [isRooted](#isrooted) checks to see if the user's phone is rooted. If a phone is rooted, it is possible to bypass the earlier mocking checks. For high security applications it is essential to check these flags too.

You can also use the location manager to see if your app is running in a [work profile](https://support.google.com/work/android/answer/6191949?hl=en) on the user's phone.

```kotlin
val isDeveloper = naurtLocationManager.isDeveloper()
val isRooted = naurtLocationManager.isRooted()
val inWorkMode = naurtLocationManager.inWorkProfile()
```


---


<br>

## Battery optimisation

<br>


Naurt also offers a setting to reduce battery consumption. The simple method [activateBatteryOptimisation](#activatebatteryoptimisation) enables you to activate or deactivate battery saving mode. By default this option will not be turned on and ideally, for any given initialisation period, this setting should only be changed once.

```kotlin 

// To activate 
naurtLocationManager.activateBatteryOptimisation(true)

// To deactivate 
naurtLocationManager.activateBatteryOptimisation(false)

```

By activating this setting, the SDK will turn off specific calculations in different areas. For instance, in good, open sky conditions when driving or stationary, the location manager will not provide indoor/outdoor flags as they are likely unnecessary in these regimes. This will reduce the overall battery consumption.




---

<br>

## Location intelligence

<br>

One of Naurt's key features is the additional information provided within the [NaurtLocation](#naurtlocation) object. Two of the most powerful fields are the [NaurtMovement](#naurtmovement) type and the [NaurtEnvironmentFlag](#naurtenvironmentflag) type. 

### Movement type
The movement type specifies if the user is **on foot, in a vehicle or stationary**. This aids decision making within your app and can also be used to validate the information being inputted to it. 

### Environment type
The environment type specifies whether a user is **outdoors or indoors**, though this feature is only available on phones running on API level 30 and up.

```kotlin
import com.naurt.sdk.enums.NaurtEnvironmentFlag
import com.naurt.sdk.enums.NaurtMovement

override fun onLocationChanged(location: NaurtLocation) {
        when(location.environmentFlag){
            NaurtEnvironmentFlag.Outside -> Log.d("Naurt", "User is outside.")
            NaurtEnvironmentFlag.Inside -> Log.d("Naurt", "User is inside.")
            NaurtEnvironmentFlag.NA -> Log.d("Naurt", "This phone is unable to produce this flag.")
        }

        when (location.motionFlag){
            NaurtMovement.Stationary -> Log.d("Naurt", "User not currently moving.")
            NaurtMovement.OnFoot -> Log.d("Naurt", "User is on foot.")
            NaurtMovement.VehicleMotion -> Log.d("Naurt", "User is in a vehicle.")
            NaurtMovement.NA -> Log.d("Naurt", "This flag has not been initialised yet.")
        }
}

```

Continue below for the full API documentation. A java docs version of the following documentation can be found in the Location Manager's Maven repository.

---


<br>

