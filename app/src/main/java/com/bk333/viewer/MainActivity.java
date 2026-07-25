package com.bk333.viewer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private ProgressBar progressBar;
    private String currentUrl = "https://www.www-bk333.com/m/home";
    private Handler handler = new Handler();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();
        setupButtons();
        loadUrl(currentUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 BK333/1.0");

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
                    try {
                        startActivity(new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse(url)
                        ));
                    } catch (Exception e) {}
                    return true;
                }
                loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlInput.setText(url);
                currentUrl = url;
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                injectBypassScript();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("BK333")
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setOnDismissListener(d -> result.confirm())
                    .show();
                return true;
            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    private void injectBypassScript() {
        String script = "(function() {" +
            "try{" +
            "var m=document.querySelectorAll('meta');" +
            "for(var i=0;i<m.length;i++){" +
            "var h=m[i].getAttribute('http-equiv');" +
            "if(h&&(h.toLowerCase().indexOf('x-frame')>=0||" +
            "h.toLowerCase().indexOf('content-security')>=0))" +
            "m[i].remove();" +
            "}" +
            "Object.defineProperty(window,'top',{get:function(){return window.self}});" +
            "Object.defineProperty(window,'parent',{get:function(){return window.self}});" +
            "}catch(e){}" +
            "})();";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, null);
        } else {
            webView.loadUrl("javascript:" + script);
        }
    }

    private void loadUrl(String url) {
        if (url == null || url.isEmpty()) return;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        currentUrl = url;
        urlInput.setText(url);

        // Proxy URL (bypass X-Frame-Options)
        String proxyUrl = "https://api.allorigins.win/raw?url=" + Uri.encode(url);
        webView.loadUrl(proxyUrl);

        // Fallback to direct after 10 seconds
        handler.postDelayed(() -> {
            webView.loadUrl(url);
        }, 10000);
    }

    private void setupButtons() {
        urlInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrl(urlInput.getText().toString().trim());
                return true;
            }
            return false;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });

        findViewById(R.id.btnForward).setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });

        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadUrl(currentUrl));

        findViewById(R.id.btnHome).setOnClickListener(v ->
            loadUrl("https://www.www-bk333.com/m/home"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }
}
