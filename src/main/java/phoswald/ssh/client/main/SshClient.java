package phoswald.ssh.client.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import phoswald.ssh.client.SshFileTransfer;
import phoswald.ssh.client.SshSession;

public class SshClient {

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage:");
            System.out.println("    $ java -cp ... phoswald.ssh.client.main.SshClient [user[:password]@]host[:port] OPTION... COMMAND...");
            System.out.println("Options:");
            System.out.println("    -log=<level>       Sets the log level, possible values are DEBUG (default), INFO, WARN, ERROR, FATAL");
            System.out.println("Commands:");
            System.out.println("    exec=<command>     Executes the given shell command, redirecing stdio");
            System.out.println("    ls=<directory>     Lists a directory to stdout");
            System.out.println("    cat=<file>         Dumps a text file to stdout");
            return;
        }
        List<BiConsumer<SshSession, SshFileTransfer>> actions = new ArrayList<>();
        for(int i = 1; i < args.length; i++) {
            if(args[i].startsWith("-log=")) {
                SshSession.setLogLevel(args[i].substring(5));
            } else if(args[i].startsWith("exec=")) {
                String command = args[i].substring(5);
                actions.add((ssh, sftp) -> {
                    ssh.createCommand(command).
                        //setStdInput(System.in).
                        setStdOutput(System.out).
                        setStdError(System.err).
                        execute().
                        checkExitStatus();
                });
            } else if(args[i].startsWith("ls=")) {
                String directory = args[i].substring(3);
                actions.add((ssh, sftp) -> {
                    sftp.list(directory).forEach(System.out::println);
                });
            } else if(args[i].startsWith("cat=")) {
                String file = args[i].substring(4);
                actions.add((ssh, sftp) -> {
                    sftp.download(file, System.out);
                });
            } else {
                throw new IllegalArgumentException("Invalid argument: " + args[i]);
            }
        }

        try(SshSession ssh = SshSession.createSession(args[0])) {
            System.out.println("whoami: " + ssh.createCommand("whoami").
                    execute().
                    checkExitStatus().
                    getStdOutAsString().trim());
            System.out.println("pwd: " + ssh.createCommand("pwd").
                    execute().
                    checkExitStatus().
                    getStdOutAsString().trim());
            try(SshFileTransfer sftp = ssh.openFileTransfer()) {
                actions.forEach(action -> action.accept(ssh, sftp));
            }
        }
    }
}
