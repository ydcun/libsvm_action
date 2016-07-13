/**
 * 
 */
package com.ydcun.libsvm_action.svm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ydcun.libsvm_action.libsvm.svm_scale;
import com.ydcun.libsvm_action.libsvm.svm_train;
import com.ydcun.libsvm_action.model.Item;
import com.ydcun.libsvm_action.util.CSVFileUtil;
import com.ydcun.libsvm_action.util.Constant;
import com.ydcun.libsvm_action.util.DateUtil;
import com.ydcun.libsvm_action.util.Features;
import com.ydcun.libsvm_action.util.Util;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * @author ydcun-psjs
 *
 */
public class Main {
	private static String dir = Main.class.getResource("../resource").getPath().substring(1);
	/**
	 * 训练model
	 * @throws Exception
	 */
	public static void trainModel() throws Exception{
		String fileName = "accdata2.csv";
		List<Item> list = readFile(fileName);
		System.out.println(list.size());
		//创建训练文件
		createTrainFile(list,"train");
		//创建归一化文件
		creatScaleFile( new String[]{"-l","0","-u","1","-s",dir+"/range",dir+"/train"}, dir+"/scale");
		//用grid.py计算c g的值  如果报错极有可能是scale文件内格式错误
		String[] cgr = cmdGridPy("python "+Util.getSvmPath()+"/libsvm/tools/grid.py"+" "+dir+"/scale",dir+"/grid");
		//生成model模型文件 "-v","5",
		creatModeFile(new String[]{"-s","0","-c",cgr[0],"-t","2","-g",cgr[1],"-e","0.1",dir+"/scale",dir+"/model"});
	}
	/**
	 * 识别
	 * @throws Exception 
	 */
	public static void predict() throws Exception{
		String[] ruleArr = readRule(dir+"/range");
		int sum = ruleArr.length-2;//获取标准文件中特征数量
		String[] tempArr = null;//临时存放每个特征信息lable：tempArr[0] 最小值:tempArr[1] 最大值:tempArr[2]
		//分析归一化内部文件
		tempArr = ruleArr[1].split(" ");
		double lower = Double.parseDouble(tempArr[0]);
		double upper = Double.parseDouble(tempArr[1]);
		
		//定义数据点
		String trainTest = Util.readFileToString(dir+"/trainTest2");
		System.out.println(trainTest);
		String[] trainTestLineArry = trainTest.split(Util.getChangeRow());
		String[] trainTestItemArry = null;
		svm_node[] px =null;
		svm_node p = null;
		String[] tempNode = null;
		StringBuffer sb = new StringBuffer();
		for(int j=0;j<trainTestLineArry.length;j++){
			trainTestItemArry = trainTestLineArry[j].split(" ");
			px = new svm_node[sum];
			for(int i=0;i<9;i++){
				p = new svm_node();
				tempArr = ruleArr[i+2].split(" ");
				tempNode = trainTestItemArry[i+1].split(":");
				p.index = Integer.parseInt(tempNode[0]);
				p.value = Features.zeroOneLibSvm(lower, upper, Double.parseDouble(tempNode[1]), Double.parseDouble(tempArr[1]), Double.parseDouble(tempArr[2]));
				px[i] = p;
			}
			svm_model model = svm.svm_load_model(dir+"/model");
			double code = svm.svm_predict(model, px);
			DateUtil.printNameDate(new Date(),"预测结束");
			if(trainTestItemArry[0].equals(code+"")){
				System.out.println("预测结果："+code+" 真实结果:"+trainTestItemArry[0]+" true");
				sb.append("预测结果："+code+" 真实结果:"+trainTestItemArry[0]+" true");
			}else{
				System.err.println("预测结果："+code+" 真实结果:"+trainTestItemArry[0]+" false");
				sb.append("预测结果："+code+" 真实结果:"+trainTestItemArry[0]+" false");
			}
			sb.append(Util.getChangeRow());
		}
		Util.stringToFile(sb.toString(),dir+"/trainTestResult", false);
		
	}
	public static void main(String[] args) throws Exception {
		predict();
	}

	
	
	
	
	
	/**
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private static List<Item> readFile(String fileName) throws Exception {
		DateUtil.printNameDate(new Date(), "开始读取" + fileName + "文件");
		List<Item> list = new ArrayList<Item>();
		
		CSVFileUtil csv = new CSVFileUtil(dir + "/" + fileName);
		String temp = null;
		ArrayList<String> tempList = null;
		csv.readLine();// 先读取表头
		while ((temp = csv.readLine()) != null) {
			tempList = csv.fromCSVLinetoArray(temp);
			Item tempItem = new Item(tempList.get(1), tempList.get(2), Double.valueOf(tempList.get(3)),
					Double.valueOf(tempList.get(4)), Double.valueOf(tempList.get(5)), Double.valueOf(tempList.get(6)),
					Double.valueOf(tempList.get(7)), Double.valueOf(tempList.get(8)), Double.valueOf(tempList.get(9)),
					Double.valueOf(tempList.get(10)), Double.valueOf(tempList.get(11)));
			list.add(tempItem);
		}
		DateUtil.printNameDate(new Date(), "读取" + fileName + "文件 完成");
		return list;
	}
	private static void createTrainFile(List<Item> list,String trainFileName){
		DateUtil.printNameDate(new Date(), "创建"+trainFileName+"文件");
		// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
		StringBuffer sb = new StringBuffer();
		Item tempItem = null;
		for(int i=0;i<list.size();i++){
			tempItem = list.get(i);
			//定义该行训练节点
			sb.append(Constant.actMapToCode.get(tempItem.getAct()));
			//最小值
			sb.append(" "+Constant.FUN_101_MINIMUM_CODE+":"+tempItem.getT_min());
			//最小值
			sb.append(" "+Constant.FUN_102_MAXIMUM_CODE+":"+tempItem.getT_max());
			//方差 
			sb.append(" "+Constant.FUN_103_VARIANCE_CODE+":"+tempItem.getT_variance());
			//过均值率
			sb.append(" "+Constant.FUN_104_MEANCROSSINGSRATE_CODE+":"+tempItem.getT_mcr());
			//标准差
			sb.append(" "+Constant.FUN_105_STANDARDDEVIATION_CODE+":"+tempItem.getT_sttdev());
			//平均值
			sb.append(" "+Constant.FUN_106_MEAN_CODE+":"+tempItem.getT_mean());
			//均方根平均值
			sb.append(" "+Constant.FUN_112_RMS_CODE+":"+tempItem.getT_rms());
			//四分卫距
			sb.append(" "+Constant.FUN_114_IQR_CODE+":"+tempItem.getT_iqr());
			//绝对平均差
			sb.append(" "+Constant.FUN_115_MAD_CODE+":"+tempItem.getT_mad());
			sb.append(Util.getChangeRow());//根据系统不同获取换行符
		}
		Util.stringToFile(sb.toString(),dir+"/"+trainFileName,false);
		DateUtil.printNameDate(new Date(), trainFileName+"文件创建完成");
	}
	
	/**
	 * 训练数据train 进行归一化处理并生生scale文件
	 * @param args String[] args = new String[]{"-l","0","-u","1",path+"/train"};
	 * @param scalePath  结果输出文件路径
	 */
	private static void creatScaleFile(String[] args,String scalePath) {

		DateUtil.printNameDate(new Date(), "开始归一化");
		FileOutputStream fileOutputStream =null;
		PrintStream printStream = null;
		try {
			File file = new File(scalePath);
			file.createNewFile();
			fileOutputStream = new FileOutputStream(file);
		    printStream = new PrintStream(fileOutputStream);
		    // old stream
		    PrintStream oldStream = System.out;
	        System.setOut(printStream);//重新定义system.out
			svm_scale.main(args);//开始归一化
	        System.setOut(oldStream);//回复syste.out
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(fileOutputStream!=null){
					fileOutputStream.close();
				}
				if(printStream != null){
					printStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DateUtil.printNameDate(new Date(), "归一化结束");
	}
	/**
	 * 训练c 和g的值
	 * @param string 
	 */
	private static String[] cmdGridPy(String str,String gridPath){
		DateUtil.printNameDate(new Date(), "开始计算cg的值");
		String grid = Util.exeCmd(str);
		System.out.println(str);
		Util.stringToFile(grid, gridPath,false);//将数据保存到文件里
		String gridEndLine = Util.readLastLine(new File(gridPath),null);
		gridEndLine = gridEndLine.substring(0, gridEndLine.indexOf("\n"));
		String[] cgr= gridEndLine.split(" ");
		DateUtil.printNameDate(new Date(),"cg的值计算结束：c="+cgr[0]+" γ="+cgr[1]+" CV Rate="+cgr[2]+"%");
		return cgr;
	}
	/**
	 * 创建model
	 * @param agrs
	 */
	private static void creatModeFile(String[] agrs){
		DateUtil.printNameDate(new Date(),"开始计算model");
		try {
			svm_train.main(agrs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DateUtil.printNameDate(new Date(),"计算model结束");
	}
	/**
	 * 读取归一化标标准文件 以行为单位放到字符串数组里
	 */
	private static String[] readRule(String rulePath){
		String ruleStr = Util.readFileToString(rulePath);
		String[] ruleArr = ruleStr.split(Util.getChangeRow());
		return ruleArr;
	}
}
