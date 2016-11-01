package phoswald.ssh.client;

import com.jcraft.jsch.Logger;

class SshLogger implements Logger {

    private static final String[] LEVELS = { "DEBUG", "INFO", "WARN", "ERROR", "FATAL" };

    private static volatile int minLevel = DEBUG;

    static void setLevel(String level) {
        for(int i = 0; i < LEVELS.length; i++) {
            if(LEVELS[i].equals(level)) {
                minLevel = i;
                return;
            }
        }
        throw new IllegalArgumentException("Invalid log level: " + minLevel);
    }

    @Override
    public boolean isEnabled(int level) {
        return level >= minLevel;
    }

    @Override
    public void log(int level, String message) {
        System.out.println(String.format("[%-5s] com.jcraft.jsch: %s", LEVELS[level], message));
    }
}
