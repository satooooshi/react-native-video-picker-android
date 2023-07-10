import {
  NativeModules,
  Platform,
  NativeEventEmitter,
  NativeEventSubscription,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-video-picker-android' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const VideoPickerAndroid = NativeModules.VideoPickerAndroid
  ? NativeModules.VideoPickerAndroid
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const VideoPickerAndroidEventEmitter = new NativeEventEmitter(
  NativeModules.VideoPickerAndroid
);

export function multiply(a: number, b: number): Promise<number> {
  return VideoPickerAndroid.multiply(a, b);
}

export function clean(): Promise<void> {
  return VideoPickerAndroid.clean();
}

export async function pickVideo(
  options: {
    compress?: boolean;
    multiple?: boolean;
    maxFiles?: number;
    onProgress?: (progress: number) => void;
  } = { compress: false, multiple: false, maxFiles: 0, onProgress: undefined }
): Promise<string[]> {
  let subscription: NativeEventSubscription;
  try {
    if (options?.onProgress) {
      subscription = VideoPickerAndroidEventEmitter.addListener(
        'onCompressProgress',
        (event: any) => {
          options?.onProgress?.(event.progress);
        }
      );
    }
    return await VideoPickerAndroid.pickVideo(options);
  } finally {
    // @ts-ignore
    if (subscription) {
      subscription.remove();
    }
  }
}
