package com.att.ingestion.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtils extends org.apache.commons.io.FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
    private static final int BUF_SIZE = 16 * 1024;

    public static int DEFAULT_READ_LIMIT = 2 * 1024 * 1024 ; //Two megabytes
    /**
     * Delete files in list. If file is a directory, delete it and all sub-directories.
     *
     * @param files
     */
    public static void forceDeleteFiles(List<File> files) {
        for (File file : files) {
            try {
                if (file != null) {
                    FileUtils.forceDelete(file);
                }
            } catch (IOException ioe) {
                log.warn(ioe.getMessage());
            }
        }
    }

    /**
     * Write the inputstream to a file
     *
     * @param inputStream
     * @param destFile
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
	public static void writeToFile(InputStream inputStream, File destFile) throws IOException {
        if ((destFile.exists()) && (destFile.isDirectory())) {
            throw new IOException(String.format("Destination %s exists but is a directory", destFile.getAbsolutePath()));
        }
        FileOutputStream fos = null;
        ReadableByteChannel input = null;
        FileChannel output = null;
        try {
            fos = new FileOutputStream(destFile);
            input = Channels.newChannel(inputStream);
            output = fos.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUF_SIZE);
            while (input.read(buffer) != -1) {
                buffer.flip();
                output.write(buffer);
                buffer.compact();
            }
            // EOF will leave data in buffer, make sure the buffer is fully flushed.
            buffer.flip();
            while (buffer.hasRemaining()) {
                output.write(buffer);
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(input);
        }
    }

    public static boolean fileExists(String filePath) {
        File f = new File(filePath);
        if(f.exists() && f.isFile()) {
            return true;
        }
        return false;
    }

    /**
     * Reads the last "Some Bytes" contents of a file into a String using the default encoding for the VM.
     * The file is always closed.
     *
     * Test Case: get last 4 megabytes of text file.
     *
     * @param file  the file to read, must not be {@code null}
     * @param lastBytesLimit last byte limit (For example: Read last 4 bytes)
     * @return the file contents, never {@code null}
     * @throws IOException in case of an I/O error
     * @since 1.3.1
     */
    @SuppressWarnings("deprecation")
	public static String readFileToString(File file, long lastBytesLimit) throws IOException {

        StringBuilderWriter sw = new StringBuilderWriter();

        RandomAccessFile rfa = null;
        try {

            rfa = new RandomAccessFile(file, "r");

            log.debug("File length:"+file.length());
            log.debug("Read Last Bytes:"+ lastBytesLimit);

            long readFromPos = file.length() - ( lastBytesLimit );
            if( readFromPos <= 0){
                readFromPos = 0;
            }
            rfa.seek(readFromPos);

            String line;

            while ((line = rfa.readLine()) != null) {
                sw.write(line);
                sw.write(System.lineSeparator());
            }

        } finally {
            IOUtils.closeQuietly(rfa);
            sw.close();
        }
        return sw.toString();
    }
}