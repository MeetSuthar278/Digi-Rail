package com.example.myrail;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class IrctcReservationWebview extends AppCompatActivity {

    WebView web;
    ProgressBar pgBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irctc_web);

        web = findViewById(R.id.web);
        pgBar = findViewById(R.id.pgBar);
        web.setVisibility(View.VISIBLE);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.loadUrl("https://www.irctc.co.in/nget/train-search");
//        web.loadUrl("https://www.google.com");
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pgBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pgBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });

    }
    @Override
    public void onBackPressed() {
        if (web.canGoBack()){
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}