package com.obaied.commandsexample;

import android.os.*;
import android.support.v7.app.*;

import com.adjust.analyzertest.*;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent intent = getIntent();
//        Uri data = intent.getData();
//        Adjust.appWillOpenUrl(data);

        AdjustAnalyzer.init("http://10.0.1.6:8080",
                new AdjustAnalyzer.AnalyzerCallback_OnPostInit() {
                    @Override
                    public void onPostInit(String commands) {
                        AnalyzerDictionary.executeCommand(commands);
                    }
                });

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "run: command 1: sleep");
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "run: command 2: oncreate");
//                AdjustConfig adjustConfig = new AdjustConfig(GlobalApplication.getAppContext(), "qwerty12345x", "sandbox", false);
//                Adjust.onCreate(adjustConfig);
//
//                Log.d(TAG, "run: command 3: sleep");
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "run: command 4: reportState");
//                AdjustAnalyzer.reportState("callsite A");
//
//                Log.d(TAG, "run: command 5: onresume");
//                Adjust.onResume();
//
//                Log.d(TAG, "run: command 6: sleep");
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "run: command 7: terminate");
//                AdjustAnalyzer.terminate();
//
//                Log.d(TAG, "run: command 8: sleep");
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
    }
}
