import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  NativeEventEmitter,
} from 'react-native';
import { multiply, pickVideo } from 'react-native-video-picker-android';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    // multiply(3, 7).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Button
        title="choose video"
        onPress={async () => {
          let ans = 0;
          await multiply(3, new Date().getTime()).then((res) => {
            console.log(res);
            ans = res;
          });
          const uris = await pickVideo({
            maxFiles: 10,
            compress: true,
            multiple: false,
            onProgress: (progress) => {
              console.log(progress);
              setResult(progress);
            },
          });
          console.log('---- CalendarModule.pickVideo() ', uris);
        }}
      />
      <Text style={styles.box}>{result ? `Progress: ${result} %...` : ''}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 100,
    height: 60,
    marginVertical: 20,
  },
});
