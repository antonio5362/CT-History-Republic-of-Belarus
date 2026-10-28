package by.cthistory.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // localStorage: прогресс, дневник, серия дней
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setTextZoom(100);                    // не масштабировать шрифт системными настройками
        s.setSupportZoom(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
        });

        // alert() из таймера помодоро
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onResume() { super.onResume(); webView.onResume(); }

    @Override
    protected void onPause() { webView.onPause(); super.onPause(); }

    @Override
    public void onBackPressed() {
        // На экране с материалом кнопка «назад» возвращает к разделам, иначе — выход
        webView.evaluateJavascript(
            "(function(){var c=document.getElementById('screen-content');" +
            "if(c&&c.classList.contains('active')){document.getElementById('contentBack').click();return 1}" +
            "var n=document.querySelector('.nav-btn.active');" +
            "if(n&&n.dataset.screen!=='sections'){document.querySelector('.nav-btn[data-screen=sections]').click();return 1}" +
            "return 0})()",
            value -> { if (!"1".equals(value)) MainActivity.super.onBackPressed(); });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.destroy(); }
        super.onDestroy();
    }
}
