package com.yiyang.cn.adapter;

import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yiyang.cn.R;
import com.yiyang.cn.baseadapter.baserecyclerviewadapterhelper.BaseQuickAdapter;
import com.yiyang.cn.baseadapter.baserecyclerviewadapterhelper.BaseViewHolder;
import com.yiyang.cn.model.TaoKeDetailList;

import java.util.List;

public class TaoKeListAdapter2 extends BaseQuickAdapter<TaoKeDetailList.DataBean, BaseViewHolder> {

    public TaoKeListAdapter2(int layoutResId, @Nullable List<TaoKeDetailList.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TaoKeDetailList.DataBean item) {
        helper.addOnClickListener(R.id.constrain);
//        if (helper.getAdapterPosition() % 2 == 1) {
//
//            ((RelativeLayout.LayoutParams) helper.getView(R.id.constrain).getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT); //沉于父容器右边
//          // setMargins(helper.getView(R.id.constrain),0, 8, 0, 0);
//        } else {
//          //  setMargins(helper.getView(R.id.constrain), 0, 8, 0, 0);
//
//            ((RelativeLayout.LayoutParams) helper.getView(R.id.constrain).getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT); //沉于父容器右边



//        }

      //  RelativeLayout rlMain = helper.getView(R.id.rl_main);

        Log.i("getPostion()", helper.getPosition() + "");
        Log.i("getLayoutPosition()", helper.getLayoutPosition() + "");
        Log.i("getAdapterPosition()", helper.getAdapterPosition() + "");
        Glide.with(mContext).load(item.getPict_url()).into((ImageView) helper.getView(R.id.iv_product));
        helper.setText(R.id.tv_title, item.getTitle());//标题
        helper.setText(R.id.tv_yishou, "已售" + item.getVolume() + "");//已售
        helper.setText(R.id.tv_xianjia, item.getZk_final_price());//现价
        helper.setText(R.id.tv_yuanjia, item.getReserve_price());//原价
        TextView tvYuanjia = helper.getView(R.id.tv_yuanjia);
        tvYuanjia.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线
        helper.setText(R.id.rtv_youhuijuan, item.getCoupon_amount() + "元券");//优惠券
        if (item.getUser_type() == 0) {
            //0淘宝
            ImageView ivProduct = helper.getView(R.id.biaozhi);
            ivProduct.setBackgroundResource(R.mipmap.taokeshop_icon_tianmao);
        } else if (item.getUser_type() == 1) {
            //1天猫
            ImageView ivProduct = helper.getView(R.id.biaozhi);
            ivProduct.setBackgroundResource(R.mipmap.taokeshop_icon_taobao);
        }

    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
