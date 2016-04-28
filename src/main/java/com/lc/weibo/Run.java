package com.lc.weibo;

import org.apache.log4j.PropertyConfigurator;

public class Run {
    public static void main(String[] args){
        PropertyConfigurator.configure("./src/log4j.properties");

        Weibo weibo = new Weibo();

        //weibo.outputWeiboReportSumInTimeDuration(1000L*60*60*24, 50); //50 slots with a day duration.
        //weibo.outputReportOfRelatedWeibo(1000L*60*60*24, 50);
        weibo.outputWeiboReportSumInTimeDuration(1000L*60*60, 24*50); //50 slots with a day duration.
        weibo.outputReportOfRelatedWeibo(1000L*60*60, 24*50); //output several weibos

        weibo.outputReportOfRelatedWeiboGuanzhu(1000L*60*60, 24*50);
        weibo.outputReportOfRelatedWeiboFensi(1000L*60*60, 24*50);

        weibo.outputReportOfRelatedWeiboAverageGuanzhu(1000L*60*60, 24*50);
        weibo.outputReportOfRelatedWeiboAverageFensi(1000L*60*60, 24*50);
    }
}
