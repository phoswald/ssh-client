package phoswald.ssh.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class SshFileInfo {

    private static final int S_IFMT   = 0xf000;
    private static final int S_IFIFO  = 0x1000;
    private static final int S_IFCHR  = 0x2000;
    private static final int S_IFDIR  = 0x4000;
    private static final int S_IFBLK  = 0x6000;
    private static final int S_IFREG  = 0x8000;
    private static final int S_IFLNK  = 0xa000;
    private static final int S_IFSOCK = 0xc000;

    private static final int S_ISUID = 04000; // set user ID on execution
    private static final int S_ISGID = 02000; // set group ID on execution
    private static final int S_ISVTX = 01000; // sticky bit

    private static final int S_IRUSR = 00400; // read by owner
    private static final int S_IWUSR = 00200; // write by owner
    private static final int S_IXUSR = 00100; // execute/search by owner

    private static final int S_IRGRP = 00040; // read by group
    private static final int S_IWGRP = 00020; // write by group
    private static final int S_IXGRP = 00010; // execute/search by group

    private static final int S_IROTH = 00004; // read by others
    private static final int S_IWOTH = 00002; // write by others
    private static final int S_IXOTH = 00001; // execute/search by others

    private final String name;
    private final long size;
    private final int perms;
    private final int uid;
    private final int gid;
    private final Instant mtime;

    public SshFileInfo(String name, long size, int perms, int uid, int gid, Instant mtime) {
        this.name = name;
        this.size = size;
        this.perms = perms;
        this.uid = uid;
        this.gid = gid;
        this.mtime = mtime;
    }

    @Override
    public String toString() {
        String mtimeString = LocalDateTime.ofInstant(mtime, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
        return String.format("%s %5d %5d %12d %s %s", permissions(), uid, gid, size, mtimeString, name);
    }

    public String name() {
        return name;
    }

    public long size() {
        return size;
    }

    public int perms() {
        return perms;
    }

    public int uid() {
        return uid;
    }

    public int gid() {
        return gid;
    }

    public Instant mtime() {
        return mtime;
    }

    public boolean isReg() {
        return (perms & S_IFMT) == S_IFREG;
    }

    public boolean isDir() {
        return (perms & S_IFMT) == S_IFDIR;
    }

    public boolean isChr() {
        return (perms & S_IFMT) == S_IFCHR;
    }

    public boolean isBlk() {
        return (perms & S_IFMT) == S_IFBLK;
    }

    public boolean isFifo() {
        return (perms & S_IFMT) == S_IFIFO;
    }

    public boolean isLink() {
        return (perms & S_IFMT) == S_IFLNK;
    }

    public boolean isSock() {
        return (perms & S_IFMT) == S_IFSOCK;
    }

    public String permissions() {
        StringBuilder sb = new StringBuilder();

        sb.append(isDir() ? 'd' : isLink() ? 'l' : '-');

        sb.append((perms & S_IRUSR) != 0 ? 'r' : '-');
        sb.append((perms & S_IWUSR) != 0 ? 'w' : '-');
        sb.append((perms & S_ISUID) != 0 ? 's' : (perms & S_IXUSR) != 0 ? 'x' : '-');

        sb.append((perms & S_IRGRP) != 0 ? 'r' : '-');
        sb.append((perms & S_IWGRP) != 0 ? 'w' : '-');
        sb.append((perms & S_ISGID) != 0 ? 's' : (perms & S_IXGRP) != 0 ? 'x' : '-');

        sb.append((perms & S_IROTH) != 0 ? 'r' : '-');
        sb.append((perms & S_IWOTH) != 0 ? 'w' : '-');
        sb.append((perms & S_IXOTH) != 0 ? 'x' : '-');

        return sb.toString();
      }
}
