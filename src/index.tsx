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

export interface CommonOptions {
  /**
   * Muximum selectable file size. (Set 0 for no limit.)
   *
   * @default 0
   */
  maxFileSize?: number;
  onProgress?: (progress: number) => void;
}

type SingleOptions = CommonOptions & {
  /**
   * @default false
   */
  multiple: false;
  /**
   * Enable or disable compression.
   *
   * @default false
   */
  compress?: boolean;
  /**
   * Only compress file whose minimum size exceeds lowerBoundForCompress.(Set 0 for compressing unconditionally.)
   *
   * @default 0
   */
  lowerBoundForCompress?: number;
};

type MultipleOptions = CommonOptions & {
  /**
   * @default false
   */
  multiple: true;

  /**
   * Muximum number of files selectable. (Set 0 for no limit.)
   *
   * @default 0
   */
  maxFiles?: number;
};

export type Options = SingleOptions | MultipleOptions;

export type PickerErrorCode =
  | 'E_ACTIVITY_DOES_NOT_EXIST'
  | 'E_CALLBACK_ERROR'
  | 'E_NO_LIBRARY_PERMISSION'
  | 'E_FAILED_TO_SHOW_PICKER'
  | 'E_NO_IMAGE_DATA_FOUND'
  | 'E_ERROR_WHILE_CLEANING_FILES'
  | 'E_VIDEO_SIZE_TOO_BIG'
  | 'E_COMPRESS_CANCELLED'
  | 'E_EXCEEDS_MAX_NUM_OF_FILES';

export function clean(): Promise<void> {
  return VideoPickerAndroid.clean();
}

export function cancel(): Promise<void> {
  return VideoPickerAndroid.cancel();
}

export async function pickVideo(options: Options): Promise<string[]> {
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
