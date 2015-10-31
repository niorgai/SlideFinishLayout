# SlideFinishLayout

从左侧滑动返回的Layout

特性
---
1. 当Activity无横向滚动的控件(即子View不会消费Touch事件),可以从Activity任意位置向右侧滑动返回.
2. 当Activity中有横向滚动的控件,则必须从左侧边缘开始滑动才能返回.

用法
---
1. 将`SlideFinishLayout`作为xml文件的根布局.
2. Java文件中设置`FinishListener`

		((SlideFinishLayout)findViewById(R.id.root_layout)).setFinishListener(new SlideFinishLayout.onSlideFinishListener() {
	            @Override
	            public void onSlideFinish() {
	                this.finish();
	                //消除Finish的动画
	                overridePendingTransition(0, 0);
	            }
	        });
3. style文件中设置`windowIsTranslucent`为true使Activity透明.

自定义
---
建议将代码复制到项目中使用.

1. 初始化默认使用黑色透明作为底层.即`backDrawable`对象的初始化.
2. 默认设置白色作为背景色,即`setBackgroundResource`方法

Issues
---
因为设置的Activity透明,所以Activity的`onStop()`方法不会被调用,Activity过多会导致性能下降,目前暂时没有想到解决方法.