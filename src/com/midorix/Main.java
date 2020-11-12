package com.midorix;

import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        //sudo /usr/sbin/pppd call hmavpn debug logfd 0 nodetach
        //ppp0 38400 172.16.36.11 172.16.36.1 hmavpn


/*

-----------------------------------------------------------

            $ cat /etc/ppp/peers/hmavpn
        pty "pptp fr8.vpnbook.com --nolaunchpppd"
        lock
        noauth
        nobsdcomp
        nodeflate
        name vpnbook
        ipparam hmavpn
        remotename hmavpn
        require-mppe-128
        persist

-----------------------------------------------------------

            $ cat /etc/ppp/chap-secrets
        vpnbook        hmavpn        6b7wEa7        *

-----------------------------------------------------------

            # pon hmavpn2 debug logfd 0 nodetach

            # pppstats: for info

-----------------------------------------------------------

*/

        NetworkInterface ni = NetworkInterface.getByName("ppp0");
        InetAddress source = ni.getInetAddresses().nextElement();

        //System.out.println(source);


        RequestConfig config = RequestConfig.custom()
                //.setLocalAddress(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 80}))
                .setCircularRedirectsAllowed(true)
                .setLocalAddress(source)
                .setConnectionRequestTimeout(1000)
                .setConnectTimeout(2000)
                .setSocketTimeout(2000)
                .setExpectContinueEnabled(true)
                .build();
        HttpGet httpGet = new HttpGet("http://checkip.amazonaws.com");


        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(5);

        CloseableHttpClient httpclient = HttpClients.custom()
                .disableAuthCaching()
                .setDefaultRequestConfig(config)
                .disableAutomaticRetries()
                .disableConnectionState()
                .disableContentCompression()
                .disableRedirectHandling()
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                .setConnectionManager(cm)
                .build();

        System.out.println("...");

        CloseableHttpResponse response = httpclient.execute(httpGet);
        StatusLine sl = response.getStatusLine();
        System.out.println(sl);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
    }
}
