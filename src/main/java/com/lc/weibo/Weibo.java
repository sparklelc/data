package com.lc.weibo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.text.StyledEditorKit.BoldAction;

import com.mongodb.*;

import org.apache.log4j.*;
import org.bson.BSONObject;

public class Weibo {
	public static Logger logger = Logger.getLogger(Weibo.class);
	HashSet<String> allUsers = new HashSet<String>();
	File inputWeiboFile = new File("input_weibo.txt");
	File relatedWeiboFile = new File("related_weibo.txt");
	
	public Weibo(){
		DBCollection user_col = getCollection("users");
		for(DBObject obj: user_col.find()){
			String uid = (String) obj.get("id");
			allUsers.add(uid);
		}
	}
	
	public DBCollection getCollection(String colName){
		DB db = null;
		DBCollection col = null;
		try{
			MongoClient mongocli = new MongoClient("localhost", 27017);
			db = mongocli.getDB("weiboSina3");
			col = db.getCollection(colName);
		}catch(Exception e){
			logger.error("fail to connect to mongo-client or db");
			System.exit(0);
		}
		return col;
	}
	
	
	// from file 
	// the last slot store all other reports.
	public void outputWeiboReportSumInTimeDuration(long duration, int storeNum){
		try{
			Scanner sc = new Scanner(new FileInputStream(inputWeiboFile));			
			
			//each weibo
			while(sc.hasNext()){ 
				String url = sc.next();
				int[] store = new int[storeNum];
				// example: http://weibo.com/1749127163/BiQgAqoOZ;雷军冰桶
				String weiboId = url.split("/")[4].split(";")[0];
				String durationStr = new Long(duration/1000L).toString();	
				HashSet<String> hash = new HashSet<String>();
				
				DBCollection weiboCol = getCollection("weibo");
				BasicDBObject condition = new BasicDBObject();
				condition.put("weibo", weiboId);
				long startDate = Long.valueOf((String) weiboCol.findOne(condition).get("date"));
				logger.info("start date: " + startDate);
				
				int count = 0; // the total number of repost
				DBCollection col = getCollection(weiboId);
				
				for(DBObject obj: col.find().sort(new BasicDBObject("date", 1))){
					String dateStr = (String) obj.get("date");
					String uid = (String) obj.get("uid");
					long date = Long.valueOf(dateStr);
					
					if(!hash.contains(uid) && allUsers.contains(uid)){
						hash.add((String) obj.get("uid"));
						int index = (int) ((date - startDate) / duration);
						if(index >= storeNum){
							index = storeNum-1;
						}
						
						store[index]++;
						count++;
					}
				}
				
				FileWriter writer = new FileWriter(new File("Duration_"+weiboId+"_"+durationStr+"_"+storeNum+".txt"), false);
				for(int i=0; i<store.length; i++){
					writer.write(i+"\t"+store[i]+"\r\n");
				}
				writer.close();
				
				logger.info("count: "+count);
			}
			sc.close();
		}catch(Exception e){
			logger.error("can not find file: input_weibo.txt");
			System.exit(0);
		}		
	}
	
	// make sure that the related weibo is sorted by time.
	public void outputReportOfRelatedWeibo(long duration, int slotsNum){
		try{
			Scanner sc = new Scanner(new FileInputStream(relatedWeiboFile));
			while(sc.hasNextInt()){
				int relatedWeiboNum = sc.nextInt();
				
				ArrayList<String> allWeibo = new ArrayList<String>(relatedWeiboNum);
				ArrayList<int[]> allSlots = new ArrayList<int[]>(relatedWeiboNum);
				long minStartTime = Long.MAX_VALUE;
				long maxStartTime = Long.MIN_VALUE;
				
				for(int i=0; i<relatedWeiboNum; i++){
					String url = sc.next();
					// example: http://weibo.com/1749127163/BiQgAqoOZ;雷军冰桶
					String weiboId = url.split("/")[4].split(";")[0];
					allWeibo.add(weiboId);
					
					DBCollection weiboCol = getCollection("weibo");
					BasicDBObject condition = new BasicDBObject();
					condition.put("weibo", weiboId);
					long startDate = Long.valueOf((String) weiboCol.findOne(condition).get("date"));
					logger.info("weibo:" + weiboId + "start date: " + startDate);
					minStartTime = Math.min(startDate, minStartTime);
					maxStartTime = Math.max(startDate, maxStartTime);
				}
				
				int allSlotsNum = (int) Math.ceil((maxStartTime - minStartTime) / (double) duration) + slotsNum;
				for(int i=0; i<relatedWeiboNum; i++){
					int count = 0; // the total number of repost
					allSlots.add(new int[allSlotsNum]);
					
					HashSet<String> hash = new HashSet<String>();
					DBCollection col = getCollection(allWeibo.get(i));
	
					for(DBObject obj: col.find().sort(new BasicDBObject("date", 1))){
						String dateStr = (String) obj.get("date");
						String uid = (String) obj.get("uid");
						long date = Long.valueOf(dateStr);
						
						if(!hash.contains(uid) && allUsers.contains(uid)){
							hash.add((String) obj.get("uid"));
							int index = (int) ((date - minStartTime) / duration);
							if(index >= allSlotsNum){
								index = allSlotsNum-1;
							}
							
							allSlots.get(i)[index]++;
							count++;
						}
					}							
					logger.info("weiboID:" + allWeibo.get(i) + "count: "+count);
				}

				String durationStr = new Long(duration/1000L).toString();	
				StringBuilder fileName = new StringBuilder();
				for(String weiboID: allWeibo){
					fileName.append(weiboID+"_");
				}
				fileName.append("%_" + durationStr + "_" + allSlotsNum + ".txt");
				FileWriter writer = new FileWriter(new File(fileName.toString()), false);
				for(int i=0; i<allSlotsNum; i++){
					Date date = new Date(minStartTime + i*duration);
					String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(date);
					writer.write(dateStr);
					for(int[] slots:allSlots){
						writer.write("\t"+slots[i]);
					}
					writer.write("\r\n");
				}
				writer.close();			
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error("can not find file: related_weibo.txt");
			System.exit(0);
		}
	}
	
	public static void main(String[] args){
		PropertyConfigurator.configure("./src/log4j.properties");
		
		Weibo weibo = new Weibo();
		//weibo.outputWeiboReportSumInTimeDuration(1000L*60*60*24, 50); //50 slots with a day duration.
		//weibo.outputReportOfRelatedWeibo(1000L*60*60*24, 50);
		weibo.outputWeiboReportSumInTimeDuration(1000L*60*60, 24*50); //50 slots with a day duration.
		weibo.outputReportOfRelatedWeibo(1000L*60*60, 24*50);
	}
}
