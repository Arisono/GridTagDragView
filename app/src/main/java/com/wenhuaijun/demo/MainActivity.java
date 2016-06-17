package com.wenhuaijun.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wenhuaijun.easytagdragview.EasyTipDragView;
import com.wenhuaijun.easytagdragview.bean.SimpleTitleTip;
import com.wenhuaijun.easytagdragview.bean.Tip;
import com.wenhuaijun.easytagdragview.R;
import com.wenhuaijun.easytagdragview.widget.TipItemView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity /*implements AbsTipAdapter.DragDropListener, TipItemView.OnSelectedListener, TipItemView.OnDeleteClickListener */{
    private EasyTipDragView easyTipDragView;
    private Button btn;

    private Context ct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ct=this;
        btn =(Button)findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (window != null) {
                    if (window.isShowing()) {
                        window.dismiss();
                    } else {
                        showPopwindow(btn);
                    }
                } else {
                    showPopwindow(btn);
                }
                Toast.makeText(MainActivity.this, "点击", Toast.LENGTH_LONG).show();
            }

        });
    }
    PopupWindow window;
    private void showPopwindow(View mView) {
        if (window==null) {
            // 利用layoutInflater获得View
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.pop_tag_show, null);
            easyTipDragView =(EasyTipDragView)view.findViewById(R.id.easy_tip_drag_view);
            window = new PopupWindow(view,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            //设置已包含的标签数据
            easyTipDragView.setAddData(TipDataModel.getAddTips());
            //设置可以添加的标签数据
            easyTipDragView.setDragData(TipDataModel.getDragTips());
            //在easyTipDragView处于非编辑模式下点击item的回调（编辑模式下点击item作用为删除item）
            easyTipDragView.setSelectedListener(new TipItemView.OnSelectedListener() {
                @Override
                public void onTileSelected(Tip entity, int position, View view) {
                    List<Boolean> mSelected= easyTipDragView.getDragTipAdapter().getmTextSelected();
                    for (int i=0;i<mSelected.size();i++){
                        if (i!=position){
                            easyTipDragView.getDragTipAdapter().getmTextSelected().set(i,false);
                        }else{
                            easyTipDragView.getDragTipAdapter().getmTextSelected().set(i,true);
                        }
                    }
                    easyTipDragView.getDragTipAdapter().notifyDataSetChanged();
                }
            });
            //设置每次数据改变后的回调（例如每次拖拽排序了标签或者增删了标签都会回调）
            easyTipDragView.setDataResultCallback(new EasyTipDragView.OnDataChangeResultCallback() {
                @Override
                public void onDataChangeResult(ArrayList<Tip> tips) {
                    Log.i("heheda", tips.toString());
                }
            });
            //设置点击“确定”按钮后最终数据的回调
            easyTipDragView.setOnCompleteCallback(new EasyTipDragView.OnCompleteCallback() {
                @Override
                public void onComplete(ArrayList<Tip> tips) {
                    toast("最终数据：" + tips.toString());
                    window.dismiss();
                    btn.setVisibility(View.VISIBLE);
                }
            });

        }else {
            easyTipDragView =(EasyTipDragView)window.getContentView().findViewById(R.id.easy_tip_drag_view);
        }

        easyTipDragView.open();

        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        window.setFocusable(true); // 设置PopupWindow可获得焦点
        window.setTouchable(true); // 设置PopupWindow可触摸
        window.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
        window.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.mypopwindow_anim_style);
        window.showAsDropDown(mView);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    /*private List<Tip> obtainData() {
            List<Tip> list = new ArrayList<>();
            for (int i = 0; i <= 24; i++) {
                SimpleTitleTip entry = new SimpleTitleTip();
                entry.setId(i);
                entry.setTip(i + " Item");
                list.add(entry);
            }
            return list;
        }

        private List<Tip> obtainAddData() {
            List<Tip> list = new ArrayList<>();
            for (int i = 25; i <= 35; i++) {
                SimpleTitleTip entry = new SimpleTitleTip();
                entry.setId(i);
                entry.setTip(i + " Item");
                list.add(entry);
            }
            return list;
        }*/
    public void toast(String str){
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            //点击返回键
            case KeyEvent.KEYCODE_BACK:
                //判断easyTipDragView是否已经显示出来
                if(easyTipDragView.isOpen()){
                    if(!easyTipDragView.onKeyBackDown()){
                        btn.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
                //....自己的业务逻辑

                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
