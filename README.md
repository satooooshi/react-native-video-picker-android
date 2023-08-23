# react-native-video-picker-android

A React Native package that allows you to select single or multiple videos from Android device library with support for compression. A common use case is to compress videos and optimize the file size before uploading them to the server.

## Installation

```sh
npm install react-native-video-picker-android
yarn add react-native-video-picker-android
```

## Usage

```js
// react-native.config.js
module.exports = {
  dependencies: {
    'react-native-video-picker-android': {
      platforms: {
        ios: null,
      },
    },
  },
};
```

```ts
import { pickVideo } from 'react-native-video-picker-android';
// ...
try {
  const uris: string[] = await pickVideo({
    maxFileSize: 1024 * 1000000,
    multiple: false,
    compress: true,
    onProgress: (prog) => {
      setProgress(prog);
    },
    quality: 'high',
    lowerBoundForCompress: 70 * 1000000,
    duration: 300,
  });
  console.log('---- pickVideo() success ', uris);
} catch (err) {
  console.log('---- pickVideo() error ', err, (err as any)?.code);
}
```

## Run Example Project

```sh
yarn example android
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
