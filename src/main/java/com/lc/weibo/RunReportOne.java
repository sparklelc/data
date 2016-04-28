package com.lc.weibo;

public class RunReportOne {
    public static void main(String[] args) {
        ReportOne run = new ReportOne();
        run.createReports(1000L*60*60*24, 50);
    }
}
