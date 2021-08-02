package com.example.mobilevideo.modules.utils;

import com.example.mobilevideo.modules.bean.VideoBean;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    public static List<VideoBean> getVideoList() {
        List<VideoBean> videoList = new ArrayList<>();

        videoList.add(new VideoBean("CCTV中国中央电视台-1 综合",
                "http://epg.51zmt.top:8000/tb1/CCTV/CCTV1.png",
                "http://183.207.248.71:80/cntv/live1/CCTV-1/cctv-1"));

        videoList.add(new VideoBean("CCTV中国中央电视台-2 财经",
                "http://epg.51zmt.top:8000/tb1/CCTV/CCTV2.png",
                "http://183.207.248.71:80/cntv/live1/CCTV-2/cctv-2"));

        videoList.add(new VideoBean("CCTV中国中央电视台-3 综艺",
                "http://epg.51zmt.top:8000/tb1/CCTV/CCTV3.png",
                "http://183.207.248.71:80/cntv/live1/CCTV-3/cctv-3"));

        videoList.add(new VideoBean("CCTV中国中央电视台-4 中文国际",
                "http://epg.51zmt.top:8000/tb1/CCTV/CCTV4.png",
                "http://183.207.248.71:80/cntv/live1/CCTV-4/cctv-4"));

        videoList.add(new VideoBean("CCTV中国中央电视台-5 体育",
                "http://epg.51zmt.top:8000/tb1/CCTV/CCTV5.png",
                "http://183.207.248.71:80/cntv/live1/CCTV-5/cctv-5"));

        return videoList;
    }
}
