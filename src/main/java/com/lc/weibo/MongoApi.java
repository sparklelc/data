package com.lc.weibo;

import com.mongodb.*;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;

public class MongoApi {
    public static Logger logger = Logger.getLogger(MongoApi.class);
    public static MongoClient mongocli = null;

    public static HashSet<String> allUsers = new HashSet<String>();
    public static HashMap<String, Integer> userGuanzhu = new HashMap<String, Integer>();
    public static HashMap<String, Integer> userFensi = new HashMap<String, Integer>();

    static {
        try {
            mongocli = new MongoClient("localhost", 27017);
        } catch (Exception e) {
            logger.error("create mongo client fail");
        }

        DBCollection user_col = getCollection("users");
        for(DBObject obj: user_col.find()){
            String uid = (String) obj.get("id");
            allUsers.add(uid);

            try {
                String guanzhuStr = (String) obj.get("friend");
                if (!guanzhuStr.equals("")) {
                    Integer guanzhu = Integer.parseInt(guanzhuStr);
                    userGuanzhu.put(uid, guanzhu);
                } else {
                    userGuanzhu.put(uid, 0);
                }

                String fensiStr = (String) obj.get("follow");
                if(!fensiStr.equals("")) {
                    Integer fensi = Integer.parseInt(fensiStr);
                    userFensi.put(uid, fensi);
                } else {
                    userFensi.put(uid, 0);
                }
            } catch (Exception e) {
                logger.error(uid);
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static int getUserFollowerNum(DBCollection col, String uid) {
        DBObject condition = new BasicDBObject();
        condition.put("id", uid);
        if (((String) col.findOne(condition).get("follow")).equals(""))
            return 1;
        return Integer.parseInt((String) col.findOne(condition).get("follow"));
    }

    public static DBCollection getCollection(String colName){
        DB db = null;
        DBCollection col = null;
        try {
            db = mongocli.getDB("weiboSina3");
            col = db.getCollection(colName);
        } catch (Exception e) {
            logger.error("fail to connect to mongo-client or db");
            System.exit(0);
        }
        return col;
    }
}
