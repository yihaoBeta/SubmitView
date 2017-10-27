# SubmitView
一个用来提交表单或者某些短时耗时操作的自定义view，有两种模式，带有进度值和没有进度值

* 不带进度的SubmitView
* 带进度的SubmitWithProgressView
---

![](https://github.com/yihaoBeta/SubmitView/blob/master/screenshot/screenshot.gif)

---

### 使用方法

代码比较简单，注释也比较详细，支持自定义各种属性

* 不带进度的SubmitView
  - `submit()` 该方法用于开始进行操作或者提交信息
  - `submitCompleted()` 该方法用于操作完成时调用，以便显示最后的完成动画
  - `reset()` 该方法用于重置view，恢复最初的状态
  - `setText(@NonNull String text)` 设置文本
  - `setTextSize(@NonNull float textSize)` 设置文本字体大小
  - `setTextColor(@NotNull int textColor)` 设置文本颜色
  - `setBgColor(@NotNull int bgColor)` 设置整个view的背景颜色
  - `setTickColor(@NotNull int tickColor)` 设置最后的对勾的颜色
  
* 带进度值的SubmitWithProgressView
  - `submit()` 该方法用于开始进行操作或者提交信息
  - `setProgressValue(@NotNull int progressValue)` 设置进度值
  - `setMaxProgressValue(@NotNull int maxProgressValue)` 设置最大进度值
  - `reset()` 重置view状态
  - `cancel()` 用于在操作过程中取消操作时调用
  - `getCurrentStatus()` 获取当前view的状态，返回的是Status枚举
  - 其他的自定义属性方法就不一一列举了，大家看代码吧
  
关于在xml使用自定义属性，可以看源码来了解
  
