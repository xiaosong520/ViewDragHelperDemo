# ViewDragHelperDemo
利用ViewDragHelper 打造一个可拖拽下拉关闭/左右切换的页面。

#### QZone QQ空间的效果：

![QZone](https://github.com/xiaosong520/ViewDragHelperDemo/blob/master/Gif/QZone.gif)

#### MoMo播放页的效果：
![Momo.gif](http://upload-images.jianshu.io/upload_images/4835249-de402c5058e44b6c.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### demo实现的拖拽效果：

![效果图一](https://github.com/xiaosong520/ViewDragHelperDemo/blob/master/Gif/dragview_horizontal.gif)

#### demo真机演示效果：

![效果图二](https://github.com/xiaosong520/ViewDragHelperDemo/blob/master/Gif/dragview_vertical.gif)

### 主要的功能点有如下几个：
1. 滑动方向判定。
2. 单个方向的拖拽限制。
3. 事件分发以及拦截。
4. 平移动画处理。
5. 下拉时缩放及背景透明处理。
6. 背景高斯图片替换处理。
7. 嵌套ScrollView / RecyclerView事件冲突处理。
8. 多点触控 Invalid pointerId 问题解决。
