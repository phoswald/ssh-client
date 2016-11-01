package phoswald.ssh.client.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import phoswald.ssh.client.SshFileTransfer;
import phoswald.ssh.client.SshSession;

public class SshClient {

    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Usage:");
            System.out.println("    $ java -cp ... phoswald.ssh.client.main.SshClient <host> <user> OPTION... COMMAND...");
            System.out.println("Options:");
            System.out.println("    -password=...      Sets the password (if not defined: RSA authentication is used)");
            System.out.println("    -log=<level>       Sets the log level, possible values are DEBUG (default), INFO, WARN, ERROR, FATAL");
            System.out.println("Commands:");
            System.out.println("    -exec=<command>    Executes the given shell command, redirecing stdio");
            System.out.println("    -ls=<directory>    Lists a directory to stdout");
            System.out.println("    -cat=<file>        Dumps a text file to stdout");
            return;
        }
        int port = 22;
        String host = args[0];
        String user = args[1];
        Optional<String> password = Optional.empty();
        List<BiConsumer<SshSession, SshFileTransfer>> actions = new ArrayList<>();
        for(int i = 2; i < args.length; i++) {
            if(args[i].startsWith("-password=")) {
                password = Optional.of(args[i].substring(10));
            } else if(args[i].startsWith("-log=")) {
                SshSession.setLogLevel(args[i].substring(5));
            } else if(args[i].startsWith("-exec=")) {
                String command = args[i].substring(6);
                actions.add((ssh, sftp) -> {
                    ssh.createCommand(command).
                        //setStdInput(System.in).
                        setStdOutput(System.out).
                        setStdError(System.err).
                        execute().
                        checkExitStatus();
                });
            } else if(args[i].startsWith("-ls=")) {
                String directory = args[i].substring(4);
                actions.add((ssh, sftp) -> {
                    sftp.list(directory).forEach(System.out::println);
                });
            } else if(args[i].startsWith("-cat=")) {
                String file = args[i].substring(5);
                actions.add((ssh, sftp) -> {
                    sftp.download(file, System.out);
                });
            } else {
                throw new IllegalArgumentException("Invalid argument: " + args[i]);
            }
        }

        try(SshSession ssh = createSession(host, port, user, password)) {
            System.out.println("whoami: " + ssh.createCommand("whoami").
                    execute().
                    checkExitStatus().
                    getStdOutAsString());
            System.out.println("pwd: " + ssh.createCommand("pwd").
                    execute().
                    checkExitStatus().
                    getStdOutAsString());
            try(SshFileTransfer sftp = ssh.openFileTransfer()) {
                actions.forEach(action -> action.accept(ssh, sftp));
            }
        }
    }

    private static SshSession createSession(String host, int port, String user, Optional<String> password) {
        if(password.isPresent()) {
            return SshSession.createPasswordSession(host, port, user, password.get());
        } else {
            return SshSession.createRsaSession(host, port, user);
        }
    }
}
