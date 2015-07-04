package com.flicq.tennis.framework;

import android.content.Context;

public interface IActivityAdapter {

    void SetStatus(StatusType type, String s);

    Context GetApplicationContext();

    void writeToUi(String str);

    void onDisconnected();
}
