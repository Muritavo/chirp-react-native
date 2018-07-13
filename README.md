
# chirp-react-native
Use Chirp SDK for transfering data over sound on React Native. Developed based on [Chirp + React Native blog post](https://blog.chirp.io/chirp-connect-with-react-native-b5fb9977337)
For more info and getting credentials please visit [Chirp.io](https://developers.chirp.io)
## Getting started

`$ npm install chirp-react-native --save`

### Mostly automatic installation

`$ react-native link chirp-react-native`

### Manual installation


#### iOS (!!!NOT COMPATIBLE YET!!! Don't have a MAC for testing)

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `chirp-react-native` and add `RNChirpReactNative.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNChirpReactNative.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNChirpReactNativePackage;` to the imports at the top of the file
  - Add `new RNChirpReactNativePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':chirp-react-native'
  	project(':chirp-react-native').projectDir = new File(rootProject.projectDir, 	'../node_modules/chirp-react-native/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':chirp-react-native')
  	```


## Usage
```javascript
import RNChirpReactNative, {ChirpEvent} from 'chirp-react-native';

RNChirpReactNative;

//Initialize the SDK using the credentials given on the Chirp Admin Center https://admin.chirp.io/
RNChirpReactNative.init("API_KEY", "API_SECRET");

//Get the license (On the Admin Center can be set the method used by the SDK for communication. Standard (For audible sound) or Ultrasonic (for inaudible sound))
await RNChirpReactNative.getLicense();

//Start the SDK
RNChirpReactNative.start();

//Send a test sound (If set to Ultrasonic mode, the sound will be inaudible. You can use any app that detects Ultrasonic available on the Play Store for testing)
RNChirpReactNative.sendRandom();

//Or send an encoded data
RNChirpReactNative.send([172, 47, 117, 192]);

//Register a listener to catch transmited data
const listener = RNChirpReactNative.on(ChirpEvent.onReceived, (data) => {
	//Use the data received
	console.log(data);
})

//Stop the SDK and remove listeners to free up resources
RNChirpReactNative.stop();
RNChirpReactNative.removeListeners(); //Removes all listeners (useful for when closing the app or destroying the component)
listener.remove(); //Remove a single listener (useful for enabling or disabling specific actions)
```
  