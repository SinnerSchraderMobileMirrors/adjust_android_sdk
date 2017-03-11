package com.example.testlibrary;

import com.adjust.sdk.Util;

import java.util.Arrays;
import java.util.List;

import static com.example.testlibrary.Utils.gson;
import static com.example.testlibrary.Utils.sendPostI;

/**
 * Created by nonelse on 11.03.17.
 */

public class TestLibraryCommandExecutor {
    private TestLibrary testLibrary;

    TestLibraryCommandExecutor(TestLibrary testLibrary) {
        this.testLibrary = testLibrary;
    }
    void executeCommand(TestCommand testCommand) {
        switch (testCommand.functionName) {
            case "end_test": endTest(); break;
        }
    }

    void endTest() {
        Util.HttpResponse httpResponse = sendPostI("/end_test");
        if (httpResponse == null) {
            return;
        }

        List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
        this.testLibrary.execTestCommands(testCommands);
    }
}
