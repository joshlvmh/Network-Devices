package com.joshuameasurehughes.networkdevices;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class SSHCommand {

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channel = (ChannelExec)
                session.openChannel("exec");
        channel.setCommand("reboot");

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();

        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                outputBuffer.write(tmp, 0, i);
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                errorBuffer.write(tmp, 0, i);
            }
            if (channel.isClosed()) {
                if ((in.available() > 0) || (err.available() > 0)) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        System.out.println("output: " + outputBuffer.toString("UTF-8"));
        System.out.println("error: " + errorBuffer.toString("UTF-8"));

        channel.disconnect();
        if (errorBuffer.toString("UTF-8")=="") return errorBuffer.toString("UTF-8");
        return outputBuffer.toString("UTF-8");
    }
}
