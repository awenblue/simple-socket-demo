package com.awen.socket;

/**
 * Created by Awen on 2016/7/6.
 */
public interface TCPConnectNotify {

    void connectSuccess();

    void connectFailed();

    void newMessageSuccess(Object msg);

    void newMessageFailed();
}
