package ch.hsr.plttest;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int NOF_MEASUREMENTS = 2;
    public static String TAG = "PLTTest";

    private WebView webView;
    private EditText fileNameField;
    private Button startBtn;
    private int urlIndex;
    private int measurementCount;
    private long startTime;
    private long doneTime;
    private boolean loadingFinished, redirect, isLoading;
    private String currentURL;
    private CountDownTimer timer;

    private ArrayList<ArrayList<Long>> results = new ArrayList<ArrayList<Long>>();


    private static String[] urls = {
            "http://youtube.com", "http://apple.com", "http://nzz.ch", "http://amazon.de", "http://20min.ch", "http://mobile2.tagesanzeiger.ch",
            "http://blick.ch", "http://m.facebook.com", "http://google.com" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        fileNameField = (EditText) findViewById(R.id.fileNameField);
        startBtn = (Button) findViewById(R.id.startTestButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
//                writeResultsToFile();
            }
        });

        timer = new CountDownTimer(5000, 6000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.e(TAG, "timer finished");
                if (getIsLoading()) {
//                    webView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            webView.stopLoading();
//                            doneTime = startTime;
//                            processResult();
//                        }
//                    });
                    webView.stopLoading();
                    doneTime = startTime;
                    processResult();
                }
            }
        };

        webView.setWebViewClient(new WebViewClient() {

//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
//                if (!loadingFinished) {
//                    redirect = true;
//                }
//
//                loadingFinished = false;
//                currentURL = urlNewString;
//                webView.loadUrl(urlNewString);
//                return true;
//            }

//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                loadingFinished = false;
////                Log.e(TAG, "Page started");
////                startTime = System.currentTimeMillis();
//                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
//            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                if (!currentURL.equals(url)) {
//                    Toast.makeText(getBaseContext(), "loaded url unequal", Toast.LENGTH_SHORT).show();
//                }
//                if (!redirect) {
//                    loadingFinished = true;
//                }
//                if (loadingFinished && !redirect) {
//                    doneTime = System.currentTimeMillis();
                Log.e(TAG, "loaded url: " + url);
//                    processResult();
//                    //int timeToLoad = doneTime - startTime;
//                } else {
//                    redirect = false;
//                }
                doneTime = System.currentTimeMillis();
                setIsLoading(false);
                timer.cancel();
                super.onPageFinished(view, url);
                webView.stopLoading();
                processResult();
            }
        });

        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);

        Log.e(TAG, "onCreate() finished");
    }

    private synchronized boolean getIsLoading() { return isLoading; }
    private synchronized void setIsLoading(boolean state) { isLoading = state; }

    private void startTest() {
        Log.e(TAG, "startTest()");
        urlIndex = 0;
        measurementCount = 0;
        if (results.size() < 1) {
            for (int i = 0; i < NOF_MEASUREMENTS; i++)
                results.add(new ArrayList<Long>());
            Log.e(TAG, "Added Arrays to array");
        }
//        webView.clearCache(true);
        startBtn.setEnabled(false);
        startBtn.setVisibility(View.GONE);
        fileNameField.setVisibility(View.GONE);
        loadUrlInWebView(urls[urlIndex]);
    }

    private void processResult() {
        long loadingTime = doneTime - startTime;
        Log.e(TAG, "processResult() cycle: " + measurementCount + "  time: " + loadingTime);
        if(measurementCount < NOF_MEASUREMENTS) {
//            Log.e(TAG, "processResult step 1");
            results.get(measurementCount).add(loadingTime);

            if (++urlIndex < urls.length && measurementCount < NOF_MEASUREMENTS) {
//                Log.e(TAG, "processResult step 2");
                loadUrlInWebView(urls[urlIndex]);
            } else {
                if (++measurementCount < NOF_MEASUREMENTS) {
//                    Log.e(TAG, "processResult step 4");
                    urlIndex = 0;
                    webView.clearCache(true);
                    loadUrlInWebView(urls[urlIndex]);
                } else {
//                    Log.e(TAG, "processResult step 5");
//                    writeResultsToFile();
                    startBtn.setEnabled(true);
                    startBtn.setVisibility(View.VISIBLE);
                    fileNameField.setVisibility(View.VISIBLE);
                }
            }
        }
//        Log.e(TAG, "processResult step 6");
    }

    private void loadUrlInWebView(String url) {
        currentURL = urls[urlIndex];
        Log.e(TAG, "loadUrl(" + urls[urlIndex] + ")");
        setIsLoading(true);
//        timer.start();
        startTime = System.currentTimeMillis();
        webView.loadUrl(url);
    }

    private void writeResultsToFile() {
        String fileName = fileNameField.getText().toString();
        Log.e(TAG, "fileName: " + fileName);
        if (!fileName.isEmpty()) {
            String baseFolder;
// check if external storage is available
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                baseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
// revert to using internal storage (not sure if there's an equivalent to the above)
            else {
                baseFolder = getFilesDir().getAbsolutePath();
            }

            String string = "hello world!";
            Log.e(TAG, "path: " + baseFolder);
            File file = new File(baseFolder + File.separator + fileName + ".csv");
            file.getParentFile().mkdirs();
            PrintWriter fos = null;
            try {
                fos = new PrintWriter(file);
//                fos.write(string.getBytes());
                for (int i = 0; i<urls.length;i++) {
                    if (i == urls.length - 1) {
                        fos.println(urls[i]);
                    } else {
                        fos.print(urls[i] + ";");
                    }
                }
                ArrayList<Long> tmp;
                for(int j = 0; j<results.size();j++) {
                    tmp = results.get(j);
                    for (int i = 0; i < tmp.size(); i++) {
                        if (i == tmp.size() - 1) {
                            fos.printf("%d\n", tmp.get(i).longValue());
                        } else {
                            fos.printf("%d;", tmp.get(i).longValue());
                        }
                    }
                    tmp.clear();
                }


                fos.flush();
                fos.close();
                Log.e(TAG, "writet out");
            } catch (Exception e) {
                Log.e(TAG, "error writing");
                e.printStackTrace();
            }

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } else
            Log.e(TAG, "File not accesiiblye");
    }

}
