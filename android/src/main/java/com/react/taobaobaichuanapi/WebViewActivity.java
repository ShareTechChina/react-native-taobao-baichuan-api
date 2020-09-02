package com.react.taobaobaichuanapi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alibaba.baichuan.android.trade.AlibcTrade;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeCallback;
import com.alibaba.baichuan.android.trade.model.AlibcShowParams;
import com.alibaba.baichuan.android.trade.model.OpenType;
import com.alibaba.baichuan.trade.biz.applink.adapter.AlibcFailModeType;
import com.alibaba.baichuan.trade.biz.context.AlibcTradeResult;
import com.alibaba.baichuan.trade.biz.core.taoke.AlibcTaokeParams;
import com.alibaba.baichuan.trade.common.utils.AlibcLogger;

import java.util.HashMap;
import java.util.Map;
import com.react.taobaobaichuanapi.R;
import java.net.URLDecoder;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

public class WebViewActivity extends Activity implements View.OnClickListener {

    private static CallBack callBack;
    private ImageView backBtn, closeBtn;
    private TextView titleTv;
    private WebView webView;
    public static void setCallBack(CallBack callBack){
        WebViewActivity.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webview_activity);
        Intent intent = getIntent();
        if (intent != null) {
            final String url = intent.getStringExtra("url");
            HashMap<String, Object> arguments = (HashMap<String, Object>) intent.getSerializableExtra("arguments");
            webView = findViewById(R.id.webview);
            //启用支持JavaScript
            webView.getSettings().setJavaScriptEnabled(true);
            //启用支持DOM Storage
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);

            backBtn = (ImageView) findViewById(R.id.back_btn);
            backBtn.setOnClickListener(this);
            closeBtn = (ImageView) findViewById(R.id.close_btn);
            closeBtn.setOnClickListener(this);
            titleTv = (TextView) findViewById(R.id.title);
            titleTv.setOnClickListener(this);

            openByUrl(url, webView, arguments);
        }

    }

    private String getParam(String url, String key) {
        try {
            int startIndex = url.indexOf(key);
            String subStr = url.substring(startIndex);
            String tempUrl = URLDecoder.decode(subStr, "UTF-8");
            int endIndex = tempUrl.indexOf("&");
            subStr = tempUrl.substring(0, endIndex);
            startIndex = subStr.indexOf("=");
            subStr = subStr.substring(startIndex+1);
            return subStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void openByUrl(String url, WebView webView, HashMap argument) {
        //展示参数配置
        AlibcShowParams showParams = new AlibcShowParams();
        showParams.setOpenType(OpenType.Auto);
        showParams.setClientType("taobao");
        showParams.setBackUrl("duoshouji://");
        AlibcTaokeParams taokeParams = new AlibcTaokeParams("", "", "");
        taokeParams.setPid("mm_112883640_11584347_72287650277");
        Map<String, String> trackParams = new HashMap<>();

        AlibcTrade.openByUrl(WebViewActivity.this,
                "", url,
                webView,
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        super.onLoadResource(view, url);
                        if (url.contains("access_token")){
                            String accessToken = getParam(url, "access_token");
                            String userId = getParam(url, "taobao_user_id");
                            if (callBack != null){
                                Map<String, String> map = new HashMap<>();
                                map.put("accessToken", accessToken);
                                map.put("userId", userId);
                                callBack.success(map);
                                callBack = null;
                            }
                            finish();
                        }
                    }
                },
                new WebChromeClient() {
                },
                showParams,
                taokeParams,
                trackParams,
                new AlibcTradeCallback() {
                    @Override
                    public void onTradeSuccess(AlibcTradeResult tradeResult) {
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (callBack != null){
            callBack.failed("授权失败");
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() ==  R.id.back_btn) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        }
        if (v.getId() ==  R.id.close_btn) {
            finish();
        }
    }

    public interface CallBack{
        void success(Map accessToken);

        void failed(String errorMsg);
    }

}
