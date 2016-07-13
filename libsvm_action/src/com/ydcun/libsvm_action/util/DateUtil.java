package com.ydcun.libsvm_action.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DateUtil {
	public static String getString(Date date,String format){
		if(date==null){
			return null;
		}
		SimpleDateFormat sf = new SimpleDateFormat(format);
		return sf.format(date);
	}
	
	public static String getString(Date date){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.format(date);
	}
	/**
	 * 某个任务的开始时间结束时间，运行时间
	 * @param start
	 * @param end
	 * @param name
	 */
	public static List<String> printStartEnd(Date start,Date end,String name){
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		list.add(name+"开始时间："+sf.format(start));
		list.add(name+"结束时间："+sf.format(end)+" 用时："+(end.getTime()-start.getTime())+"毫秒");
		return list;
	}
	/**
	 * 打印某个任务的时间
	 * @param start
	 * @param end
	 * @param name
	 */
	public static void printNameDate(Date date,String name){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		System.out.println("----"+sf.format(date)+" "+name+"------");
	}
	/**
	 * 将map<string,Double> 根据double值进行降序排序
	 * @param listmap
	 */
	public static void sortMapStringDouble(List<Map.Entry<String, Double>> listmap){
		if(listmap==null){
			return;
		}
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(listmap, new Comparator<Map.Entry<String, Double>>(){  
			@Override  
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
            }  
        });  
	}
	public static void main(String[] args) {
		Map<String,Double> map = new HashMap<String,Double>();
		map.put("d",0.31d);
		map.put("dd",0.11d);
		map.put("da",0.04d);
		map.put("d3",0.8d);
		map.put("d4",0.91d);
		map.put("d7",0.2d);
		ArrayList<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());
		sortMapStringDouble(list);
		System.out.println("");
	}
	/**
	 * 将map<string,Double> 根据double值进行降序排序
	 * @param listmap
	 */
	public static void sortMapIntegerDouble(List<Map.Entry<Integer, Double>> listmap){
		if(listmap==null){
			return;
		}
		Collections.sort(listmap, new Comparator<Map.Entry<Integer, Double>>(){  
			@Override  
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {  
				return (int) (o1.getValue() - o2.getValue());  
			}  
		});  
	}
}
