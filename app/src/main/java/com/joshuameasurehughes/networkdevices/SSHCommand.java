package com.joshuameasurehughes.networkdevices;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;

public class SSHCommand {

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port,
            String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();
        } catch (Exception e) {
            throw new ConnectException();
        }


        // SSH Channel
        ChannelExec channel = (ChannelExec)
                session.openChannel("exec");
        channel.setCommand(command);

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
        session.disconnect();

        if (command.equals("sudo halt")) { // Verify off
            try {
                session.connect();
                session.disconnect();
                return "Shutdown failed\n";
            } catch (Exception e) {
                return "Shutdown successful\n";
            }
        }

        //if (errorBuffer.toString("UTF-8")=="") return errorBuffer.toString("UTF-8");
        return outputBuffer.toString("UTF-8");
    }
}
