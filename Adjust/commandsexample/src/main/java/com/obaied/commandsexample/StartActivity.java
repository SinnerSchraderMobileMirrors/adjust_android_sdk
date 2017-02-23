package com.obaied.commandsexample;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.util.*;

import com.adjust.analyzertest.*;
import com.adjust.sdk.*;

import java.util.*;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent intent = getIntent();
//        Uri data = intent.getData();
//        Adjust.appWillOpenUrl(data);

        AdjustAnalyzer.init("http://172.16.150.242:8080");
        AdjustAnalyzer.executeCommands(new AdjustAnalyzer.AnalyzerCallback() {
            @Override
            public void onPostGetCommands(AdjustAnalyzer.Command[] myCommands) {
                AnalyzerDictionary.executeCommand(myCommands);
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
