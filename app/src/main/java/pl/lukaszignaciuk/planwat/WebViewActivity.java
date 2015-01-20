package pl.lukaszignaciuk.planwat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

public class WebViewActivity extends Activity {

    private WebView webView;

    private boolean planLoaded;
    private boolean split = false;
    private String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());

        startAndLogin();
    }

    private void startAndLogin(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String login;
        String password;
        String url;
        String postData;

        login = prefs.getString("lgn", "");
        password = prefs.getString("pwd", "");

        url = "https://s1.wcy.wat.edu.pl/ed/index.php?sid=ea644ce6e727ef5320c56c2034bece7f";
        postData = "formname=login&userid="+login+"&password="+password;
        planLoaded = false;
        webView.postUrl(url, EncodingUtils.getBytes(postData, "UTF8"));
    }

    private void loadPlan(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String term;
        String group;
        String nUrl;

        group = prefs.getString("group", "");
        term = prefs.getString("term", "");

        if(term.contentEquals("Zimowy")) {
            nUrl = "https://s1.wcy.wat.edu.pl/ed/logged.php?" + sessionID + "&mid=328&iid=20144&prn=10&exv=" + group.toUpperCase(); //iid=20144 to semestr zimowy 2014/2015
        }
        else{
            nUrl = "https://s1.wcy.wat.edu.pl/ed/logged.php?" + sessionID + "&mid=328&iid=20145&prn=10&exv=" + group.toUpperCase(); //iid=20145 to semestr letni 2014/2015
        }
        webView.loadUrl(nUrl);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Wykonywane po wcisnieciu przycisku Opcje
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PrefsFragment())
                    .addToBackStack("settings")
                    .commit();
        }
        if (id == R.id.action_refresh) {
            //Wykonywane po wcisnieciu przycisku Odswiez
            split = false;
            planLoaded = false;
            startAndLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {

        final ProgressDialog pd = ProgressDialog.show(WebViewActivity.this, "", "Ładowanie...", true);

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            return false;
        }

        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed() ;
        }

        @Override
        public void onPageFinished(WebView view, String url){

            if(url.contains("ea644ce6e727ef5320c56c2034bece7f")){
                webView.loadData("Podaj poprawne dane logowania w opcjach, a następnie kliknij ODŚWIEŻ.", "text/html; charset=UTF-8", null);
            }
            pd.dismiss();
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            if(url.contains("logged_inc.php") && !split){
                String parts[] = url.split("\\?");
                String sid[] = parts[1].split("&");
                sessionID = sid[0];
                split = true;
                if(!planLoaded){
                    loadPlan();
                }
            }
            pd.show();
        }
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
        @Override
        public void onResume() {
            super.onResume();
            getView().setBackgroundColor(Color.WHITE);
        }
    }
}
