import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import { pickVideo } from 'react-native-video-picker-android';

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
          // await multiply(3, new Date().getTime()).then((res) => {
          //   console.log(res);
          // });

          try {
            // const aa = await clean();
            // console.log(aa);
            const uris = await pickVideo({
              maxFiles: 10,
              compress: true,
              multiple: false,
              onProgress: (progress) => {
                console.log(progress);
                setResult(progress);
              },
            });
            console.log('---- pickVideo() success ', uris);
          } catch (err) {
            console.log('---- pickVideo() error ', err, (err as any)?.code);
          }
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
