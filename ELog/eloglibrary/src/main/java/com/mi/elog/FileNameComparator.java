package com.mi.elog;

import java.util.Comparator;


/**
 *   
 * @描述：日期比较工具
 * @author：Michelle_Hong 
 * @创建时间：2015年6月1日下午4:49:35
 * @see
 * @param <String> 日期 格式为 yyyyMMddHHmmss
 */
public class FileNameComparator implements Comparator<String> {
	
	private String filenamePrex;

	
	public FileNameComparator(String filenameprex){
		this.filenamePrex = filenameprex;
	}

	/**
	 * lhs < rhs return -1
	 */
	@Override
	public int compare(String lhs, String rhs) {
		// TODO Auto-generated method stub
	  lhs = lhs.substring(filenamePrex.length()+1, filenamePrex.length()+9);
	  rhs = rhs.substring(filenamePrex.length()+1, filenamePrex.length()+9);
		
	  String year_1 = lhs.substring(0, 3);
	  String year_2 = rhs.substring(0, 3);
	  
	  String month_1 = lhs.substring(4, 5);
	  String month_2 = rhs.substring(4, 5);
	  
	  String day_1 = lhs.substring(6, 7);
	  String day_2 = rhs.substring(6, 7);
	  
	  if(Integer.parseInt(year_1) > Integer.parseInt(year_2) ){
		  return 1;
	  }
	  if(Integer.parseInt(year_1) < Integer.parseInt(year_2)){
		  return -1;
	  }
	  if(Integer.parseInt(year_1) == Integer.parseInt(year_2)){
		  
		  if(Integer.parseInt(month_1) > Integer.parseInt(month_2)){
			  return 1;
		  }
		  
		  if(Integer.parseInt(month_1) < Integer.parseInt(month_2)){
			  return -1;
		  }
		  
		  if(Integer.parseInt(month_1) == Integer.parseInt(month_2)){
			  
			  if(Integer.parseInt(day_1) > Integer.parseInt(day_2)){
				  return 1;
			  }
			  
			  if(Integer.parseInt(day_1) < Integer.parseInt(day_2)){
				  return -1;
			  }
			  
			  if(Integer.parseInt(day_1) == Integer.parseInt(day_2)){
				  return 0;
			  }
			  
		  }
		 
	  }
	  
	  return 0;
	  
	/*  if(Integer.parseInt(year_1) < Integer.parseInt(year_2)){
		  return -1;
	  }else if(Integer.parseInt(year_1) > Integer.parseInt(year_2)){
		  return 1;
	  }else{
		  if(Integer.parseInt(month_1) < Integer.parseInt(month_2)){
			  return -1;
		  }else if(Integer.parseInt(month_1) > Integer.parseInt(month_2)){
			  return 1;
		  }else{
			  if(Integer.parseInt(day_1) < Integer.parseInt(day_2)){
				  return -1;
			  }else if(Integer.parseInt(day_1)  > Integer.parseInt(day_2)){
				  return 1;
			  }else{
				  return 0;
			  }
			  
		  }
		  
	  }*/
	  
	  
	}

	
	
}
