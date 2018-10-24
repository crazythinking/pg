package net.engining.pg.support.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.engining.pg.support.stream.UnsafeByteArrayOutputStream;

/**
 * Java 调用外部命令，并获取输出 (解决了 IO 阻塞问题).
 */
public abstract class ShellUtil {

    /**
     * shell("ls -l")
     */
    public static Result shell(String command) {
        return shell(command, null, null);
    }

    public static Result shell(String command, File directory, Map<String, String> envp) {
        if (SystemUtil.IS_OS_WINDOWS) {
            return execute(directory, envp, "cmd.exe", "/c", command);
        } else {
            return execute(directory, envp, "/bin/sh", "-c", command);
        }
    }

    /**
     * shell("ls", "-l")
     */
    public static Result execute(String... command) {
        return execute(null, null, command);
    }

    public static Result execute(File directory, Map<String, String> envp, String... command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (directory != null) {
            pb.directory(directory);
        }
        if (envp != null) {
            pb.environment().putAll(envp);
        }

        Result result = new Result();
        Process p = null;
        try {
            p = pb.start();
            new InputStreamReadThread("shell-exec-stdout", p.getInputStream(), result.stdout).start();
            new InputStreamReadThread("shell-exec-stderr", p.getErrorStream(), result.stderr).start();
            p.waitFor();
            result.exitValue = p.exitValue();
        } catch (Exception e) {
            result.error = e;
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        return result;
    }

    public static final class Result {
        int exitValue = -99;
        UnsafeByteArrayOutputStream stdout = new UnsafeByteArrayOutputStream();
        UnsafeByteArrayOutputStream stderr = new UnsafeByteArrayOutputStream();
        Exception error;

        public boolean success() {
            return error == null && exitValue == 0;
        }

        public int exitValue() {
            return exitValue;
        }

        public String stdout() {
            return stdout.toString();
        }

        public String stdout(String charsetName) {
            try {
                return stdout.toString(charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public String stderr() {
            return stderr.toString();
        }

        public String stderr(String charsetName) {
            try {
                return stderr.toString(charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public Exception getException() {
            return error;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("exitValue: ").append(exitValue).append('\n');
            sb.append("stdout: ").append(stdout()).append('\n');
            sb.append("stderr: ").append(stderr()).append('\n');
            sb.append("error: ").append(error).append('\n');
            return sb.toString();
        }

        public String toString(String charsetName) {
            StringBuilder sb = new StringBuilder();
            sb.append("exitValue: ").append(exitValue).append('\n');
            sb.append("stdout: ").append(stdout(charsetName)).append('\n');
            sb.append("stderr: ").append(stderr(charsetName)).append('\n');
            sb.append("error: ").append(error).append('\n');
            return sb.toString();
        }
    }


    static final class InputStreamReadThread extends Thread {
        final InputStream is;
        final OutputStream os;

        InputStreamReadThread(String name, InputStream is, OutputStream os) {
            super(name);
            this.setDaemon(true);
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                int n = is.read();
                while (n > -1) {
                    os.write(n);
                    n = is.read();
                }
            } catch (IOException e) {
                // hit stream eof, do nothing
            } finally {
                UnsafeIOUtil.closeQuietly(is);
            }
        }
    }
}
