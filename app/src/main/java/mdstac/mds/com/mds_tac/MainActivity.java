package mdstac.mds.com.mds_tac;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    private final String LOG_TAG = this.getClass().getName();
    private Context context;
    private long timeout = 5000;
    private long interval = 500;
    private CountDownTimer timer;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        ActionBar actionBar = getDelegate().getSupportActionBar();
        try {
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not set ActionBar background.");
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        myWebView = (WebView) findViewById(R.id.webview);

        progressBar.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(timeout, interval) {
            @Override
            public void onTick(long l) {
                if (myWebView.getProgress() < 100) {
                    Log.d("progressLog", "progress =  " + myWebView.getProgress());
                }
            }

            @Override
            public void onFinish() {
                Log.d("progressLog", "timeout!");
                progressBar.setVisibility(View.GONE);
                showNoAccessDialog(context);
                timer.cancel();
            }
        }.start();

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("https://mdstac.mds.rs/mdstac.htm");
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d("progressLog", "page finished loading");
                timer.cancel();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                progressBar.setVisibility(View.GONE);
                Log.d("progressLog", "timer stopped - sslError!");
                timer.cancel();
                handler.proceed(); // ako je ovaj kod aktivan, NE prikazuje se upozorenje.
                //showWarningDialog(context, handler); // ako je ovaj kod aktivan, prikazuje se upozorenje.
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitDialog(this.context);
    }

    // Kreiranje actionBar menija sa refresh dugmetom.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Klik na menuItem (refresh dugme).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_menu_item) {
            myWebView.loadUrl("javascript:window.location.reload(true)");
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(R.string.prompt_leave_app);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showWarningDialog(Context context, final SslErrorHandler handler) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.warning);
        dialog.setMessage(R.string.connection_not_secure);  //ctrl + klik na connection_not_secure da izmenis tekst poruke
        dialog.setIcon(R.drawable.ic_warning_red_24dp);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handler.proceed();
            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showNoAccessDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.warning);
        dialog.setMessage(R.string.no_access);  //ctrl + klik na connection_not_secure da izmenis tekst poruke
        dialog.setIcon(R.drawable.ic_warning_red_24dp);
        dialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}