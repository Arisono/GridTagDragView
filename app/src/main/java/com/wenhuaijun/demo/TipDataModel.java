package com.wenhuaijun.demo;

import com.wenhuaijun.easytagdragview.bean.SimpleTitleTip;
import com.wenhuaijun.easytagdragview.bean.Tip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/30 0030.
 */
public class TipDataModel {
    private static String[] dragTips ={"魅力13","魅力12","魅力11","魅力9","魅力8","魅力7","魅力6","魅力5","魅力4","魅力3","魅力2","魅力1","头条","热点","国际足球最新","体育","财经","科技","段子","轻松一刻","军事","历史","游戏","时尚","NBA"
            ,"漫画","社会","中国足球","手机"};
    private static String[] addTips ={"数码","移动互联","云课堂","家居","旅游","健康","读书","跑步","情感","政务","艺术","博客"};
    public static List<Tip>  getDragTips(){
        List<Tip> result = new ArrayList<>();
        for(int i=0;i<dragTips.length;i++){
            String temp =dragTips[i];
            SimpleTitleTip tip = new SimpleTitleTip();
            tip.setTip(temp);
            tip.setId(i);
            if (i==0){
                tip.setIsSelected(true);
            }else{
                tip.setIsSelected(false);
            }
            result.add(tip);
        }
        return result;
    }
    public static List<Tip> getAddTips(){
        List<Tip> result = new ArrayList<>();
        for(int i=0;i<addTips.length;i++){
            String temp =addTips[i];
            SimpleTitleTip tip = new SimpleTitleTip();
            tip.setTip(temp);
            tip.setId(i+dragTips.length);
            result.add(tip);
        }
        return result;
    }
}
