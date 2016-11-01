package phoswald.ssh.client;

import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SshFileTransfer implements AutoCloseable {

    private final ChannelSftp channel;

    SshFileTransfer(Session session) {
        try {
            this.channel = (ChannelSftp) session.openChannel("sftp");
            this.channel.connect();
        } catch (JSchException e) {
            throw new SshException("Failed to open file transfer channel.", e);
        }
    }

    @Override
    public void close() {
        channel.disconnect();
    }

    public List<SshFileInfo> list(String path) {
        try {
           return Stream.of(channel.ls(path).toArray()).
                   map(obj -> createFileInfo((LsEntry) obj)).
                   filter(info -> !info.name().equals(".") && !info.name().equals("..")).
                   collect(Collectors.toList());
        } catch (SftpException e) {
            throw new SshException("Failed to list directory: " + path, e);
        }
    }

    public void download(String file, OutputStream stream) {
        try {
            channel.get(file, stream);
        } catch (SftpException e) {
            throw new SshException("Failed to download file: " + file, e);
        }
    }

    private SshFileInfo createFileInfo(LsEntry entry) {
        SftpATTRS attrs = entry.getAttrs();
        return new SshFileInfo(
                entry.getFilename(),
                attrs.getSize(),
                attrs.getPermissions(),
                attrs.getUId(),
                attrs.getGId(),
                Instant.ofEpochSecond(attrs.getMTime()));
    }
}
