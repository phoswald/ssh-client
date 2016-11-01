package phoswald.ssh.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshCommand {

    private final Session session;
    private final String command;
    private InputStream stdin;
    private OutputStream stdout;
    private OutputStream stderr;
    private int exitStatus = -1;

    SshCommand(Session session, String command) {
        this.session = session;
        this.command = command;
        this.stdin = new ByteArrayInputStream(new byte[0]);
        this.stdout = new ByteArrayOutputStream();
        this.stderr = stdout;
    }

    public SshCommand setStdInput(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    public SshCommand setStdOutput(OutputStream stdout) {
        this.stdout = stdout;
        return this;
    }

    public SshCommand setStdError(OutputStream stderr) {
        this.stderr = stderr;
        return this;
    }

    public SshCommand execute() {
        ChannelExec channel = openChannel();
        try {
            run(channel);
            exitStatus = channel.getExitStatus();
            return this;
        } finally {
            channel.disconnect();
        }
    }

    public SshCommand checkExitStatus() {
        if(exitStatus != 0) {
            throw new SshException("Command failed with exit status " + exitStatus + ": " + command);
        }
        return this;
    }

    public String getStdOutAsString() {
        return new String(((ByteArrayOutputStream) stdout).toByteArray(), StandardCharsets.UTF_8);
    }

    private ChannelExec openChannel() {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setErrStream(stderr, true);
            channel.setOutputStream(stdout, true);
            channel.setInputStream(stdin, true);
            channel.setCommand(command);
            channel.connect();
            return channel;
        } catch (JSchException e) {
            throw new SshException("Failed to execute command: " + command, e);
        }
    }

    private void run(Channel channel) {
        while (!channel.isClosed()) {
            try {
                this.session.sendKeepAliveMsg();
            } catch (Exception ex) {
                throw new SshException(ex);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SshException(ex);
            }
        }
    }
}
