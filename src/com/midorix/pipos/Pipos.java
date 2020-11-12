package com.midorix.pipos;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pipos {


    final private static String PTY_OPTION = "pty \"%s %s --nolaunchpppd\"";
    final private static File CONFIG_DIR = new File("/tmp/pipos/");
    final private static File PEERS_DIR = new File(CONFIG_DIR, "peers");

    final private static List<String> DEFAULT_OPTIONS = Arrays.asList("lock", "noauth", "persist", "nobsdcomp", "nodeflate", "nolog", "nologfd");

    final private static File PPPD_BIN = new File("ext/pppd");
    final private static File PPTP_BIN = new File("ext/pptp");
    final private static int BAUD_RATE = 38400;
    final private static String CONFIG_CHARSET = "UTF-8";

    private int no;
    private int channel;
    private String ifname;
    private String tty;
    private InetAddress localAddr;
    private InetAddress remoteAddr;
    private Credential credential;
    private File configFile;
    private String credentialHash;

    public Pipos(int no, String server, Credential credential) throws IOException {
        this.no = no;
        this.credential = credential;

        credentialHash = DigestUtils.md5Hex(String.format("%s+%s", this.credential.username, this.credential.password));


        List<String> configOptions = new ArrayList<>();

        configOptions.add(String.format(PTY_OPTION, PPTP_BIN.getAbsolutePath(), server));
        configOptions.addAll(DEFAULT_OPTIONS);

        if (this.credential.authType.equals(Credential.AuthType.MPPE_128)) {
            configOptions.add("require-mppe-128");
        }

        configOptions.add(String.format("name %s", this.credential.username));
        configOptions.add(String.format("ipparam %s", this.credentialHash));
        configOptions.add(String.format("remotename %s", this.credentialHash));


        StringBuilder configBuilder = new StringBuilder();
        int optionsSize = configOptions.size();
        for (int i = 0; i < optionsSize; i++) {
            configBuilder.append(configOptions.get(i));
            if (i < optionsSize - 1) {
                configBuilder.append("\n");
            }
        }

        String config = configBuilder.toString();

        System.out.println(config);
        System.out.println("end-config");

        String configHash = DigestUtils.md5Hex(config);

        configFile = new File(PEERS_DIR, configHash);

        if (configFile.exists()) {
            System.out.println("var");
        } else {
            System.out.println("yok");

            //todo: try
            FileUtils.writeStringToFile(configFile, config, CONFIG_CHARSET);
        }

    }


    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        Pipos p = new Pipos(0, "server2.com", new Credential("alfa", "beta", Credential.AuthType.MPPE_128));


    }


}
