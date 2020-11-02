package com.smarthome.magic.activity.zckt;import android.content.Context;import android.content.Intent;import android.graphics.Color;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.util.Log;import android.view.View;import android.widget.ImageView;import android.widget.RelativeLayout;import android.widget.TextView;import android.widget.Toast;import com.jaeger.library.StatusBarUtil;import com.rairmmd.andmqtt.AndMqtt;import com.rairmmd.andmqtt.MqttPublish;import com.rairmmd.andmqtt.MqttSubscribe;import com.rairmmd.andmqtt.MqttUnSubscribe;import com.scwang.smartrefresh.layout.SmartRefreshLayout;import com.scwang.smartrefresh.layout.api.RefreshLayout;import com.scwang.smartrefresh.layout.listener.OnRefreshListener;import com.smarthome.magic.R;import com.smarthome.magic.activity.SheBeiSetActivity;import com.smarthome.magic.activity.shuinuan.ShuinuanBaseNewActivity;import com.smarthome.magic.activity.shuinuan.ShuinuanMainActivity;import com.smarthome.magic.activity.shuinuan.Y;import com.smarthome.magic.app.App;import com.smarthome.magic.app.ConstanceValue;import com.smarthome.magic.app.Notice;import com.smarthome.magic.config.MyApplication;import com.smarthome.magic.config.PreferenceHelper;import com.smarthome.magic.dialog.newdia.TishiDialog;import com.smarthome.magic.get_net.Urls;import com.smarthome.magic.util.DoMqttValue;import com.smarthome.magic.util.SoundPoolUtils;import org.eclipse.paho.client.mqttv3.IMqttActionListener;import org.eclipse.paho.client.mqttv3.IMqttToken;import androidx.annotation.NonNull;import rx.android.schedulers.AndroidSchedulers;import rx.functions.Action1;import static com.smarthome.magic.config.MyApplication.CAR_NOTIFY;public class AirConditionerActivity extends ShuinuanBaseNewActivity implements View.OnClickListener {    private Context context = AirConditionerActivity.this;    private TextView tv_connect_device, tv_temperature, tv_device_mode, tv_device_state;    private TextView tv_hj_temperature, tv_device_fengsu, tv_alert, tv_clean_alert, tv_lamp_on, tv_lamp_off;    private RelativeLayout rl_device_state, rl_alert, rl_switch;    private TextView tv_change_mode, tv_change_fengsu;    private ImageView img_air_conditioner, img_temperature_add, img_temperature_sub, img_switch;    private SmartRefreshLayout smartRefreshLayout;    private String KT_Send = "zckt/cbox/hardware/11111111111111111111111";    private String KT_Accept = "zckt/cbox/app/11111111111111111111111";    private String KT_ccid;    private String kt_temperature = "";    private String kt_lamp = "";    private String kt_mode = "";    private String kt_fengsu = "";    private long time;    private boolean isZaixian;    private boolean iskaijiDianhou;    private boolean isKaiji;    private String nowMingling;    private String kt_lamp_dianhou;    /**     * 用于其他Activty跳转到该Activity     */    public static void actionStart(Context context, String ccid) {        Intent intent = new Intent(context, AirConditionerActivity.class);        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);        intent.putExtra("ccid", ccid);        context.startActivity(intent);    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        StatusBarUtil.setLightMode(this);        initToolbar();        initView();        initHuidiao();        registerKtMqtt();    }    private void initView() {        tv_connect_device = findViewById(R.id.tv_connect_device);        tv_temperature = findViewById(R.id.tv_temperature);        tv_device_mode = findViewById(R.id.tv_device_mode);        tv_device_state = findViewById(R.id.tv_device_state);        tv_hj_temperature = findViewById(R.id.tv_hj_temperature);        tv_device_fengsu = findViewById(R.id.tv_device_fengsu);        tv_alert = findViewById(R.id.tv_alert);        tv_clean_alert = findViewById(R.id.tv_clean_alert);        tv_lamp_on = findViewById(R.id.tv_lamp_on);        tv_lamp_off = findViewById(R.id.tv_lamp_off);        rl_device_state = findViewById(R.id.rl_device_state);        rl_alert = findViewById(R.id.rl_alert);        rl_switch = findViewById(R.id.rl_switch);        img_air_conditioner = findViewById(R.id.img_air_conditioner);        img_temperature_add = findViewById(R.id.img_temperature_add);        img_temperature_sub = findViewById(R.id.img_temperature_sub);        tv_change_mode = findViewById(R.id.tv_change_mode);        tv_change_fengsu = findViewById(R.id.tv_change_fengsu);        img_switch = findViewById(R.id.img_switch);        tv_connect_device.setOnClickListener(this);        rl_device_state.setOnClickListener(this);        tv_clean_alert.setOnClickListener(this);        img_temperature_add.setOnClickListener(this);        img_temperature_sub.setOnClickListener(this);        tv_lamp_on.setOnClickListener(this);        tv_lamp_off.setOnClickListener(this);        tv_change_mode.setOnClickListener(this);        tv_change_fengsu.setOnClickListener(this);        rl_switch.setOnClickListener(this);        PreferenceHelper.getInstance(mContext).putString(App.CHOOSE_KONGZHI_XIANGMU, DoMqttValue.KONGTIAO);        KT_ccid = getIntent().getStringExtra("ccid");//        KT_ccid = "kkkkkkkkkkkkkkkk90120018";        KT_Send = "zckt/cbox/hardware/" + KT_ccid;        KT_Accept = "zckt/cbox/app/" + KT_ccid;        MyApplication.mqttDingyue.add(KT_Send);        MyApplication.mqttDingyue.add(KT_Accept);        isZaixian = false;        smartRefreshLayout = findViewById(R.id.smartRefreshLayout);        smartRefreshLayout.setEnableLoadMore(false);        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {            @Override            public void onRefresh(@NonNull RefreshLayout refreshLayout) {                isZaixian = false;                registerKtMqtt();                smartRefreshLayout.finishRefresh();            }        });    }    private void registerKtMqtt() {        showProgressDialog("设备连接中...");        initHandlerStart();        //注册向空调发送数据的主题        AndMqtt.getInstance().subscribe(new MqttSubscribe()                .setTopic(KT_Send)                .setQos(2), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });        //注册空调向app回调数据的主题        AndMqtt.getInstance().subscribe(new MqttSubscribe()                .setTopic(KT_Accept)                .setQos(2), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });        getNs();    }    /**     * 界面打开时向空调发送查询实时数据指令     */    private void getNs() {        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg("M001.")                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    private void initHandlerStart() {        Message message = handlerStart.obtainMessage(1);        handlerStart.sendMessageDelayed(message, 1000);    }    private void initHandlerClick() {        Message message = handlerStart.obtainMessage(2);        handlerStart.sendMessageDelayed(message, 1000);    }    private void initDengClick() {        Message message = handlerStart.obtainMessage(3);        handlerStart.sendMessageDelayed(message, 1000);    }    private Handler handlerStart = new Handler(new Handler.Callback() {        @Override        public boolean handleMessage(@NonNull Message msg) {            time++;            switch (msg.what) {                case 1:                    if (!isZaixian) {                        if (time >= 30) {                            showTishiDialog();                        } else {                            if (time == 5 || time == 10 || time == 15 || time == 20 || time == 25) {                                getNs();                            }                            initHandlerStart();                        }                    } else {                        dismissProgressDialog();                        time = 0;                    }                    break;                case 2:                    if (iskaijiDianhou != isKaiji) {                        if (time >= 30) {                            iskaijiDianhou = !iskaijiDianhou;                            showTishiDialog();                        } else {                            if (time == 5 || time == 10 || time == 15 || time == 20 || time == 25) {                                sendKT_swich(nowMingling);                            }                            initHandlerClick();                        }                    } else {                        if (iskaijiDianhou) {                            SoundPoolUtils.soundPool(mContext, R.raw.shuinuan_end_on);                        } else {                            SoundPoolUtils.soundPool(mContext, R.raw.shuinuan_end_off);                        }                        dismissProgressDialog();                        time = 0;                    }                    break;                case 3:                    if (!kt_lamp.equals(kt_lamp_dianhou)) {                        if (time >= 30) {                            showTishiDialog();                        } else {                            if (time == 5 || time == 10 || time == 15 || time == 20 || time == 25) {                                sendKT_swich(nowMingling);                            }                            initDengClick();                        }                    } else {//                        if (kt_lamp.equals("1")) {//                            SoundPoolUtils.soundPool(mContext, R.raw.shuinuan_end_on);//                        } else {//                            SoundPoolUtils.soundPool(mContext, R.raw.shuinuan_end_off);//                        }                        dismissProgressDialog();                        time = 0;                    }                    break;            }            Y.e(msg.what + "  的时间时多少啊  " + time);            return false;        }    });    private void showTishiDialog() {        isZaixian = false;        time = 0;        dismissProgressDialog();        TishiDialog tishiDialog = new TishiDialog(mContext, TishiDialog.TYPE_CAOZUO, new TishiDialog.TishiDialogListener() {            @Override            public void onClickCancel(View v, TishiDialog dialog) {            }            @Override            public void onClickConfirm(View v, TishiDialog dialog) {                registerKtMqtt();            }            @Override            public void onDismiss(TishiDialog dialog) {            }        });        tishiDialog.setTextTitle("提示：网络信号异常");        tishiDialog.setTextContent("请检查设备情况。1:设备是否接通电源 2:设备信号灯是否闪烁 3:设备是否有损坏 4:手机是否开启网络，如已确认以上问题，请重新尝试。");        tishiDialog.setTextConfirm("重试");        tishiDialog.setTextCancel("忽略");        tishiDialog.show();    }    @Override    public void onClick(View v) {        switch (v.getId()) {            case R.id.tv_connect_device:                break;            case R.id.tv_clean_alert:                //清除故障                cleanAlert();                break;            case R.id.img_temperature_add:                //温度加                temperatureAdd();                break;            case R.id.img_temperature_sub:                //温度减                temperatureSub();                break;            case R.id.tv_lamp_on:                //开灯                lampOn();                break;            case R.id.tv_lamp_off:                //关灯                lampOff();                break;            case R.id.tv_change_mode:                //更改模式                changeMode();                break;            case R.id.tv_change_fengsu:                //更改风速                changeFengsu();                break;            case R.id.rl_switch:                //开关机                Ktswitch();                break;        }    }    /**     * 获取通知返回的数据     */    private void initHuidiao() {        _subscriptions.add(toObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Notice>() {            @Override            public void call(Notice message) {                if (message.type == ConstanceValue.MSG_ZCKT) {                    String msg = message.content.toString();                    getData(msg);                }            }        }));    }    private void getData(String msg) {        if (msg.contains("j_g")) {            isZaixian = true;            //接收到信息            Y.e("我接收到了数据啦" + "    " + msg.toString());            //手机终端版本号            String banbenhao = msg.substring(3, 6);            //空调模式     0.关机 1.经济模式2.标准模式3.强力模式4.固定模式            kt_mode = msg.substring(6, 7);            //风量   0 1 2 3 4 5 6            kt_fengsu = msg.substring(7, 8);            //设定温度   05-35            kt_temperature = msg.substring(8, 10);            //风门状态   4：开启扫风   0：关闭扫风            String fengmenMode = msg.substring(10, 11);            //当前欠压阀值190-230            String qianyazhi = msg.substring(11, 14);            //当前音量00-15            String yinliang = msg.substring(14, 16);            //当前电压00-50V            String dianya = msg.substring(16, 18);            //进风温度-025=-25度   0026=26度            String wendu_jinfeng = msg.substring(18, 22);            //出风温度-025=-25度   0026=26度            String wendu_chufeng = msg.substring(22, 26);            //压缩机故障码            String compress_code = msg.substring(26, 28);            //面板故障码            String panel_code = msg.substring(28, 30);            //照明状态            kt_lamp = msg.substring(30, 31);            switch (kt_mode) {                case "0":                    tv_device_mode.setText("关机状态");                    rl_device_state.setBackgroundResource(R.drawable.btn_equipment_offline);                    tv_device_state.setText("设备离线");                    img_air_conditioner.setImageResource(R.mipmap.img_air_conditioner_close);                    img_switch.setImageResource(R.mipmap.img_air_conditioner_switch_close);                    tv_device_fengsu.setText("0");                    tv_temperature.setText("0");                    tv_hj_temperature.setText("0℃");                    isKaiji = false;                    return;                case "1":                    tv_device_mode.setText("经济模式");                    rl_device_state.setBackgroundResource(R.drawable.btn_equipment_online);                    tv_device_state.setText("设备在线");                    img_air_conditioner.setImageResource(R.mipmap.img_air_conditioner_open);                    img_switch.setImageResource(R.mipmap.img_air_conditioner_switch_open);                    isKaiji = true;                    break;                case "2":                    tv_device_mode.setText("标准模式");                    rl_device_state.setBackgroundResource(R.drawable.btn_equipment_online);                    img_air_conditioner.setImageResource(R.mipmap.img_air_conditioner_open);                    img_switch.setImageResource(R.mipmap.img_air_conditioner_switch_open);                    tv_device_state.setText("设备在线");                    isKaiji = true;                    break;                case "3":                    tv_device_mode.setText("强力模式");                    rl_device_state.setBackgroundResource(R.drawable.btn_equipment_online);                    img_air_conditioner.setImageResource(R.mipmap.img_air_conditioner_open);                    img_switch.setImageResource(R.mipmap.img_air_conditioner_switch_open);                    tv_device_state.setText("设备在线");                    isKaiji = true;                    break;                case "4":                    tv_device_mode.setText("固定模式");                    rl_device_state.setBackgroundResource(R.drawable.btn_equipment_online);                    img_air_conditioner.setImageResource(R.mipmap.img_air_conditioner_open);                    img_switch.setImageResource(R.mipmap.img_air_conditioner_switch_open);                    tv_device_state.setText("设备在线");                    isKaiji = true;                    break;            }            tv_device_fengsu.setText(kt_fengsu);            tv_temperature.setText(kt_temperature);            tv_hj_temperature.setText(wendu_jinfeng + "℃");            if (compress_code.equals("00") && panel_code.equals("00")) {                rl_alert.setVisibility(View.GONE);            } else {                //压缩机故障                if (!(compress_code.equals("00") || compress_code.equals("aa"))) {                    rl_alert.setVisibility(View.VISIBLE);                    tv_alert.setText("压缩机报警");                }                if (!(panel_code.equals("00") || panel_code.equals("aa"))) {                    rl_alert.setVisibility(View.VISIBLE);                    tv_alert.setText("控制面板报警");                }            }            if (kt_lamp.equals("0")) {                //灯关闭                tv_lamp_on.setTextColor(Color.rgb(153, 153, 153));                tv_lamp_off.setTextColor(Color.rgb(6, 239, 1));            } else {                //灯开启                tv_lamp_on.setTextColor(Color.rgb(6, 239, 1));                tv_lamp_off.setTextColor(Color.rgb(153, 153, 153));            }        }    }    /**     * 清除故障     */    private void cleanAlert() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        String msg = "M081.";        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    /**     * 温度加     */    private void temperatureAdd() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        String nowTemperature = tv_temperature.getText().toString();        if (Integer.parseInt(nowTemperature) == 35) {            Toast.makeText(context, "空调温度最高为35度", Toast.LENGTH_SHORT).show();            return;        }        String temperature = String.valueOf(Integer.parseInt(nowTemperature) + 1);        if (temperature.length() == 1) {            temperature = "0" + temperature;        }        tv_temperature.setText(temperature);        String msg = "M04" + temperature + ".";        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    /**     * 温度减     */    private void temperatureSub() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        String nowTemperature = tv_temperature.getText().toString();        if (Integer.parseInt(nowTemperature) == 5) {            Toast.makeText(context, "空调温度最低为5度", Toast.LENGTH_SHORT).show();            return;        }        String temperature = String.valueOf(Integer.parseInt(nowTemperature) - 1);        if (temperature.length() == 1) {            temperature = "0" + temperature;        }        tv_temperature.setText(temperature);        String msg = "M04" + temperature + ".";        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    /**     * 开灯     */    private void lampOn() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        if (kt_lamp.equals("1")) {            Toast.makeText(context, "照明灯已打开", Toast.LENGTH_SHORT).show();            return;        }        showProgressDialog("正在开灯...");        nowMingling = "M011.";        kt_lamp_dianhou = "1";        initDengClick();        sendDeng(nowMingling);    }    /**     * 关灯     */    private void lampOff() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        if (kt_lamp.equals("0")) {            Toast.makeText(context, "照明灯已关闭", Toast.LENGTH_SHORT).show();            return;        }        showProgressDialog("正在关灯...");        nowMingling = "M010.";        kt_lamp_dianhou = "0";        initDengClick();        sendDeng(nowMingling);    }    private void sendDeng(String nowMingling) {        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(nowMingling)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    /**     * 更改模式     *     * @return     */    private void changeMode() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        String mode = tv_device_mode.getText().toString();        String msg;        if (mode.equals("经济模式")) {            msg = "M032.";            tv_device_mode.setText("标准模式");        } else if (mode.equals("标准模式")) {            msg = "M033.";            tv_device_mode.setText("强力模式");        } else if (mode.equals("强力模式")) {            msg = "M034.";            tv_device_mode.setText("固定模式");        } else {            msg = "M031.";            tv_device_mode.setText("经济模式");        }        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {                Log.i("Rair", "(KT_Accept)-onSuccess:-&gt;发布成功");            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {                Log.i("Rair", "(MainActivity.java:84)-onFailure:-&gt;发布失败");            }        });    }    /**     * 更改风速     */    private void changeFengsu() {        if (!isZaixian) {            return;        }        if (kt_mode.equals("0")) {            Y.t("空调已关机，请打开空调发送指令");            return;        }        int fengsu = Integer.parseInt(tv_device_fengsu.getText().toString());        if (fengsu == 6) {            fengsu = 1;        } else {            fengsu += 1;        }        tv_device_fengsu.setText(fengsu + "");        String msg = "M02" + fengsu + ".";        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    /**     * 空调开关     *     * @return     */    private void Ktswitch() {        if (!isZaixian) {            showTishiDialog();            return;        }        int mode = Integer.parseInt(kt_mode);        if (mode > 0) {            nowMingling = "M030.";            iskaijiDianhou = false;        } else {            nowMingling = "M031.";            iskaijiDianhou = true;        }        showProgressDialog("发送指令中...");        initHandlerClick();        sendKT_swich(nowMingling);    }    private void sendKT_swich(String msg) {        AndMqtt.getInstance().publish(new MqttPublish()                .setMsg(msg)                .setQos(2).setRetained(false)                .setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });    }    @Override    public boolean showToolBar() {        return true;    }    @Override    protected void initToolbar() {        super.initToolbar();        tv_title.setText("驻车空调");        tv_title.setTextSize(17);        tv_title.setTextColor(getResources().getColor(R.color.black));        iv_rightTitle.setImageResource(R.mipmap.fengnuan_icon_shezhi);        iv_rightTitle.setVisibility(View.VISIBLE);        iv_rightTitle.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                SheBeiSetActivity.actionStart(mContext, SheBeiSetActivity.TYPE_KONGTISO);            }        });        mToolbar.setNavigationIcon(R.mipmap.backbutton);        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                finish();            }        });    }    @Override    public int getContentViewResId() {        return R.layout.activity_air_conditioner;    }    @Override    public void initImmersion() {        mImmersionBar.with(this).statusBarColor(R.color.white).init();    }    @Override    protected void onDestroy() {        super.onDestroy();        handlerStart.removeMessages(1);        handlerStart.removeMessages(2);        handlerStart.removeMessages(3);        AndMqtt.getInstance().unSubscribe(new MqttUnSubscribe().setTopic(KT_Send), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });        AndMqtt.getInstance().unSubscribe(new MqttUnSubscribe().setTopic(KT_Accept), new IMqttActionListener() {            @Override            public void onSuccess(IMqttToken asyncActionToken) {            }            @Override            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {            }        });        for (int i = 0; i < MyApplication.mqttDingyue.size(); i++) {            if (MyApplication.mqttDingyue.get(i).equals(KT_Send)) {                MyApplication.mqttDingyue.remove(i);            }        }        for (int i = 0; i < MyApplication.mqttDingyue.size(); i++) {            if (MyApplication.mqttDingyue.get(i).equals(KT_Accept)) {                MyApplication.mqttDingyue.remove(i);            }        }    }}