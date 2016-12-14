package com.awen.socket;


import com.awen.util.Hex2StringHelper;
import com.awen.util.compact.ClassLoaderClassResolver;
import com.awen.util.compact.CompactObjectInputStream;
import com.awen.util.compact.CompactObjectOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Created by Awen on 2016/7/6.
 */
public class TCPConnector implements Connector {
    private final byte[] PACK_LENGTH = new byte[4];

    private String host;
    private int port;
    private Socket client;
    private TCPConnectNotify connectNotify;
    private boolean needWork;

    public TCPConnector(String host, int port, TCPConnectNotify connectNotify){
        this.host = host;
        this.port = port;
        this.connectNotify = connectNotify;
        this.needWork = true;
    }

    //@Override
    public void connect() {
        try {
            client = new Socket(host, port);
            client.setKeepAlive(true);
        } catch (IOException e) {
            e.printStackTrace();
            closeConnect();
            connectNotify.connectFailed();
            return;
        }

        if (!client.isConnected()) {
            connectNotify.connectFailed();
            return;
        }
        connectNotify.connectSuccess();

        work();
    }

    //@Override
    public void send(Object object) {
        if (needWork) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream oout = new CompactObjectOutputStream(byteArrayOutputStream);
                oout.writeObject(object);
                oout.flush();
                oout.close();

                int size = byteArrayOutputStream.size();
                byte[] array = ByteBuffer.allocate(4).putInt(size).array();

                ByteArrayOutputStream pack = new ByteArrayOutputStream();
                pack.write(array);
                pack.write(byteArrayOutputStream.toByteArray());

                OutputStream outputStream = client.getOutputStream();
                outputStream.write(pack.toByteArray());

            } catch (IOException e) {
                e.printStackTrace();
                closeConnect();
            }
        }
    }

    //@Override
    public void close() {
        //needWork = false;
        closeConnect();
    }

    private void work() {
        while (needWork) {
            //printStatue();
            checkConnect();
            checkReceiveMsg();
            sleepMills(10000);
        }
    }

    void checkConnect() {
        if (client == null
                || !client.isConnected()
                || client.isInputShutdown()
                || client.isOutputShutdown()) {
            connect();
        }
    }

    void checkReceiveMsg() {
        try {
            DataInputStream dataInputStream = getDataStream();
            int num = dataInputStream.available();
            if (num > 0)
                System.out.println("服务端返回长度：" + num);
            if (num > 4) {

                dataInputStream.read(PACK_LENGTH);

                String s = Hex2StringHelper.hexString(PACK_LENGTH);
                System.out.println(s);

                ObjectInputStream objectInputStream = new CompactObjectInputStream(
                        dataInputStream, new ClassLoaderClassResolver(this.getClass().getClassLoader()));
                Object object = objectInputStream.readObject();
                connectNotify.newMessageSuccess(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            connectNotify.newMessageFailed();
            closeConnect();
        }
    }

    void sleepMills(int mills) {
        try {
            TimeUnit.MILLISECONDS.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    DataInputStream getDataStream() {
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            closeConnect();
        } finally {
            return dataInputStream;
        }
    }

    void closeConnect() {
        if (client != null) {
            try {
                client.shutdownOutput();
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                client = null;
            }
        }
    }
}
