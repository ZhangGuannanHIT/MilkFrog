@Entry
@Component
struct Index {
  @State @Watch('onVideoSwitch') isVideo1: boolean = true;
  @State isLooping: boolean = false;
  @State buttonText: string = '循环播放';
  private videoController: VideoController = new VideoController();

  // 页面初次加载：展示首帧作为视频封面
  aboutToAppear(): void {
    this.showFirstFrame();
  }

  // 视频切换后的回调：新Video组件已创建完毕
  onVideoSwitch(): void {
    // 始终展示新视频的首帧封面（播放中切换已事先停止，此处不再恢复播放）
    this.showFirstFrame();
  }

  // 短暂启播后暂停并回到起始位置，将首帧固定在页面上
  private showFirstFrame(): void {
    this.videoController.start();
    setTimeout(() => {
      this.videoController.pause();
      this.videoController.setCurrentTime(0);
    }, 400);
  }

  build() {
    Stack({ alignContent: Alignment.Bottom }) {
      // 使用 if/else 条件渲染：视频1与Cover缩放永久绑定，视频2与Contain缩放永久绑定
      // 切换时整个Video组件销毁并重建，确保源文件和缩放同步生效
      if (this.isVideo1) {
        Video({
          src: $rawfile('1.mp4'),
          controller: this.videoController
        })
          .width('100%')
          .height('100%')
          .objectFit(ImageFit.Cover) // 横向视频：等比放大填满屏幕高度，左右裁剪，展示中间适配手机的细长条
          .controls(false)
          .loop(this.isLooping)
      } else {
        Video({
          src: $rawfile('2.mp4'),
          controller: this.videoController
        })
          .width('100%')
          .height('100%')
          .objectFit(ImageFit.Contain) // 竖屏视频：完整适配手机屏幕
          .controls(false)
          .loop(this.isLooping)
          .backgroundColor(Color.White) // 覆盖视频组件内部默认黑边
      }

      // 底部按钮区域
      Row({ space: 12 }) {
        // 循环播放/停止播放 按钮（居中长条）
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

        // 切换视频按钮
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
              // 1. 停止播放
              this.videoController.stop();
              // 2. 改变播放按钮状态
              this.isLooping = false;
              this.buttonText = '循环播放';
            }
            // 3. 切换视频（触发onVideoSwitch → showFirstFrame）
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
