# ApkShield
一代壳加固方案的demo

大致思路就是先写一共壳apk,将它的dex文件取出,将源apk放到壳dex中,再将合并的dex文件放回到源apk中,这个方案本身也不安全因为apk最后还是会落地的，这里的意义更多的只是个人学习．

最近正好看了一点相关的东西,一代壳还是比较简单的,而且涉及到一些dex文件处理和apk动态加载等等我想折腾一下的点,所以就想实现一个简单的版本玩一下.

项目下有两个文件夹

### androidpjt
主要是android项目,一个壳应用,一个用来演示的源应用,以及一个native的壳的项目(开发中,目前对jni不是很熟,还在看)

因为涉及到apk的动态加载,所以现在还是一个helloworld的app,没有写的太复杂.

另外一个是壳app,这个应用只用来生成dex,再和源apk合并用的,主要涉及的就是动态加载

### tool
主要是三个脚本

#### apkshield.py
用来合并壳dex和源apk,然后生成加固后的apk.<br>
使用方法 -s <壳dex路径> -p <源apk路径>

#### shield_manifest.py
用来对加壳后的apk Androidmanifest.xml进行处理的工具,将源application名称改为壳的application,并在meta中添加源appliction信息<br>
使用方法 -p <源apk路径>

#### get_payload_dex.py 
用来从合并后的dex(注意这里是dex不是合并后的apk,这里的dex就是放回到源apk里面的),取出源apk,一开始是调试用的,现在也一并留着了<br>
使用方法 -p 合并后的dex路径

### 加壳方法
首先使用apkshield.py将源apk加到壳dex中并放回源apk中,再使用shield_manifest.py来修改源apk中的androidmanifest.xml,最后自行使用签名工具签名即可

### 进度
已经完成了最基本的demo,可以将原apk藏到壳dex后面,并成功运行,使用工具进行自动化加壳

### 还能优化的点
虽然一代壳的技术本身已经比较旧了但是我比较菜很多东西都不会，感觉还能再学一些看看，所以看了一点儿大佬的博客，这个东西还是有点改进空间的<br>
+ 这里的加壳其实主要是为了dex来加壳,所以其实没有必要将整个apk文件加到壳dex后面,可以只将源apk的dex加到壳dex后面
+ 使用jni,加大破解难度,还能学一点儿c和jni知识
+ 将源文件藏到别的地方可能也是一个可以优化的地方,这里暂时不做展开了

#### 踩到的坑
emmmm....又弄到很晚,还是没有太大的进展,倒是发现一些其他的东西,今天试了一下修改manifest,shieldapplication去代替源apk的application时,需要写上完整包名,现在解出来的apk长度太长了,java不支持32位以上的下标,刚刚查知乎说c++可以,明天再试一下看看.

对python的xml解析不熟悉也吃了一点小亏浪费很多时间,不过现在可以完全跑起来了,实现自动化加壳.

今天又试了一下这个东西,大小超长并不是因为本身apk超长了,而是为了修改manifest,使用了apktool反编译apk,然后修改manifest后,再重新对apk打包,这个重新打包的过程肯定是有些动作的,比如说优化了之类的,实际情况下看了一下重打包之前的apk,
大小和没有加壳之前差不多,晚上吃饭的时候和土豆师傅聊了下这个.现在打算仍然重打包apk,但是仅仅取出重打包apk后的manifest文件,然后再加入到重打包之前的apk中,这样就还是成功改动了manifest文件并且不让dex有什么变化,实际操作后当然也是成功了,
源程序重新启动了.本身代码没什么改动的地方,毕竟现在情况很简单.

又踩到一个坑...卧槽从八点多到十一点半了,按照上面说的优化点,单独将源dex合并,然后再进行加壳,这里发现一个问题,
我解出来的dex放的文件夹,和创建classloader时创建时设置的odex文件夹目录是同一个,这里是不是会有这样一个问题,
当做dex2oat优化的时候,将dex转化为odex,先前在classlooader传入的是apk,所以是从payload.apk得到payload.dex,不会有什么问题,
但是现在直接解出来的就是一个dex,这里可能是有重名的问题,生成的odex(应该是odex,我看了一下是一共elf文件,但是这里后缀仍然是dex),将解出来的那个dex覆盖了,
我这里也确实看到了源dex变成一个elf的情况,所以后来将odexpath设置为了别的目录,就成功启动了.
