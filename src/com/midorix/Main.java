package com.midorix;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
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

            $ cat /etc/ppp/chap-secrets.pacsave
        vpnbook        hmavpn        6b7wEa7        *

-----------------------------------------------------------

            # pon hmavpn2 debug logfd 0 nodetach

            # pppstats: for info

-----------------------------------------------------------

*/
        NetworkInterface ni = NetworkInterface.getByName("ppp0");
        InetAddress source = ni.getInetAddresses().nextElement();

        System.out.println(source);

        SSLContext sslContext = SSLContext.getInstance("SSL");

// set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers =============");
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkClientTrusted =============");
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkServerTrusted =============");
            }
        } }, new SecureRandom());

        SSLSocketFactory sf = new SSLSocketFactory(sslContext);
        Scheme httpsScheme = new Scheme("https", 443, sf);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);


        RequestConfig config = RequestConfig.custom()
                //.setLocalAddress(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 80}))
                .setLocalAddress(source)
                .setConnectTimeout(7000)
                .build();
        HttpGet httpGet = new HttpGet("http://checkip.amazonaws.com/");
        httpGet.setConfig(config);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println(responseBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
