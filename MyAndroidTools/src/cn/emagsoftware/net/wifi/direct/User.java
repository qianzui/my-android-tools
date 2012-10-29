package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.util.Base64;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.MathUtilities;

public class User
{

    private static final String ACTION_CHARSET  = "UTF-8";
    private static final int    LISTENING_PORT  = 7001;

    private String              name            = null;

    private WifiConfiguration   preApConfig     = null;
    private int                 preWifiStaticIp = -1;
    private boolean             preWifiEnabled  = false;

    private Selector            selector        = null;
    private RemoteCallback      callback        = null;

    private SelectionKey        listeningKey    = null;

    private Handler             handler         = new Handler(Looper.getMainLooper());

    public User(String name, RemoteCallback callback) throws IOException
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
        selector = Selector.open();
        callback.bindSelector(selector);
        this.callback = callback;
        new Thread(callback).start();
    }

    public void listening(Context context,final ListeningCallback callback)
    {
        try
        {
            openAp(context, new WifiCallback(context)
            {
                @Override
                public void onWifiApEnabled()
                {
                    // TODO Auto-generated method stub
                    super.onWifiApEnabled();
                    try
                    {
                        acceptIfNecessary();
                        callback.onListening();
                    }catch(final IOException e)
                    {
                        try
                        {
                            closeAp(context, new CloseApCallback(){
                                @Override
                                 public void onClosed()
                                 {
                                     // TODO Auto-generated method stub
                                    callback.onError(e);
                                 }
                                 @Override
                                 public void onError()
                                 {
                                     // TODO Auto-generated method stub
                                     LogManager.logE(User.class, "close ap failed by 'onError()'.");
                                     callback.onError(e);
                                 }
                             });
                        }catch(ReflectHiddenFuncException e1)
                        {
                            LogManager.logE(User.class, "close ap failed.", e1);
                            callback.onError(e);
                        }
                    }
                }

                @Override
                public void onError()
                {
                    // TODO Auto-generated method stub
                    super.onError();
                    callback.onError(new RuntimeException("open ap failed by 'onError()'."));
                }
            });
        } catch (final ReflectHiddenFuncException e)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onError(e);
                }
            });
        }
    }

    private void openAp(Context context, WifiCallback callback) throws ReflectHiddenFuncException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        if (preApConfig == null)
        {
            preApConfig = wifiUtils.getWifiApConfiguration();
            try
            {
                preWifiStaticIp = Settings.System.getInt(context.getContentResolver(), "wifi_static_ip");
            } catch (SettingNotFoundException e)
            {
            }
            preWifiEnabled = wifiUtils.isWifiEnabled();
        }
        wifiUtils.setWifiApEnabled(createDirectApConfig(context), true, callback, 0);
    }

    private WifiConfiguration createDirectApConfig(Context context)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        WifiConfiguration apconfig = new WifiConfiguration();
        String apName = null;
        try
        {
            apName = "GHFY_" + Base64.encode(name.getBytes(ACTION_CHARSET)) + "_" + MathUtilities.Random(10000);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        String apPassword = null;
        apconfig.SSID = apName;
        apconfig.BSSID = wifiUtils.getConnectionInfo().getMacAddress();
        apconfig.preSharedKey = apPassword;
        apconfig.allowedAuthAlgorithms.clear();
        apconfig.allowedAuthAlgorithms.set(0);
        apconfig.allowedProtocols.clear();
        apconfig.allowedProtocols.set(1);
        apconfig.allowedProtocols.set(0);
        apconfig.allowedKeyManagement.clear();
        if (apPassword == null)
            apconfig.allowedKeyManagement.set(0);
        else
            apconfig.allowedKeyManagement.set(1);
        apconfig.allowedPairwiseCiphers.clear();
        apconfig.allowedPairwiseCiphers.set(2);
        apconfig.allowedPairwiseCiphers.set(1);
        apconfig.allowedGroupCiphers.clear();
        apconfig.allowedGroupCiphers.set(3);
        apconfig.allowedGroupCiphers.set(2);
        apconfig.hiddenSSID = false;
        return apconfig;
    }
    
    private void acceptIfNecessary() throws IOException
    {
        if(listeningKey != null) return;
        ServerSocketChannel serverChannel = null;
        try
        {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(LISTENING_PORT));
            listeningKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            try
            {
                if (serverChannel != null)
                    serverChannel.close();
            }catch(IOException e1)
            {
                LogManager.logE(User.class, "close server socket channel failed.", e1);
            }
            throw e;
        }
    }

    private void closeAp(Context context, final CloseApCallback callback) throws ReflectHiddenFuncException
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiApEnabled(null, false, new WifiCallback(context)
        {
            @Override
            public void onWifiApDisabled()
            {
                // TODO Auto-generated method stub
                super.onWifiApDisabled();
                if (preApConfig != null)
                {
                    try
                    {
                        wifiUtils.setWifiApConfiguration(preApConfig);
                    } catch (ReflectHiddenFuncException e)
                    {
                        LogManager.logE(User.class, "restore ap config failed.", e);
                    }
                    if (preWifiStaticIp != -1)
                        Settings.System.putInt(context.getContentResolver(), "wifi_static_ip", preWifiStaticIp);
                    if (preWifiEnabled)
                        wifiUtils.setWifiEnabled(true, null, 0);
                    preApConfig = null;
                    preWifiStaticIp = -1;
                    preWifiEnabled = false;
                }
                callback.onClosed();
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError();
            }
        }, 0);
    }

    private interface CloseApCallback
    {
        public void onClosed();

        public void onError();
    }

    public void finishListening(Context context,final FinishListeningCallback callback)
    {
        try
        {
            if (listeningKey != null)
            {
                listeningKey.cancel();
                ((ServerSocketChannel) listeningKey.channel()).close();
                listeningKey = null;
            }
            if(preApConfig != null)
            {
                closeAp(context, new CloseApCallback()
                {
                    @Override
                    public void onClosed()
                    {
                        // TODO Auto-generated method stub
                        callback.onFinished();
                    }
                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        callback.onError(new RuntimeException("close ap failed by 'onError()'."));
                    }
                });
            }
        }catch(final Exception e)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onError(e);
                }
            });
        }
    }
    
    public void scanUsers(Context context, final ScanUsersCallback callback)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.startScan(new WifiCallback(context)
        {
            @Override
            public void onScanResults(List<ScanResult> scanResults)
            {
                // TODO Auto-generated method stub
                super.onScanResults(scanResults);
                List<RemoteUser> callbackVal = new ArrayList<RemoteUser>();
                for (ScanResult result : scanResults)
                {
                    String ssid = result.SSID;
                    if (ssid.startsWith("GHFY_"))
                    {
                        String[] ssidInfo = ssid.split("_");
                        if (ssidInfo.length == 3)
                        {
                            RemoteUser user = null;
                            try
                            {
                                user = new RemoteUser(new String(Base64.decode(ssidInfo[1]), ACTION_CHARSET));
                            } catch (UnsupportedEncodingException e)
                            {
                                throw new RuntimeException(e);
                            }
                            user.setScanResult(result);
                            callbackVal.add(user);
                        }
                    }
                }
                callback.onScanned(callbackVal);
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError();
            }
        }, 0);
    }

    public void connectToRemoteAp(Context context, final RemoteUser user, final ConnectToRemoteApCallback callback)
    {
        ScanResult result = user.getScanResult();
        if (result == null)
            throw new IllegalStateException("can only connect to ap which has been found by scanning.");
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.connect(result, null, new WifiCallback(context)
        {
            @Override
            public void onNetworkConnected(WifiInfo wifiInfo)
            {
                // TODO Auto-generated method stub
                super.onNetworkConnected(wifiInfo);
                int apIp = wifiUtils.getWifiManager().getDhcpInfo().serverAddress;
                String apIpStr = String.format("%d.%d.%d.%d", (apIp & 0xff), (apIp >> 8 & 0xff), (apIp >> 16 & 0xff), (apIp >> 24 & 0xff));
                user.setIp(apIpStr);
                callback.onConnected(user);
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError(user);
            }
        }, 0);
    }

    public void connectToUser(RemoteUser user) throws IOException
    {
        String ip = user.getIp();
        if (ip == null)
            throw new IllegalStateException("can only connect to user which ap has already been connected,call 'connectToRemoteAp(...)' first.");
        SocketChannel sc = SocketChannel.open();
        SocketChannel transferSc = SocketChannel.open();
        sc.configureBlocking(false);
        transferSc.configureBlocking(false);
        sc.connect(new InetSocketAddress(ip, LISTENING_PORT));
        transferSc.connect(new InetSocketAddress(ip, LISTENING_PORT));
        user.setKey(sc.register(selector, SelectionKey.OP_CONNECT, user));
        user.setTransferKey(transferSc.register(selector, SelectionKey.OP_CONNECT, user));
    }

    public void sendTransferRequest(RemoteUser user, File file)
    {
        SelectionKey key = user.getKey();
        if (key == null)
            throw new IllegalStateException("the input user has not been connected already.");
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_request", file });
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void replyTransferRequest(RemoteUser user, boolean allow, String path)
    {
        SelectionKey key = user.getKey();
        if (key == null)
            throw new IllegalStateException("the input user has not been connected already.");
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_reply", allow, path });
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void sendTransfer(final RemoteUser user, final File file)
    {
        SelectionKey transferKey = user.getTransferKey();
        if (transferKey == null)
            throw new IllegalStateException("the input user has not been connected already.");
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                callback.onTransferProgress(user, file.getAbsolutePath(), null, file.length(), 0);
            }
        });
        transferKey.attach(new Object[] { user, "transfer", file });
        transferKey.interestOps(SelectionKey.OP_WRITE);
    }

    public void close() throws IOException
    {
        selector.close();
    }

}