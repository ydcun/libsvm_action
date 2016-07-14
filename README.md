# libsvm_action

该库是利用[libsvm](http://www.csie.ntu.edu.tw/~cjlin/)进行行为预测的案例库。
详细完整的介绍了数据从获取到featurs提取到最后的行为预测的完整过程。
> [系列教程推荐](http://blog.csdn.net/flydreamgg/article/details/4466023)

## 软件&&设备
- android手机
- R v3.3.1 && RStudio v0.99.902
- weak v3.9
- eclipse集成开发环境
- jdk1.8
- python2.6
- gp424win32

## 环境配置
- 将svm文件夹拷贝到系统当前用户目录下如：C:\Users\ydcun-psjs
- 安装好python2.6后将python 目录添加到环境变量path中
- gp424win32解压出来来的gnuplot文件夹：如：D:\Program Files\gnuplot\bin\pgnuplot.exe
- 修改svm\libsvm\tools\grid.py 中
	self.gnuplot_pathname = r'D:/Program Files/gnuplot/bin/pgnuplot.exe' 替换成自己的目录

## 训练样本

#### 手机获取加速速度数据并对行为进行标定
- 将手机加速度传感器的加速度值进行三轴向量和运算（使得运动的时候行为与手机姿态没有关系）
- 采集的时候是用32HZ频率以4s为一个时间窗进行采集数据，每窗数据有128个加速度数据
- 训练需要的数据在csv文件中（com.ydcun.libsvm_action.accdata.csv文件中）
- 将csv文件导入到R中
	> accdata = read.csv(file="C:\\Users\\ydcun-psjs\\Desktop\\机器学习\\accdata.csv")
	> library(sqldf)
	> accdata2 = sqldf("select act,position,t_min,t_max,t_mcr,t_sttdev,t_mean,t_rms,t_iqr,t_mad,t_variance from accdata where act='Walking' or act='Running'")
	> write.csv(accdata2,file="C:\\Users\\ydcun-psjs\\Desktop\\机器学习\\accdata2.csv");
- weka将accdata2.csv文件转换成arff文件

#### 提取特征
- 最小值 	min
- 最大值 	max
- 方差 variance 1/length*sum(xi-x)^2   (0 < i < length)
- 过均值率 mcr （以均值为分界线，前后点分布在分界线两侧的几位一次 sum/length）
- 标准差 stddev
- 平均值 mean
- 均方根平均值 rms  （先平方、再平均、然后开方）
- 四分位距 iqr （Q1~Q3 的差距）
- 绝对平均差 mad sum(|xi-x|)/length

#### 原始数据作图分析（accdata-data列）

> 加速度原始数据图
- library(rjson)
- walking = fromJSON(accdata[1,3])
- plot(walking,ylim=c(7,15),pch=20,xlab="accdata",ylab="accvalue",col="red",type="o")
- title("走路-128个加速度采样点图像")
- plot(running,pch=20,xlab="accdata",ylab="accvalue",col="red",type="o")
- title("跑步-128个加速度采样点图像")

> 特征图
- 将数据进行行为与位置进行排序，
	accdata2 = sqldf("select * from accdata2 order by act,position")
	plot(accdata2$t_min,pch=20,xlab="accdata",ylab="accvalue",col="red",type="o")
- 查看不同类型数据数量
	sqldf("select count(*),act from accdata2 group by act")
	abline(v=858, col="blue")
	sqldf("select count(*),act,position from accdata2 group by act,position")
	adcount = sqldf("select count(*) count,act,position from accdata2 group by act,position")
	temp =0;
	for(c in adcount$count){
 	temp =temp +c;
	abline(v=temp, col="black")
	}

```R
优化代码：
	adcount = sqldf("select count(*) count,act,position from accdata2 group by act,position")
	adcount2 = sqldf("select count(*),act from accdata2 group by act")
	setwd("C:\\Users\\ydcun-psjs\\Desktop\\机器学习")
	for(i in c(1:9)){
  	dev.new()
	  future1=paste(names(accdata2)[i+2],".png")
	  png(file=future1)

	  plot(accdata2[,i+2],pch=20,xlab="accdata",ylab="accvalue",col="red",type="o")
	  title(names(accdata2)[i+2])
	  #画分割线
	  temp =0;
	  for(c in adcount$count){
		temp =temp +c;
		abline(v=temp, col="black")
	  }
	  abline(v=adcount2[1,1], col="blue")
	  dev.off()
	}
```

#### Weka进行模型验证
- 保留所有列
- 删除方差
- 删除标准差
<table>
<tr><td></td><td>保留所有列</td><td>删除方差</td><td>删除标准差</td></tr>
<tr><td>Correctly Classified Instances</td><td>94.0473 %</td><td>93.9693 %</td><td>94.0473 %</td></tr>
<tr><td>Incorrectly Classified Instances</td><td>5.9527 %</td><td>6.0307 %</td><td>5.9527 %</td></tr>
<tr><td>Kappa statistic</td><td>0.8206</td><td>0.8156</td><td>0.8207</td></tr>
<tr><td>Mean absolute error</td><td>0.0595</td><td>0.0603</td><td>0.0595</td></tr>
<tr><td>Root mean squared error</td><td>0.244</td><td>0.2456</td><td>0.244 </td></tr>
<tr><td>Relative absolute error</td><td>17.1713%</td><td>17.3963 %</td><td>17.1713%</td></tr>
<tr><td>Root relative squared error</td><td>58.61%</td><td>58.9927 %</td><td>58.61%</td></tr>
<tr><td>Total Number of Instances</td><td>3847</td><td>3847 </td><td>3847</td></tr>
</table>
ps:
	Correctly Classified Instances:正确分类的实例
	Incorrectly Classified Instances:错误分类的实例
	Kappa statistic：k=1表明分类器完全与随机分类相异 k=0与随即分类相同 k=-1比随即分类还要差
	Mean absolute error：平均绝对误差
	Root mean squared error：均方根误差
	Relative absolute error：相对绝对误差
	Root relative squared error：相对均方误差

#### 读取mysql中的数据到R中处理
>	1. 安装RODBC工具
		install.packages("RODBC")
		library(RODBC)
>	2. 建立连接mysql
		channel=odbcConnect("mysqlodbc", uid="root", pwd="123456")
>	3. 查看所有的表
		sqlTables(channel)
>    4. 用sql语句获取数据
>> accdata = sqlQuery(channel,"select act,position,data,t_min,t_max,t_mcr,t_sttdev,t_mean,t_rms,t_iqr,t_mad,t_variance from accdata",stringsAsFactors=FALSE)
> library(sqldf)
> accdata2 = sqldf("select act,position,t_min,t_max,t_mcr,t_sttdev,t_mean,t_rms,t_iqr,t_mad,t_variance from accdata where act in ('Walking','Running') and position in ('Hand Fixed','Hand Swing')")
**ps:** stringsAsFactors 不会将不认识的string类型转换成factors

#### R语言导出csv文件
	write.csv(accdata2,file="C:\\Users\\ydcun-psjs\\Desktop\\机器学习\\accdata.csv");
