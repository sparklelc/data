package com.lc.weibo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;


// strategy-one
public class ReportOne {
    public static Logger logger = MongoApi.logger;

    // file content like:
    //*url1
    //*url2
    //*#
    File inputFile = new File("inputOne.txt");


    // just for two weibos
    public void createReports(long duration, int slotsNum) {
        try {
            Scanner sc = new Scanner(new FileInputStream(inputFile));
            int i=1;
            while (sc.hasNext()) {
                String url1 = sc.next();
                String url2 = sc.next();
                if (!(sc.next().charAt(0) == '#')) {
                    logger.error("data format error");
                    System.exit(0);
                }

                this.createOneReport(url1, url2, duration, slotsNum, i, "");
                this.createOneReport(url2, url1, duration, slotsNum, i, "_");
                i++;
            }
            sc.close();
        } catch (Exception e) {
            logger.error("can not find file: inputOne.txt");
            System.exit(0);
        }
    }

    // just for two weibo
    // the firstName is important because we count according to the sequence of names.
    public void createOneReport(String url1, String url2, long duration, int slotsNum, int num, String startStr) {
        ArrayList<int[]> list = new ArrayList<int[]>();
        list.add(0, new int[slotsNum]); // date
        list.add(1, new int[slotsNum]); // repost
        list.add(2, new int[slotsNum]);
        list.add(3, new int[slotsNum]); // follower
        list.add(4, new int[slotsNum]);

        // example: http://weibo.com/1749127163/BiQgAqoOZ;雷军冰桶
        String weiboId1 = url1.split("/")[4].split(";")[0];
        String weiboId2 = url2.split("/")[4].split(";")[0];

        DBCollection weiboCol = MongoApi.getCollection("weibo");
        BasicDBObject condition = new BasicDBObject();
        condition.put("weibo", weiboId1);
        long startDate = Long.valueOf((String) weiboCol.findOne(condition).get("date"));

        try {
            countRepostAndFollower(weiboId1, list.get(1), list.get(3), startDate, duration);
            countRepostAndFollower(weiboId2, list.get(2), list.get(4), startDate, duration);

            FileWriter writer = new FileWriter(new File(startStr + "One_" + num + "_" + Long.toString(duration / 1000L) + "_" + slotsNum + ".txt"), false);
            for (int i = 0; i < slotsNum; i++) {
                Date date = new Date(startDate + i * duration);
                String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(date);
                writer.write(dateStr + "\t" + list.get(1)[i] + "\t" + list.get(2)[i] + "\t" + list.get(3)[i] + "\t" + list.get(4)[i] + "\t" + "\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }


    // count the report number from a startDate.
    public void countRepost(String colName, int[] result, long startDate, long duration) {
        HashSet<String> uidSet = new HashSet<String>();

        int slotsNum = result.length;
        DBCollection col = MongoApi.getCollection(colName);

        int count = 0;
        for(DBObject obj: col.find().sort(new BasicDBObject("date", 1))){
            String dateStr = (String) obj.get("date");
            String uid = (String) obj.get("uid");
            long date = Long.valueOf(dateStr);

            if (!uidSet.contains(uid) && MongoApi.allUsers.contains(uid)) {
                uidSet.add((String) obj.get("uid"));
                int index = (int) ((date - startDate) / duration);
                if (index >= slotsNum){
                    index = slotsNum-1;
                } else if (index < 0) {
                    continue;
                }
                result[index]++;
                count++;
            }
        }
        logger.info("the total report number of weibo:" + colName + "from startDate: " + startDate + " is: " + count);
    }


    // count the report number and follower's number from a startDate.
    public void countRepostAndFollower(String colName, int[] reportResult, int[] followerResult, long startDate, long duration) {
        HashSet<String> uidSet = new HashSet<String>();

        int slotsNum = reportResult.length;
        DBCollection col = MongoApi.getCollection(colName);

        int countReport = 0;
        int countFollower = 0;
        for(DBObject obj: col.find().sort(new BasicDBObject("date", 1))){
            String dateStr = (String) obj.get("date");
            String uid = (String) obj.get("uid");
            long date = Long.valueOf(dateStr);

            if (!uidSet.contains(uid) && MongoApi.allUsers.contains(uid)) {
                uidSet.add((String) obj.get("uid"));
                int index = (int) ((date - startDate) / duration);
                if (index >= slotsNum) {
                    index = slotsNum-1;
                } else if (index < 0) {
                    continue;
                }
                reportResult[index]++;
                countReport++;

                int followerNum = MongoApi.getUserFollowerNum(MongoApi.getCollection("users"), uid);
                followerResult[index] += followerNum;
                countFollower += followerNum;
            }
        }

        logger.info("the total report number of weibo:" + colName + "from startDate: " + startDate + " is: " +
                countReport + " and the followers' number is:" + countFollower);
    }
}
