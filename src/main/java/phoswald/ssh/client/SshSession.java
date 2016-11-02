package phoswald.ssh.client;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshSession implements AutoCloseable {

    static {
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch.setLogger(new SshLogger());
    }

    private final Session session;

    private SshSession(Session session) {
        this.session = session;
    }

    public static SshSession createSession(String url) {
        String host;
        String user;
        String password = null;
        int port = 22;
        if(url.contains("@")) {
            host = url.substring(url.indexOf('@') + 1);
            user = url.substring(0, url.indexOf('@'));
            if(user.contains(":")) {
                password = user.substring(user.indexOf(':') + 1);
                user = user.substring(0, user.indexOf(':'));
            }
        } else {
            host = url;
            user = System.getProperty("user.name");
        }
        if(host.contains(":")) {
            port = Integer.parseInt(host.substring(host.indexOf(':') + 1));
            host = host.substring(0, host.indexOf(':'));
        }
        if(password == null) {
            return createRsaSession(host, port, user);
        } else {
            return createPasswordSession(host, port, user, password);
        }
    }

    public static SshSession createRsaSession(String host, int port, String user) {
        Path home = Paths.get(System.getProperty("user.home"));
        return createRsaSession(host, port, user, home.resolve(".ssh/id_rsa"));
    }

    public static SshSession createRsaSession(String host, int port, String user, Path privateKey) {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.toAbsolutePath().toString());
            Session session = jsch.getSession(user, host, port);
            session.setServerAliveInterval(10000);
            session.setServerAliveCountMax(1000000);
            session.connect();
            return new SshSession(session);
        } catch (JSchException e) {
            throw new SshException("Failed to open session to " + user + "@" + host + ":" + port + " (authentication=rsa).", e);
        }
    }

    public static SshSession createPasswordSession(String host, int port, String user, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setServerAliveInterval(10000);
            session.setServerAliveCountMax(1000000);
            session.connect();
            return new SshSession(session);
        } catch (JSchException e) {
            throw new SshException("Failed to open session to " + user + "@" + host + ":" + port + " (authentication=password)." , e);
        }
    }

    public static void setLogLevel(String level) {
        SshLogger.setLevel(level);
    }

    @Override
    public void close() {
        session.disconnect();
    }

    public SshCommand createCommand(String command) {
        return new SshCommand(session, command);
    }

    public SshFileTransfer openFileTransfer() {
        return new SshFileTransfer(session);
    }
}
