# react-native-video-picker-android

A React Native package that allows you to select single or multiple videos from Android device library with support for compression. A common use case is to compress videos and optimize the file size before uploading them to the server.

## Installation

```sh
npm install react-native-video-picker-android
yarn add react-native-video-picker-android
```

## Usage

```ts
const { pickVideo } = require('react-native-video-picker-android');
const uris: string[] = await pickVideo({
  maxFiles: 1,
  compress: true,
  multiple: false,
  onProgress: (progress: number) => {
    setProgCompressAndroid(progress);
  },
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
