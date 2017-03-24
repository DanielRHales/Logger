package com.logging;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Daniel
 */
@SuppressWarnings("unused")
public class Logger {

    /**
     * The System User Home
     */
    private String USER_HOME = System.getProperty("user.home");

    /**
     * The Main Directory
     */
    private final File MAIN_DIRECTORY = new File(USER_HOME, getDirectoryName());

    /**
     * The Directory in which the Log file will be stored
     */
    private final File ERROR_DIRECTORY = new File(MAIN_DIRECTORY, "Errors");

    /**
     * The Log file in which the Exception errors will be stored in GZIP format
     */
    private final File LOG = getFile();

    /**
     * The Exceptions error Date format
     */
    private final SimpleDateFormat FORMAT = new SimpleDateFormat("d/MM/y h:m:s.S a zzzz");

    /**
     * Always Logs an Exception to the {@link #LOG} File
     *
     * @param clazz     - The class in which the Exception was caught / thrown
     * @param level     - The Level of severity for the Exception thrown
     * @param message   - The Exception specific message
     * @param exception - The Exception caught / thrown
     */
    public static void log(final Class clazz, final java.util.logging.Level level, final String message, final Exception exception) {
        log(clazz, level, message, exception, true);
    }

    /**
     * Logs an Exception to the {@link #LOG} File if {@param write} {@return true}
     *
     * @param clazz     - The class in which the Exception was caught / thrown
     * @param level     - The Level of severity for the Exception thrown
     * @param message   - The Exception specific message
     * @param exception - The Exception caught / thrown
     * @param write     - {@link #write(Class, Exception)} if {@return true}
     */
    @SuppressWarnings("all")
    public static void log(final Class clazz, final java.util.logging.Level level, final String message, final Exception exception, boolean write) {
        java.util.logging.Logger.getLogger(clazz.getName()).log(level, message, exception);
        if (write) {
            try {
                getInstance().write(clazz, exception);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gets the {@link #MAIN_DIRECTORY} name from the Resource file Path.gz
     *
     * @return - The {@link #MAIN_DIRECTORY} name
     */
    private String getDirectoryName() {
        try {
            DataInputStream stream = new DataInputStream(new GZIPInputStream(Logger.class.getResourceAsStream("/Path.gz")));
            final String directory = stream.readUTF();
            stream.close();
            return directory;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Writes an Exception to the {@link #LOG} File in GZIP format
     * @param clazz - The Class in which the Exception was caught / thrown
     * @param exception - The Exception caught / thrown to parse to the {@link #LOG} File
     * @throws IOException - if Unable to create or write to the {@link #LOG} File
     */
    private void write(Class clazz, Exception exception) throws IOException {
        if (LOG == null) {
            return;
        }
        if (!ERROR_DIRECTORY.isDirectory()) {
            if (!ERROR_DIRECTORY.mkdirs()) {
                return;
            }
        }
        if (!LOG.exists()) {
            if (!LOG.createNewFile()) {
                return;
            }
        }
        DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(LOG, true)));
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        dataOutputStream.writeUTF(String.format("Date: %s\nClass: %s\nStack Element: %s\nException: %s", FORMAT.format(System.currentTimeMillis()), clazz.getCanonicalName(), Arrays.toString(exception.getStackTrace()), stringWriter.getBuffer().toString()));
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    /**
     * Gets the Program Specific File to create a Logger
     *
     * @return - The {@link #LOG} File to parse Exceptions to
     */
    private File getFile() {
        try {
            final String name = new File(Logger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            return new File(ERROR_DIRECTORY, String.format("%s.log", name.replace(".", "-")));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Private {@link Logger} Constructor to prevent Logging Instantiation
     */
    private Logger() {

    }

    /**
     * The Logger Instance Getter
     *
     * @return - The Logging Instance Singleton
     */
    private static Logger getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * private static Instance Holder to prevent Accessing the {@link InstanceHolder#INSTANCE}
     */
    private static class InstanceHolder {
        /**
         * Logger Instance Singleton
         */
        private static final Logger INSTANCE = new Logger();
    }

}
