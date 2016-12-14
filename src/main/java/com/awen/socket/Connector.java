package com.awen.socket;

/**
 * Created by Awen on 2016/7/14.
 */
public interface Connector {

    void connect() throws Exception;

    void send(Object ojb);

    void close() throws Exception;
}
