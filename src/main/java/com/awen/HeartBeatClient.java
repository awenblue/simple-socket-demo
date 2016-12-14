package com.awen;


import com.awen.socket.Connector;
import com.awen.socket.TCPConnectNotify;
import com.awen.socket.TCPConnector;

/**
 * Created by Awen on 2016/11/23.
 */
public class HeartBeatClient {

    private Connector tcpConnector;

    private static volatile HeartBeatClient client;

    static class Task implements Runnable {

        public void run() {
            if (client == null) {
                getClient().connect();
                return;
            }
        }
    }

    public static void main(String[] args) {
        startTask();
    }

    public static void startTask() {
        new Thread(new Task()).start();
    }

    private static HeartBeatClient getClient() {
        if (client == null) {
            synchronized (HeartBeatClient.class) {
                if (client == null) {
                    client = new HeartBeatClient();
                }
            }
        }
        return client;
    }

    private HeartBeatClient() {
        tcpConnector = new TCPConnector("127.0.0.1", 8081, new TCPNotification());
    }

    private void connect() {
        try {
            tcpConnector.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send(Object object) {
        try {
            tcpConnector.send(object);
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    private void close() {
        try {
            tcpConnector.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tcpConnector = null;
            client = null;
        }
    }

    class TCPNotification implements TCPConnectNotify {
        
        public void connectSuccess() {
            System.out.println("connectSuccess");
        }

        public void connectFailed() {
            System.out.println("connectFailed");
        }

        public void newMessageSuccess(Object msg) {
            System.out.println("newMessageSuccess: " + msg);
        }

        
        public void newMessageFailed() {
            System.out.println("newMessageFailed");
        }
    }
}
