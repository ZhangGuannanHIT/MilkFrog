@Entry
@Component
struct Index {
  @State @Watch('onVideoSwitch') isVideo1: boolean = true;
  @State isLooping: boolean = false;
  @State buttonText: string = '循环播放';
  private videoController: VideoController = new VideoController();

  // 页面初次加载
  aboutToAppear(): void {
    this.showFirstFrame();
  }

  // 视频切换后
  onVideoSwitch(): void {
    this.showFirstFrame();
  }

  // 首帧固定
  private showFirstFrame(): void {
    this.videoController.start();
    setTimeout(() => {
      this.videoController.pause();
      this.videoController.setCurrentTime(0);
    }, 400);
  }

  build() {
    Stack({ alignContent: Alignment.Bottom }) {
      if (this.isVideo1) {
        Video({
          src: $rawfile('1.mp4'),
          controller: this.videoController
        })
          .width('100%')
          .height('100%')
          .objectFit(ImageFit.Cover) 
          .controls(false)
          .loop(this.isLooping)
      } else {
        Video({
          src: $rawfile('2.mp4'),
          controller: this.videoController
        })
          .width('100%')
          .height('100%')
          .objectFit(ImageFit.Contain) 
          .controls(false)
          .loop(this.isLooping)
          .backgroundColor(Color.White) 
      }

      // 底部按钮
      Row({ space: 12 }) {

        Button(this.buttonText)
          .width('65%')
          .height(44)
          .fontSize(16)
          .fontColor(Color.White)
          .backgroundColor(this.isLooping ? '#FF5722' : '#4CAF50')
          .borderRadius(22)
          .onClick(() => {
            this.isLooping = !this.isLooping;
            if (this.isLooping) {
              this.buttonText = '停止播放';
              this.videoController.start();
            } else {
              this.buttonText = '循环播放';
              this.videoController.pause();
            }
          })

        Button() {
          Text('⇄')
            .fontSize(22)
            .fontColor(Color.White)
        }
          .width(44)
          .height(44)
          .borderRadius(22)
          .backgroundColor('#2196F3')
          .onClick(() => {
            if (this.isLooping) {
              this.videoController.stop();
              this.isLooping = false;
              this.buttonText = '循环播放';
            }
            this.isVideo1 = !this.isVideo1;
          })
      }
      .justifyContent(FlexAlign.Center)
      .width('100%')
      .margin({ bottom: 36 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor(Color.White)
  }
}
