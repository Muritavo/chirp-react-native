
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
package com.reactlibrary;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import chirpconnect.Chirpconnect;
import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.interfaces.ConnectLicenceListener;
import io.chirp.connect.models.ChirpError;
import io.chirp.connect.models.ConnectState;


public class RNChirpReactNativeModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String TAG = "RNChirpReactNative";
    private ChirpConnect chirpConnect;
    private ReactContext context;
    private boolean started = false;
    private boolean wasStarted = false;

    @Override
    public String getName() {
        return TAG;
    }

    public RNChirpReactNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("CHIRP_CONNECT_NOT_CREATED", ConnectState.ConnectNotCreated.ordinal());
        constants.put("CHIRP_CONNECT_STATE_STOPPED", ConnectState.AudioStateStopped.ordinal());
        constants.put("CHIRP_CONNECT_STATE_PAUSED", ConnectState.AudioStatePaused.ordinal());
        constants.put("CHIRP_CONNECT_STATE_RUNNING", ConnectState.AudioStateRunning.ordinal());
        constants.put("CHIRP_CONNECT_STATE_SENDING", ConnectState.AudioStateSending.ordinal());
        constants.put("CHIRP_CONNECT_STATE_RECEIVING", ConnectState.AudioStateReceiving.ordinal());
        return constants;
    }

    /**
     * init(key, secret)
     *
     * Initialise the SDK with an application key and secret.
     * Callbacks are also set up here.
     */
    @ReactMethod
    public void init(String key, String secret) {
        chirpConnect = new ChirpConnect(this.getCurrentActivity(), key, secret);
        chirpConnect.setListener(new ConnectEventListener() {

            @Override
            public void onSending(byte[] data) {
                WritableMap params = assembleData(data);
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onSending", params);
            }

            @Override
            public void onSent(byte[] data) {
                WritableMap params = assembleData(data);
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onSent", params);
            }

            @Override
            public void onReceiving() {
                WritableMap params = Arguments.createMap();
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onReceiving", params);
            }

            @Override
            public void onReceived(byte[] data) {
                WritableMap params = assembleData(data);
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onReceived", params);
            }

            @Override
            public void onStateChanged(byte oldState, byte newState) {
                WritableMap params = Arguments.createMap();
                params.putInt("status", newState);
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onStateChanged", params);
            }

            @Override
            public void onSystemVolumeChanged(int oldVolume, int newVolume) {}
        });
    }

    /**
     * getLicence()
     *
     * Fetch default licence from the network to configure the SDK.
     */
    @ReactMethod
    public void getLicence(final Promise promise) {
        chirpConnect.getLicence(new ConnectLicenceListener() {

            @Override
            public void onSuccess(String networkLicence) {
                ChirpError setLicenceError = chirpConnect.setLicence(networkLicence);
                if (setLicenceError.getCode() > 0) {
                    promise.reject("Licence Error", setLicenceError.getMessage());
                } else {
                    promise.resolve("Initialisation Success");
                }
            }

            @Override
            public void onError(ChirpError chirpError) {
                promise.reject("Network Error", chirpError.getMessage());
            }
        });
    }

    /**
     * setLicence(licence)
     *
     * Configure the SDK with a licence string.
     */
    @ReactMethod
    public void setLicence(String licence) {
        ChirpError setLicenceError = chirpConnect.setLicence(licence);
        if (setLicenceError.getCode() > 0) {
            onError(context, setLicenceError.getMessage());
        }
    }

    /**
     * start()
     *
     * Starts the SDK.
     */
    @ReactMethod
    public void start() {
        ChirpError error = chirpConnect.start();
        if (error.getCode() > 0) {
            onError(context, error.getMessage());
        }
        started = true;
    }

    /**
     * stop()
     *
     * Stops the SDK.
     */
    @ReactMethod
    public void stop() {
        ChirpError error = chirpConnect.stop();
        if (error.getCode() > 0) {
            onError(context, error.getMessage());
        }
        started = false;
    }

    /**
     * send(data)
     *
     * Encodes a payload of bytes, and sends to the speaker.
     */
    @ReactMethod
    public void send(ReadableArray data) {
        byte[] payload = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            payload[i] = (byte)data.getInt(i);
        }

        long maxSize = chirpConnect.getMaxPayloadLength();
        if (maxSize < payload.length) {
            onError(context, "Invalid payload");
            return;
        }
        ChirpError error = chirpConnect.send(payload);
        if (error.getCode() > 0) {
            onError(context, error.getMessage());
        }
    }

    /**
     * sendRandom()
     *
     * Sends a random payload to the speaker.
     */
    @ReactMethod
    public void sendRandom() {
        Random r = new Random();
        long length = (long)r.nextInt((int)chirpConnect.getMaxPayloadLength() - 1);
        byte[] payload = chirpConnect.randomPayload(length);


        ChirpError error = chirpConnect.send(payload);
        if (error.getCode() > 0) {
            onError(context, error.getMessage());
        }
    }

    /**
     * asString(data)
     *
     * Returns a payload represented as a hexadecimal string.
     */
    public static String asString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static WritableMap assembleData(byte[] data) {
        WritableArray payload = Arguments.createArray();
        for (int i = 0; i < data.length; i++) {
            payload.pushInt(data[i]);
        }
        WritableMap params = Arguments.createMap();
        params.putArray("data", payload);
        return params;
    }

    private void onError(ReactContext reactContext,
                         String error) {
        WritableMap params = Arguments.createMap();
        params.putString("message", error);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onError", params);
    }

    @Override
    public void onHostResume() {
        if (wasStarted) {
            chirpConnect.start();
        }
    }

    @Override
    public void onHostPause() {
        wasStarted = started;
        chirpConnect.stop();
    }

    @Override
    public void onHostDestroy() {
        wasStarted = started;
        try {
            chirpConnect.close();
        } catch(IOException err) {}
    }
}