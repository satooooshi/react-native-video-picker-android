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

export type PickerErrorCode =
  | 'E_ACTIVITY_DOES_NOT_EXIST'
  | 'E_CALLBACK_ERROR'
  | 'E_NO_LIBRARY_PERMISSION'
  | 'E_FAILED_TO_SHOW_PICKER'
  | 'E_NO_IMAGE_DATA_FOUND'
  | 'E_ERROR_WHILE_CLEANING_FILES'
  | 'E_VIDEO_SIZE_TOO_BIG'
  | 'E_COMPRESS_CANCELLED';

export function clean(): Promise<void> {
  return VideoPickerAndroid.clean();
}

export function cancel(): Promise<void> {
  return VideoPickerAndroid.cancel();
}

export async function pickVideo(
  options: {
    compress?: boolean;
    multiple?: boolean;
    maxFiles?: number;
    maxFileSize?: number;
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
