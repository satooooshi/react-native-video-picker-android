import * as React from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import { pickVideo, clean } from 'react-native-video-picker-android';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  return (
    <View style={styles.container}>
      <Button
        title="choose video"
        onPress={async () => {
          try {
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
      <Button
        title="clean"
        onPress={async () => {
          try {
            await clean();
            console.log('---- clean() success ');
          } catch (err) {
            console.log('---- clean() error ', err, (err as any)?.code);
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
