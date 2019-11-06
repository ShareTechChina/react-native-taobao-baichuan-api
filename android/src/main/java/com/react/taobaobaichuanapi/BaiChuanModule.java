package com.react.taobaobaichuanapi;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.alibaba.baichuan.android.trade.AlibcTrade;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeCallback;
import com.alibaba.baichuan.android.trade.page.AlibcBasePage;
import com.alibaba.baichuan.android.trade.page.AlibcDetailPage;
import com.alibaba.baichuan.android.trade.page.AlibcMyOrdersPage;
import com.alibaba.baichuan.trade.biz.core.taoke.AlibcTaokeParams;
import com.alibaba.baichuan.trade.biz.context.AlibcTradeResult;
import com.alibaba.baichuan.android.trade.model.AlibcShowParams;
import com.alibaba.baichuan.android.trade.model.OpenType;
import android.app.Activity;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import android.content.Intent;
import com.facebook.react.bridge.Promise;

import java.util.Map;
import java.util.HashMap;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.util.Log;

public class BaiChuanModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Promise promise;
    private final static String NOT_LOGIN = "not login";
    private EventManager eventManager;

    public BaiChuanModule(ReactApplicationContext reactContext) {

        super(reactContext);
        reactContext.addActivityEventListener(this);
        this.eventManager = new EventManager(reactContext);
    }

    @Override
    public String getName() {
        return "React_Native_Taobao_Baichuan_Api";
    }

    @ReactMethod
    public void jump(String itemId, String orderId,String type,Callback successCallback ) {

            //商品详情page
            AlibcBasePage detailPage = new AlibcDetailPage(itemId);
            //实例化我的订单打开page
            AlibcBasePage ordersPage = new AlibcMyOrdersPage(1, true);
            //展示参数配置
            AlibcShowParams showParams = new AlibcShowParams();
            showParams.setOpenType(OpenType.Auto);
            showParams.setClientType("taobao");
            showParams.setBackUrl("duoshouji://");

            AlibcTaokeParams taokeParams = new AlibcTaokeParams("", "", "");
            taokeParams.setPid("mm_120032403_0_0");

            Map<String, String> trackParams = new HashMap<>();

            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                return;
            }

            final  EventManager eventManager = this.eventManager;
            AlibcTrade.openByBizCode(currentActivity,
                    (itemId == null ||"".equals(itemId))?ordersPage:detailPage,
                    null,
                    new WebViewClient(),
                    new WebChromeClient(),
                    (itemId == null ||"".equals(itemId))?"orders":"detail",
                    showParams,
                    taokeParams,
                    trackParams,
                    new AlibcTradeCallback() {

                @Override
                public void onTradeSuccess(AlibcTradeResult tradeResult) {
                    //打开电商组件，用户操作中成功信息回调。tradeResult：成功信息（结果类型：加购，支付；支付结果）
                    eventManager.send("backFromTB", null);
                }

                @Override
                public void onFailure(int code, String msg) {
                    //打开电商组件，用户操作中错误信息回调。code：错误码；msg：错误信息
                    Log.e( "", "(" + code + ")" + msg +"test");
                    eventManager.send("backFromTB", null);

                }

            });
    }

    @ReactMethod
    public void jumpByUrl(String url, Callback successCallback ) {

        //展示参数配置
        AlibcShowParams showParams = new AlibcShowParams();
        showParams.setOpenType(OpenType.Auto);
        showParams.setClientType("taobao");
        showParams.setBackUrl("duoshouji://");

        AlibcTaokeParams taokeParams = new AlibcTaokeParams("", "", "");
        taokeParams.setPid("mm_112883640_11584347_72287650277");

        Map<String, String> trackParams = new HashMap<>();

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        final EventManager eventManager = this.eventManager;
        AlibcTrade.openByUrl(currentActivity,
                "", url,
                null,
                new WebViewClient(),
                new WebChromeClient(),
                showParams,
                taokeParams,
                trackParams,
                new AlibcTradeCallback() {
                    @Override
                    public void onTradeSuccess(AlibcTradeResult tradeResult) {
                        //打开电商组件，用户操作中成功信息回调。tradeResult：成功信息（结果类型：加购，支付；支付结果）
                        eventManager.send("backFromTB", null);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        //打开电商组件，用户操作中错误信息回调。code：错误码；msg：错误信息
                        Log.e("", "(" + code + ")" + msg + "test");
                        eventManager.send("backFromTB", null);

                    }
                });
        successCallback.invoke(true);

    }


    @Override
    public void onActivityResult(Activity activity,final int requestCode, final int resultCode, final Intent intent) {
        this.eventManager.send("backFromTB", null);
    }
    @Override
    public void onNewIntent(final Intent intent) {
        this.eventManager.send("backFromTB", null);
    }
}