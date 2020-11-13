package com.midorix.pipos;

import org.apache.commons.io.FileUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pipos {

    final private static File RT_TABLES = new File("/etc/iproute2/rt_tables");

    final private static String PTY_OPTION = "%s %s --nolaunchpppd --nohostroute --loglevel 0 --timeout %d";
    final private static String IFACE_RPFILTER = "/proc/sys/net/ipv4/conf/%s/rp_filter";
    final private static File IP_FORWARD = new File("/proc/sys/net/ipv4/ip_forward");
    final private static File IP_DYNADDR = new File("/proc/sys/net/ipv4/ip_dynaddr");

    final private static List<String> DEFAULT_OPTIONS = Arrays.asList("debug", "lock", "noauth", "persist", "nobsdcomp", "nodeflate", "nodetach");


    final private static int BASE_FROM_NO = 200;
    final private static int BASE_TABLE_NO = 300;


    final private static File PPPD_BIN = new File("ext/pppd");
    final private static File PPTP_BIN = new File("ext/pptp");
    final private static int BAUD_RATE = 38400; //speed
    final private static int MTU = 1400;
    final private static int MRU = 1400;
    final private static int PACKET_TIMEOUT = 8; //Time to wait for reordered packets (0.01 to 10 secs)

    public enum AuthType {
        MPPE_128,
        UNKNOWN
    }

    private int no;
    private String ifname;
    private String tableName;
    private int tableNo;
    private File rpFilterFile;
    private int fromNo;
    private String server;
    private String username;
    private String password;
    private AuthType authType;
    private Process process;
    private int channel;
    private String tty;
    private String localAddr;
    private String remoteAddr;

    public Pipos(int no) throws IOException {
        this.no = no;
        this.ifname = String.format("pipos%d", this.no);
        this.tableName = String.format("pipos%d", this.no);
        this.tableNo = BASE_TABLE_NO + this.no;
        this.rpFilterFile = new File(String.format(IFACE_RPFILTER, this.ifname));
        this.fromNo = BASE_FROM_NO + this.no;

        boolean tableContain = false;
        List<String> lines = FileUtils.readLines(RT_TABLES, StandardCharsets.UTF_8);
        if (lines.size() > 0) {
            for (String tableLine : lines) {
                if (tableLine.contains("\t")) {
                    String[] spaceSplitted = tableLine.split("\t");
                    String hash = spaceSplitted[1];
                    if (hash.equals(this.tableName)) {
                        tableContain = true;
                    }
                }
            }
        }

        if (!tableContain) {
            String line = String.format("%d\t%s", this.tableNo, this.tableName);
            FileUtils.writeStringToFile(RT_TABLES, line + "\n", StandardCharsets.UTF_8, true);
        }

    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setCredentials(String username, String password, AuthType authType) {
        this.username = username;
        this.password = password;
        this.authType = authType;
    }


    public void connect() throws IOException, InterruptedException {
        List<String> params = new ArrayList<>();
        params.add(PPPD_BIN.getAbsolutePath());
        params.add("pty");
        params.add(String.format(PTY_OPTION, PPTP_BIN.getAbsolutePath(), server, PACKET_TIMEOUT));
        params.addAll(DEFAULT_OPTIONS);
        params.add("ipparam");   //todo ?
        params.add(this.ifname); //todo ?
        params.add("ifname");
        params.add(this.ifname);


        params.add("mtu");
        params.add(String.valueOf(MTU));
        params.add("mru");
        params.add(String.valueOf(MRU));

        if (authType.equals(AuthType.MPPE_128)) {
            params.add("require-mppe-128");
        }
        params.add("name");
        params.add(this.username);
        params.add("password");
        params.add(this.password);

        System.out.println(params);
        ProcessBuilder processBuilder = new ProcessBuilder(params);
        processBuilder.redirectErrorStream(true);
        process = processBuilder.start();


        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        System.out.println("aaa");

        while ((line = br.readLine()) != null) {
            if (line.contains("using channel")) {
                this.channel = Integer.parseInt(line.split(" ")[2]);
            } else if (line.contains("Connect:") && line.contains("<-->")) {
                this.tty = line.split(" ")[3];
            } else if (line.contains("local  IP address")) {
                this.localAddr = line.split(" ")[4];
            } else if (line.contains("remote IP address")) {
                this.remoteAddr = line.split(" ")[3];
                break;
            } else {
                System.out.println(line);
            }
        }

        Helpers.pinSwitcher(this.rpFilterFile, false);
        Helpers.pinSwitcher(IP_FORWARD, true);
        Helpers.pinSwitcher(IP_DYNADDR, true);

        Runtime.getRuntime().exec(String.format("ip route flush table %s", tableName));
        Runtime.getRuntime().exec(String.format("ip route add default via %s dev %s table %s", remoteAddr, ifname, tableName));
        Runtime.getRuntime().exec(String.format("ip rule add from %s pri %d table %s", localAddr, fromNo, tableName));
        Runtime.getRuntime().exec("ip route flush cache");



        //System.out.println(source);

        //sudo
        //sudo ip route del 172.16.36.1
        System.out.println("remote " + remoteAddr);
        System.out.println("local " + localAddr);



    }

    public void disconnect() throws IOException {
        process.destroy();
        process.destroyForcibly();

        Runtime.getRuntime().exec(String.format("ip route del %s", remoteAddr));
        Runtime.getRuntime().exec(String.format("ip rule del pref %d", fromNo));
        Runtime.getRuntime().exec(String.format("ip route flush table %s", tableName));
        Runtime.getRuntime().exec("ip route flush cache");
    }

    public void check() throws IOException {

        NetworkInterface ni = NetworkInterface.getByName(this.ifname);
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


    public static void main(String[] args) throws IOException, InterruptedException {
        Pipos p0 = new Pipos(0);
        p0.setServer("fr8.vpnbook.com");
        p0.setCredentials("vpnbook", "6b7wEa7", AuthType.MPPE_128);

        for (int i = 0; i <100 ; i++) {
            p0.connect();
            p0.check();
            p0.disconnect();
        }

    }


}
