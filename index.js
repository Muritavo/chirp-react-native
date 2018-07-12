import { NativeModules, NativeEventEmitter, EmitterSubscription } from "react-native";

type ChirpEvent =
  | "onSending"
  | "onSent"
  | "onReceiving"
  | "onReceived"
  | "onStateChanged";
type ChirpConnect = {
    init: (key: string, secret: string) => void,
    getLicence: () => Promise,
    setLicence: (licence: string) => void,
    start: () => void,
    stop: () => void,
    send: (data: Uint8Array) => void,
    sendRandom: () => void,
    on: (eventType: ChirpEvent, callback: (event: object) => void) => EmitterSubscription,
    removeListeners: () => void
}

const ChirpConnect: ChirpConnect = NativeModules.RNChirpReactNative;
const RNChirpReactNativeEvents = new NativeEventEmitter(ChirpConnect);

const ChirpEvents = {
  onSending: "onSending",
  onSent: "onSent",
  onReceiving: "onReceiving",
  onReceived: "onReceived",
  onStateChanged: "onStateChanged"
};

const RNChirpReactNativeEventsHandler = (event: ChirpEvent, callback) => {
  return RNChirpReactNativeEvents.addListener(event, callback);
};
const clearRNChirpReactNativeEventsHandler = () => {
    RNChirpReactNativeEvents.removeAllListeners();
}

ChirpConnect.on = RNChirpReactNativeEventsHandler;
ChirpConnect.removeListeners = clearRNChirpReactNativeEventsHandler;

export default ChirpConnect;
export { ChirpEvents };
