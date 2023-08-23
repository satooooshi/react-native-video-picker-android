import * as React from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
import { pickVideo, clean } from 'react-native-video-picker-android';

export default function App() {
  const [progress, setProgress] = React.useState<number | undefined>();

  return (
    <View style={styles.container}>
      <Button
        title="choose video"
        onPress={async () => {
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
      <Text style={styles.box}>
        {progress ? `Progress: ${progress} %...` : ''}
      </Text>
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
