# ApkShield
一代壳加固方案的demo

大致思路就是先写一共壳apk,将它的dex文件取出,将源apk放到壳dex中,再将合并的dex文件放回到源apk中,这个方案本身也不安全因为apk最后还是会落地的，这里的意义更多的只是个人学习．

最近正好看了一点相关的东西,一代壳还是比较简单的,而且涉及到一些dex文件处理和apk动态加载等等我想折腾一下的点,所以就想实现一个简单的版本玩一下.

项目下有两个文件夹

### androidpjt
主要是android项目,一个壳应用,一个用来演示的源应用.

因为涉及到apk的动态加载,所以现在还是一个helloworld的app,没有写的太复杂.

另外一个是壳app,这个应用只用来生成dex,再和源apk合并用的,主要涉及的就是动态加载

### tool
主要是两个脚本

apkshield.py用来合并壳dex和源apk,然后生成加固后的apk.

get_payload_dex.py 用来从合并后的dex(注意这里是dex不是合并后的apk,这里的dex就是放回到源apk里面的),取出源apk,一开始是调试用的,现在也一并留着了

### 进度
目前没有做完,并没有让加固后的apk跑起来

#### 已经完成的部分
将壳dex和源apk 合并,生成加固后的apk

在apk中解析出源apk,工具和应用内都完成了该部分功能

调用源app的application 的oncreate方法,参考了一些别人的思路,先调起来吧,activity还没起来

#### 未完成的部分

反编译源apk，修改其mainfest.xml文件，因为这里要让壳来解出原先的apk，再运行apk的application

动态加载apk，使其完整启动，还得再看看相关的知识，看起来很多地方得拿反射替一下,现在只能起来application
