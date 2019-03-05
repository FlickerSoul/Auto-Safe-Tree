# Auto-Safe-Tree

好了抽一(ji)个小时出来写这个。

在早先[这个文章](https://blog.flicker-soul.me/2018/code011-auto-safetree/)里面有提到过这么一个自动完成脚本。但是用起来不爽，且不能给别人用，那就想到做一个UI提供给用户输入。最近在搞JavaFX，就顺便做了一下。原本以为只要15分钟，结果搞了一个星期。

正式开始介绍软件使用：

运行环境：JAVA 11

主界面：

![main window](https://blog.flicker-soul.me/wp-content/uploads/2019/02/empty-main-window.png)

第一栏的login section用于填写学生信息：用户名和密码；分别用两个文件包装；格式：一行一个用户名或者对应密码。当前用户名列表的密码统一为一个时，将密码写在第一行即可。对于那些懒的管理员来说，123456无需修改即可直接当作初始密码，同时不需要导入密码文件。

接下来个列表大框时用于展示配置文件，配置文件由下面两个按钮导入。导入后结果：

![typical configuration main window](https://blog.flicker-soul.me/wp-content/uploads/2019/02/typical-main-window.png)

双击配置文件可以进入修改；选中配置文件然后按下`Del`可以进行删除。

然后讲讲如何配置所需的配置文件。

![categories](https://blog.flicker-soul.me/wp-content/uploads/2019/02/categories.png)

可以看到，目前教育平台分为两类：一个是安全学习，我把它叫做Test，因为它的网页里面自带答案，同时就是像一个答卷一样；另一个时专题活动，他通常包括一个已观看按钮，和一个问卷调差，所以我把它叫做Survey。

Test的配置如下

![test configuration](https://blog.flicker-soul.me/wp-content/uploads/2019/02/default-test-page.png)

由于tests的格式比较固定，所以默认的配置一般够用，同时这些配置有很高的通用性，所以可以同时使用。如果想让一个配置同时在多个Test中使用，则不需要填写第一行的URL，用于创建全局配置，当未完成的作业里面的Test在自动完成时没有找到对应的URL的配置时，则会调用全局配置。

打开Chrome或者其他现代浏览器，打开开发者选项用来查找需要的配置语句

![test page](https://blog.flicker-soul.me/wp-content/uploads/2019/02/second-selector-on-test-page.png)

由于test一般没有观看按钮，所以第一个按钮一般放空，直接查找第二个按钮用于进入测试界面。

如图所示，在Chrome里面按下`CTRL + SHIFT + C` 可以在界面上查找元素。找到点击完成的入口，点击一下，在开发者选项中就会选中相应的元素，然后右键在图示的菜单区域选中复制XPath，然后填入Test配置界面内指定区域。

由于页面自带答案，所以一般使用默认的选择器（即value选择器）即可。

![self-answered](https://blog.flicker-soul.me/wp-content/uploads/2019/02/input-selector-on-test-page.png)

然后在文档的最下面用相同的方式找到提交按钮的配置，填入即可；

![get submit button](https://blog.flicker-soul.me/wp-content/uploads/2019/02/submit-button-on-test-page.png)

随后提交即可。

关于Survey配置会有一点麻烦，下面这是空的Survey配置界面：

![empty survey configuration window](https://blog.flicker-soul.me/wp-content/uploads/2019/02/default-survey-page.png)

因为Survey的配置经常变化，所以第一个URL必须填入，一次来确定配置信息的对应。

然后再URL右边选中答案文件。答案文件标准：使用文本文本，将答案输入成连续的一行，全部大写。

至于为什么要分成男生女生的原因就在这里，有的问卷调查需要填写男女，如果一个班都是男的或者都是女的很容易引起嫌疑（23333）。

然后根据相同的方法选择“已观看”按钮和进入答题界面的按钮，然后分别填入指定区域：

![first selector on survey page](https://blog.flicker-soul.me/wp-content/uploads/2019/02/first-selector-on-survey-page.png)

![second selector on survey page](https://blog.flicker-soul.me/wp-content/uploads/2019/02/second-selector-on-survey-page.png)

然后用开发者选项查看答案选择框的规律：

![pattern](https://blog.flicker-soul.me/wp-content/uploads/2019/02/question-input-selector.png)

可以看到，答案的id是由`radio_题号_答案序号`组成的，且都是从1开始。知道这个之后就可以survey界面内的选择器了

接着用相同的方法找到下一页（可选）和提交答案的按钮配置：

![next page button](https://blog.flicker-soul.me/wp-content/uploads/2019/02/next-page-xpath.png)

![submit button](https://blog.flicker-soul.me/wp-content/uploads/2019/02/submit-xpath.png)

接着根据网络情况填写点击答案的延迟毫秒，如果电脑处理性能不行或者网络不流畅，容易导致无法答题的问题。

随后大概配置文件如下（页面在[这里](https://huodong.xueanquan.com/2019Winter/2019Winter_one.html)，可以自己练练手)：

![survey configuration](https://blog.flicker-soul.me/wp-content/uploads/2019/02/typical-configuration.png)

最后填写完成大概是这样：

![done](https://blog.flicker-soul.me/wp-content/uploads/2019/02/typical-main-window.png)

下面接着的Thread Num是线程数量，支持自己输入，必须是双数，因为将平均分配给男女生。建议为2或者4，不然资源消耗过大。

下面选择框表示失败时是否重试，默认是。

最右边的版面是Log的输出，不然没有点相应会让人很心酸

最后点击start开始运行吧！

![working](https://blog.flicker-soul.me/wp-content/uploads/2019/02/typical-main-window.png)

首先会检测账号受否由未完成；如果有，则尝试完成；没有则退出。

![done](https://blog.flicker-soul.me/wp-content/uploads/2019/02/done.png)

如果发现有什么使用问题，请联系i@flicker-soul.me；同时请发送日志文件，文件位置在`C:\Users\你的用户名\AUTO_SAFE_TREE_LOG\`。你的反馈是这个软件进步的动力！谢谢！

