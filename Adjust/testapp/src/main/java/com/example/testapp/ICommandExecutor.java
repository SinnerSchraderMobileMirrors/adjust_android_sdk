package com.example.testapp;

/**
 * Created by nonelse on 10.03.17.
 */

public abstract class ICommandExecutor {
    protected Command command;

    void executeCommand(Command command) {
        this.command = command;
    }
}
