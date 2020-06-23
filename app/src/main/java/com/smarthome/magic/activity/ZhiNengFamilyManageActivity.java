package com.smarthome.magic.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.smarthome.magic.R;
import com.smarthome.magic.adapter.ZhiNengFamilyManageAdapter;
import com.smarthome.magic.adapter.ZhiNengHomeListAdapter;
import com.smarthome.magic.adapter.ZhiNengRoomListAdapter;
import com.smarthome.magic.app.BaseActivity;
import com.smarthome.magic.app.ConstanceValue;
import com.smarthome.magic.app.Notice;
import com.smarthome.magic.baseadapter.baserecyclerviewadapterhelper.BaseQuickAdapter;
import com.smarthome.magic.callback.JsonCallback;
import com.smarthome.magic.config.AppResponse;
import com.smarthome.magic.config.UserManager;
import com.smarthome.magic.dialog.ZhiNengFamilyAddDIalog;
import com.smarthome.magic.get_net.Urls;
import com.smarthome.magic.model.ZhiNengFamilyManageBean;
import com.smarthome.magic.model.ZhiNengHomeListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.smarthome.magic.get_net.Urls.ZHINENGJIAJU;

public class ZhiNengFamilyManageActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.srL_smart)
    SmartRefreshLayout srLSmart;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.rl_family_add)
    RelativeLayout rl_family_add;
    private Context context = ZhiNengFamilyManageActivity.this;
    private List<ZhiNengHomeListBean.DataBean> dataBean = new ArrayList<>();
    private ZhiNengFamilyManageAdapter zhiNengFamilyManageAdapter;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_zhineng_family_manage;
    }

    @Override
    public void initImmersion() {
        mImmersionBar.with(this).statusBarColor(R.color.white).init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setLightMode(this);
        initToolbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getnet();
    }

    private void initView() {
        srLSmart.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getnet();
            }
        });
        srLSmart.setEnableLoadMore(false);
        rl_family_add.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        zhiNengFamilyManageAdapter = new ZhiNengFamilyManageAdapter(R.layout.item_zhineng_family_manage, dataBean);
        zhiNengFamilyManageAdapter.openLoadAnimation();//默认为渐显效果
        recyclerView.setAdapter(zhiNengFamilyManageAdapter);

        zhiNengFamilyManageAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ZhiNengHomeListBean.DataBean dataBean = (ZhiNengHomeListBean.DataBean) adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("family_id", dataBean.getFamily_id());
                startActivity(new Intent(context, ZhiNengFamilyManageDetailActivity.class).putExtras(bundle));
            }
        });
        _subscriptions.add(toObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Notice>() {
            @Override
            public void call(Notice message) {
                if (message.type == ConstanceValue.MSG_FAMILY_MANAGE_ADD) {
                    creatFamily(message.content.toString());
                }
            }
        }));
    }

    @Override
    public boolean showToolBar() {
        return true;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        tv_title.setText("家庭管理");
        tv_title.setTextSize(17);
        tv_title.setTextColor(getResources().getColor(R.color.black));
        mToolbar.setNavigationIcon(R.mipmap.backbutton);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_family_add:
                ZhiNengFamilyAddDIalog zhiNengFamilyAddDIalog = new ZhiNengFamilyAddDIalog(context, ConstanceValue.MSG_FAMILY_MANAGE_ADD);
                zhiNengFamilyAddDIalog.show();
                break;
        }
    }

    private void getnet() {
        //访问网络获取数据 下面的列表数据
        Map<String, String> map = new HashMap<>();
        map.put("code", "16013");
        map.put("key", Urls.key);
        map.put("token", UserManager.getManager(context).getAppToken());
        Gson gson = new Gson();
        Log.e("map_data", gson.toJson(map));
        OkGo.<AppResponse<ZhiNengHomeListBean.DataBean>>post(ZHINENGJIAJU)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<ZhiNengHomeListBean.DataBean>>() {
                    @Override
                    public void onSuccess(Response<AppResponse<ZhiNengHomeListBean.DataBean>> response) {
                        if (srLSmart != null) {
                            srLSmart.setEnableRefresh(true);
                            srLSmart.finishRefresh();
                            srLSmart.setEnableLoadMore(false);
                        }
                        dataBean.clear();
                        dataBean.addAll(response.body().data);
                        zhiNengFamilyManageAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void creatFamily(String familyName) {
        //访问网络获取数据 下面的列表数据
        Map<String, String> map = new HashMap<>();
        map.put("code", "16011");
        map.put("key", Urls.key);
        map.put("token", UserManager.getManager(context).getAppToken());
        map.put("family_name", familyName);
        Gson gson = new Gson();
        Log.e("map_data", gson.toJson(map));
        OkGo.<AppResponse<ZhiNengFamilyManageBean.DataBean>>post(ZHINENGJIAJU)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<ZhiNengFamilyManageBean.DataBean>>() {
                    @Override
                    public void onSuccess(Response<AppResponse<ZhiNengFamilyManageBean.DataBean>> response) {
                        if (response.body().msg.equals("ok")) {
                            Bundle bundle = new Bundle();
                            bundle.putString("family_id", response.body().data.get(0).getFamily_id());
                            startActivity(new Intent(context, ZhiNengFamilyManageDetailActivity.class).putExtras(bundle));
                        }
                    }
                });
    }
}