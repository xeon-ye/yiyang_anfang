package com.smarthome.magic.activity.tongcheng58;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.smarthome.magic.R;
import com.smarthome.magic.activity.shuinuan.Y;
import com.smarthome.magic.activity.tongcheng58.adapter.TcHomeItemAdapter;
import com.smarthome.magic.activity.tongcheng58.model.TcHomeModel;
import com.smarthome.magic.adapter.NewsFragmentPagerAdapter;
import com.smarthome.magic.app.App;
import com.smarthome.magic.basicmvp.BaseFragment;
import com.smarthome.magic.callback.JsonCallback;
import com.smarthome.magic.config.AppResponse;
import com.smarthome.magic.config.PreferenceHelper;
import com.smarthome.magic.config.UserManager;
import com.smarthome.magic.get_net.Urls;
import com.smarthome.magic.view.CustomViewPager;
import com.smarthome.magic.view.magicindicator.MagicIndicator;
import com.smarthome.magic.view.magicindicator.ViewPagerHelper;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.CommonNavigator;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import com.smarthome.magic.view.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TongchengTabHomeFragment extends BaseFragment {


    @BindView(R.id.magic_title)
    MagicIndicator magic_title;
    @BindView(R.id.viewpager)
    CustomViewPager viewpager;
    @BindView(R.id.rv_list)
    RecyclerView rv_list;
    @BindView(R.id.vpg_tag_list)
    ViewPager vpg_tag_list;
    @BindView(R.id.ll_tag_list)
    LinearLayout ll_tag_list;
    @BindView(R.id.smartRefreshLayout)
    SmartRefreshLayout smartRefreshLayout;

    private List<TcHomeModel.DataBean.ShopTypeListBean> shop_type_list = new ArrayList<>();
    private ArrayList<Fragment> messageListFragments = new ArrayList<>();
    private String x_jingdu;
    private String y_weidu;
    private int page_number;
    private TcHomeModel.DataBean homeBean;

    private List<TcHomeModel.DataBean.ShopListBean> shopListBeans = new ArrayList<>();
    private TcHomeItemAdapter itemAdapter;
    private List<TcHomeModel.DataBean.IconListBean> iconList;
    private String ir_industry_type;
    private boolean isFirst;

    @Override
    protected void initLogic() {

    }

    @Override
    protected int getLayoutRes() {
        return R.layout.tongcheng_frag_main_home;
    }

    @Override
    protected void initView(View rootView) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        initStart();
        return rootView;
    }

    private void initStart() {
        x_jingdu = PreferenceHelper.getInstance(getActivity()).getString(App.JINGDU, "");
        y_weidu = PreferenceHelper.getInstance(getActivity()).getString(App.WEIDU, "");
        page_number = 0;
        isFirst = true;
        ir_industry_type = "1851";

        initAdapter();
        initSM();
        getData();
    }

    private void initSM() {
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getData();
            }
        });

        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                lordData();
            }
        });
    }

    private void initAdapter() {
        itemAdapter = new TcHomeItemAdapter(R.layout.tongcheng_item_home_item, shopListBeans);
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setAdapter(itemAdapter);
        itemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String ir_id = shopListBeans.get(position).getIr_id();
                ShangjiaDetailsActivity.actionStart(getContext(),ir_id);
            }
        });


        View view = View.inflate(getContext(), R.layout.empty_view, null);
        ImageView noneImage = view.findViewById(R.id.iv_image);
        noneImage.setBackgroundResource(R.mipmap.shop_pic_none);
        itemAdapter.setEmptyView(view);
    }

    private void initVpg() {
        isFirst = false;
        shop_type_list = homeBean.getShop_type_list();
        for (int i = 0; i < shop_type_list.size(); i++) {
            TcHomeModel.DataBean.ShopTypeListBean shopTypeListBean = shop_type_list.get(i);
            Bundle data = new Bundle();
            data.putString("title", shopTypeListBean.getIr_industry_type_name());
            TongchengHomeItemFragment newfragment = new TongchengHomeItemFragment();
            newfragment.setArguments(data);
            messageListFragments.add(newfragment);
        }
        NewsFragmentPagerAdapter mAdapetr = new NewsFragmentPagerAdapter(getChildFragmentManager(), messageListFragments);
        viewpager.setAdapter(mAdapetr);

        initMagicIndicator();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void initMagicIndicator() {
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return shop_type_list == null ? 0 : shop_type_list.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(Y.getColor(R.color.text_color3));
                simplePagerTitleView.setSelectedColor(Y.getColor(R.color.text_red));
                simplePagerTitleView.setText(shop_type_list.get(index).getIr_industry_type_name());
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setNet(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setColors(Y.getColor(R.color.text_red));
                return linePagerIndicator;
            }
        });
        magic_title.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magic_title, viewpager);
    }

    private void setNet(int index) {
        viewpager.setCurrentItem(index);
        ir_industry_type = shop_type_list.get(index).getIr_industry_type();
        getData();
    }

    private void getData() {
        page_number = 0;
        Map<String, String> map = new HashMap<>();
        map.put("code", "17004");
        map.put("key", Urls.key);
        map.put("token", UserManager.getManager(getContext()).getAppToken());
        map.put("ir_industry_type", ir_industry_type);
        map.put("x", x_jingdu);
        map.put("y", y_weidu);
        map.put("page_number", page_number + "");
        Gson gson = new Gson();
        OkGo.<AppResponse<TcHomeModel.DataBean>>post(Urls.TONG_CHENG)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<TcHomeModel.DataBean>>() {
                    @Override
                    public void onSuccess(final Response<AppResponse<TcHomeModel.DataBean>> response) {
                        if (response.body().msg_code.equals("0000")) {
                            homeBean = response.body().data.get(0);
                            if (isFirst) {
                                initVpg();
                                initTag();
                            }

                            shopListBeans = homeBean.getShopList();
                            itemAdapter.setNewData(shopListBeans);
                            itemAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        smartRefreshLayout.finishRefresh();
                        smartRefreshLayout.finishLoadMore();
                    }
                });
    }

    private void initTag() {
        iconList = homeBean.getIconList();
        int size = iconList.size();
        int count = 0;
        ll_tag_list.removeAllViews();
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        List<View> views = new ArrayList<>();

        for (int i = 0; i < size; i = i + 10) {

            List<TcHomeModel.DataBean.IconListBean> listBeans = new ArrayList<>();
            if (i + 10 <= size) {
                for (int j = i; j < i + 10; j++) {
                    listBeans.add(iconList.get(j));
                }
            } else {
                for (int j = i; j < size; j++) {
                    listBeans.add(iconList.get(j));
                }
            }
            View view = View.inflate(getContext(), R.layout.tongcheng_item_home_tag_dian, null);
            View view_dian = view.findViewById(R.id.view_dian);
            if (i == 0) {
                view_dian.setBackgroundResource(R.drawable.bg_tongcheng_home_dian_sel);
            }
            ll_tag_list.addView(view);
            views.add(view_dian);

            TongchengTagItemFragment fragment = new TongchengTagItemFragment(listBeans);
            fragmentList.add(fragment);
            count++;
        }

        NewsFragmentPagerAdapter pagerAdapter = new NewsFragmentPagerAdapter(getChildFragmentManager(), fragmentList);
        vpg_tag_list.setAdapter(pagerAdapter);
        vpg_tag_list.setOffscreenPageLimit(count);
        vpg_tag_list.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < views.size(); i++) {
                    views.get(i).setBackgroundResource(R.drawable.bg_tongcheng_home_dian_nor);
                }
                views.get(position).setBackgroundResource(R.drawable.bg_tongcheng_home_dian_sel);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void lordData() {
        page_number++;
        Map<String, String> map = new HashMap<>();
        map.put("code", "17004");
        map.put("key", Urls.key);
        map.put("token", UserManager.getManager(getContext()).getAppToken());
        map.put("ir_industry_type", ir_industry_type);
        map.put("x", x_jingdu);
        map.put("y", y_weidu);
        map.put("page_number", page_number + "");
        Gson gson = new Gson();
        OkGo.<AppResponse<TcHomeModel.DataBean>>post(Urls.TONG_CHENG)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<TcHomeModel.DataBean>>() {
                    @Override
                    public void onSuccess(final Response<AppResponse<TcHomeModel.DataBean>> response) {
                        if (response.body().msg_code.equals("0000")) {
                            homeBean = response.body().data.get(0);
                            List<TcHomeModel.DataBean.ShopListBean> shopList = homeBean.getShopList();
                            shopListBeans.addAll(shopList);
                            itemAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        smartRefreshLayout.finishRefresh();
                        smartRefreshLayout.finishLoadMore();
                    }
                });
    }
}
