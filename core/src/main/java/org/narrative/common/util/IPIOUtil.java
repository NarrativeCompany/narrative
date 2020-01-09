package org.narrative.common.util;

import org.narrative.common.persistence.ObjectTriplet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class IPIOUtil {
    /**
     * Sends the contents of the specified file to the output stream
     *
     * @param filename the file to send
     * @param out the output stream to write the file
     * @exception FileNotFoundException if the file does not exist
     * @exception IOException if an I/O error occurs
     */
    private static final NarrativeLogger logger = new NarrativeLogger(IPIOUtil.class);

    public static void doStreamFileToOut(java.io.File file, java.io.OutputStream out, boolean closeOutputWhenDone) throws FileNotFoundException, IOException {
        // A FileInputStream is for bytes
        InputStream is = new FileInputStream(file);
        doStreamInputToOutputWithBuffering(is, out, closeOutputWhenDone);
    }

    public static boolean doCopyFile(java.io.File fileFrom, File fileTo, boolean overwrite) throws FileNotFoundException, IOException {
        return doCopyFile(fileFrom, fileTo, overwrite, false);
    }

    private static boolean doCopyFile(java.io.File fileFrom, File fileTo, boolean overwrite, boolean deleteOriginal) throws FileNotFoundException, IOException {
        // same, do nothing
        if (fileFrom.equals(fileTo)) {
            return true;
        }
        // move via rename if in same dir and deleting original
        if (deleteOriginal && IPUtil.isEqual(fileFrom.getParent(), fileTo.getParent())) {
            return fileFrom.renameTo(fileTo);
        }
        if (!overwrite && fileTo.exists()) {
            throw UnexpectedError.getRuntimeException(fileTo.getAbsolutePath() + " already exists, won't be overwritten");
        }
        Debug.assertMsg(logger, !fileTo.isDirectory(), "cant move to a directory");
        InputStream is = new FileInputStream(fileFrom);
        if (!fileTo.getParentFile().exists()) {
            if (!IPIOUtil.mkdirs(fileTo.getParentFile())) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed creating file directories!  File copy may not succeed? directory: " + fileTo.getParentFile().getAbsolutePath());
                }
            }
        }
        // create the destination file if it doesn't yet exist
        if (!fileTo.exists()) {
            if (!fileTo.createNewFile()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed creating new file!  File copy may not succeed? file: " + fileTo.getAbsolutePath());
                }
            }
        }
        OutputStream out = new FileOutputStream(fileTo);
        try {
            doStreamInputToOutputWithBuffering(is, out, true);
        } catch (Throwable t) {
            // cleanup
            fileTo.delete();
            throw UnexpectedError.getRuntimeException("Failed copying file", t);
        }
        if (fileFrom.length() != fileTo.length()) {
            boolean newFileExists = fileTo.exists();
            long newLength = fileTo.length();
            fileTo.delete();
            Debug.assertMsg(logger, false, "'from' file '" + fileFrom.getAbsolutePath() + "'.length() is " + fileFrom.length() + ", but fileTo '" + fileTo.getAbsolutePath() + "' length ended up being " + newLength + ". new file was created? " + newFileExists);
        }
        boolean result = true;

        if (deleteOriginal) {
            result = fileFrom.delete();
        }

        return result;
    }

    public static void storeBytesToFile(File file, byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        IPIOUtil.mkdirs(file.getParentFile());
        FileOutputStream out = new FileOutputStream(file);
        IPIOUtil.doStreamInputToOutputWithBuffering(in, out, true);
    }

    public static void doStreamInputToOutputWithBuffering(InputStream input, java.io.OutputStream out, boolean closeOutputWhenDone) throws IOException {
        int bufferSize = 8 * 1024;
        if (!(input instanceof BufferedInputStream) && !(input instanceof ByteArrayInputStream)) {
            input = new BufferedInputStream(input, bufferSize);
        }
        if (!(out instanceof BufferedOutputStream)) {
            out = new BufferedOutputStream(out, bufferSize);
        }
        doStreamInputToOutput(input, out, closeOutputWhenDone, true, bufferSize);
    }

    public static void doStreamInputToOutput(InputStream input, java.io.OutputStream out, boolean closeOutputWhenDone) throws IOException {
        doStreamInputToOutput(input, out, closeOutputWhenDone, true, 8 * 1024);
    }

    public static void doStreamInputToOutput(InputStream input, java.io.OutputStream out, boolean closeOutputWhenDone, boolean closeInputWhenDone) throws IOException {
        doStreamInputToOutput(input, out, closeOutputWhenDone, closeInputWhenDone, 8 * 1024);
    }

    private static void doStreamInputToOutput(InputStream input, java.io.OutputStream out, boolean closeOutputWhenDone, boolean closeInputWhenDone, int bufferSize) throws IOException {
        long startTime = System.currentTimeMillis();
        byte[] buf = new byte[bufferSize];  // 8K buffer
        try {
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        } finally {
            if (closeInputWhenDone) {
                IPIOUtil.doSafeClose(input);
            }
            out.flush();
            if (closeOutputWhenDone) {
                IPIOUtil.doSafeClose(out);
            }
            IPUtil.logTaskTiming("IPIOUtil.doStreamInputToOutputWithBuffering", null, startTime);
        }
    }

    public static void doStreamInputToOutput(Reader input, java.io.Writer out, boolean closeOutputWhenDone) throws IOException {
        if (!(input instanceof BufferedReader)) {
            input = new BufferedReader(input);
        }
        if (!(out instanceof BufferedWriter)) {
            out = new BufferedWriter(out);
        }
        try {
            char[] buf = new char[4 * 1024];  // 4K buffer
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        } finally {
            IPIOUtil.doSafeClose(input);
            out.flush();
            if (closeOutputWhenDone) {
                IPIOUtil.doSafeClose(out);
            }
        }
    }

    /**
     * read the input stream from a stream reader.  if the entry size is unknown,
     * make it -1
     */
    static public char[] getCharsFromReader(Reader is) {
        return getCharsFromReader(is, -1);
    }

    static public char[] getCharsFromReader(Reader is, int iEntrySize) {
        int iTotalRead = 0, iBytesRead = 0;
        if (!(is instanceof BufferedReader)) {
            is = new BufferedReader(is);
        }
        // Establish a buffer for the entry to decompress into    
        int iBuffersize = (iEntrySize < 0) ? 10240 : iEntrySize;
        char[] bBuffer = new char[iBuffersize];

        try {

            // Read the zip stream until its complete...    
            while ((iBytesRead = is.read(bBuffer, iTotalRead, bBuffer.length - iTotalRead)) >= 0) {
                // Increment the read count    
                iTotalRead += iBytesRead;

                // Is this current read complete?    
                if (iTotalRead == bBuffer.length) {

                    // Are we working with a valid entry size?    
                    if (iEntrySize < 0) {
                        // Entry size is unknown, double the buffer size and    
                        // keep reading until zipStream.read is < 0!    
                        bBuffer = ArrayUtil.resizeArray(bBuffer, 2 * iTotalRead);
                    } else {
                        // Valid entry size, done reading    
                        break;
                    }
                }
            }

            // Are we working with a valid entry size?    
            if (iEntrySize < 0) {
                // The entry was read and the exact size is now known -- strip any unused bytes    
                bBuffer = ArrayUtil.resizeArray(bBuffer, iTotalRead);
            }
            return bBuffer;
        } catch (IOException e) {
            logger.error("Ignoring IOException in IPIOUtil.getCharsFromReader ", e);
            return bBuffer;
        } finally {
            doSafeClose(is);
        }
    }

    public static byte[] getBytesFromInputStream(InputStream in, int bytesToRead) {
        return getBytesFromInputStream(in, bytesToRead, false);
    }

    public static byte[] getBytesFromInputStream(InputStream in, int bytesToRead, boolean rethrowException) {
        long startTime = System.currentTimeMillis();
        int inputLen, offset;
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        byte[] postedBytes = null;

        if (bytesToRead <= 0) {
            return new byte[0];
        }

        offset = 0;
        try {
            //
            // Make sure we read the entire POSTed body.
            //
            postedBytes = new byte[bytesToRead];
            do {
                inputLen = in.read(postedBytes, offset, bytesToRead - offset);
                if (inputLen < 0) {
                    throw new IOException("short read");
                }
                offset += inputLen;
            } while ((bytesToRead - offset) > 0);

        } catch (IOException e) {
            if (rethrowException) {
                throw UnexpectedError.getRuntimeException(null, e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("ignoring IO Exception getting " + bytesToRead + " bytes after reading " + offset + " bytes", e);
            }
            postedBytes = ZipClassLoader.resizeBuffer(postedBytes, offset);
        } finally {
            IPUtil.logTaskTiming("IPIOUtil.getBytesFromInputStream", null, startTime);
        }
        return postedBytes;
    }

    static public byte[] getBytesFromFile(File f) {
        try {
            return getBytesFromInputStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in IPIOUtil.getBytesFromFile ", e);
            return null;
        }
    }

    /**
     * read the input stream fully and then close it
     */
    static public byte[] getBytesFromInputStream(InputStream is) {
        return getBytesFromInputStream(is, false);
    }

    static public byte[] getBytesFromInputStream(InputStream is, boolean rethrowExceptions) {
        long startTime = System.currentTimeMillis();

        int iTotalRead = 0, iBytesRead = 0;
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }

        // Establish a buffer for the entry to decompress into    
        int iBuffersize = 65536;
        byte[] bBuffer = new byte[iBuffersize];

        try {

            // Read the zip stream until its complete...    
            do {
                try {
                    iBytesRead = is.read(bBuffer, iTotalRead, bBuffer.length - iTotalRead);
                } catch (IOException ioex) {
                    String error = ioex.getMessage();
                    if (error == null) {
                        throw ioex;
                    }
                    if (error.indexOf("Not enough storage is available") == -1) {
                        throw ioex;
                    }
                    iBuffersize = 200;
                    bBuffer = new byte[iBuffersize];
                    iBytesRead = is.read(bBuffer, iTotalRead, bBuffer.length - iTotalRead);
                }
                if (iBytesRead < 0) {
                    break;
                }
                //if(logger.isInfoEnabled()) logger.info( IPStringUtil.getBytesAsString(bBuffer, "",true));
                // Increment the read count    
                iTotalRead += iBytesRead;

                // Is this current read complete?    
                if (iTotalRead == bBuffer.length) {

                    //double the buffer size and    
                    // keep reading until zipStream.read is < 0!    
                    bBuffer = ArrayUtil.resizeArray(bBuffer, 2 * iTotalRead);
                }
            } while (true);

            // strip any unused bytes    
            bBuffer = ArrayUtil.resizeArray(bBuffer, iTotalRead);
            return bBuffer;
        } catch (IOException e) {
            if (rethrowExceptions) {
                throw UnexpectedError.getRuntimeException(null, e);
            }
            logger.error("IOException in IOIOUtil.getBytesFromInputStream ", e);
            return bBuffer;
        } finally {
            IPIOUtil.doSafeClose(is);
            IPUtil.logTaskTiming("IPIOUtil.getBytesFromInputStream", null, startTime);
        }
    }
    /*
    UCS-4 signature: 0000 FEFF
    UTF-8 signature: EF BB BF
    UCS-2 signature: FEFF
    UTF-16 signature: FEFF
    An application receiving data may either use these signatures to
    identify the coded representation form, or may ignore them and treat
    FEFF as the ZERO WIDTH NO-BREAK SPACE character.
    */

    /**
     * get a string from a input stream.  remove any unicode markers from the start of the stream
     *
     * @param nameOfResource will be used in warning messages
     */
    public static String getStringFromStreamWithUnknownEncoding(InputStream is, String nameOfResource) {
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        byte b[] = null;
        try {
            b = getBytesFromInputStream(is, true);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("failed getting bytes from stream: " + nameOfResource, t);
        } finally {
            doSafeClose(is);
        }
        return IPStringUtil.getStringFromBytesWithUnknownEncoding(b, nameOfResource);
    }

    /**
     * get a string from a reader.  remove any unicode markers from the start of the stream
     *
     * @param nameOfResource will be used in warning messages
     */
    public static String getStringFromUTF8Reader(Reader is, String nameOfResource) {
        char ch[] = getCharsFromReader(is);
        return IPStringUtil.getStringFromUTF8Chars(ch);
    }

    /**
     * gets a reader.  has to fully read the input stream.  delegates to
     * getStringFromBytesWithUnknownEncoding (i.e. looks for utf8 marker)
     */
    public static Reader getReaderFromFileWithUnknownEncoding(File file, String nameOfResource) throws FileNotFoundException, IOException {
        if (nameOfResource == null) {
            nameOfResource = file.getAbsolutePath();
        }
        return getReaderFromInputStreamWithUnknownEncoding(new BufferedInputStream(new FileInputStream(file)), nameOfResource);
    }

    /**
     * get a string from a input stream.  remove any unicode markers from the start of the stream
     *
     * @param nameOfResource will be used in warning messages
     */
    public static Reader getReaderFromInputStreamWithUnknownEncoding(InputStream is, String nameOfResource) throws IOException {
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        PushbackInputStream pb = new PushbackInputStream(is, 20);
        // check for utf-16LE or utf-16be
        {
            byte peek[] = new byte[2];
            int read = pb.read(peek);
            // it's empty
            if (read < 0) {
                return new StringReader("");
            }
            if (IPUtil.is2UnicodeLEBytes(peek)) {
                if (logger.isInfoEnabled()) {
                    logger.info("isreader: " + nameOfResource + " charset is UnicodeLittle");
                }
                return new InputStreamReader(pb, "UnicodeLittle");
            }
            if (IPUtil.is2UnicodeBEBytes(peek)) {
                if (logger.isInfoEnabled()) {
                    logger.info("isreader: " + nameOfResource + " charset is UnicodeBig");
                }
                return new InputStreamReader(pb, "UnicodeBig");
            }
            // no match? should be back where we started from
            pb.unread(peek, 0, read);
        }
        {
            byte peek[] = new byte[3];
            int read = pb.read(peek);
            // it's empty
            if (read < 0) {
                return new StringReader("");
            }
            if (IPUtil.is3UTF8Bytes(peek)) {
                if (logger.isInfoEnabled()) {
                    logger.info("isreader: " + nameOfResource + " charset is utf8");
                }
                return new InputStreamReader(pb, "UTF8");
            }
            pb.unread(peek, 0, read);
        }
        if (logger.isInfoEnabled()) {
            logger.info("isreader: " + nameOfResource + " charset defaulting to iso-latin-8859");
        }
        return new InputStreamReader(pb, IPUtil.getJavaEncoding(IPUtil.IANA_ISO8859_ENCODING_NAME));
    }

    public static Reader getReaderFromFileWithUnknownEncoding(String fileName) throws FileNotFoundException, IOException {
        return getReaderFromFileWithUnknownEncoding(new File(fileName), null);
    }

    /**
     * returns a writer.  pushes a utf8 marker out first
     */
    public static BufferedWriter getUTF8FileWriter(String file, boolean append) throws IOException {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF8"));
        } catch (UnsupportedEncodingException use) {
            Debug.assertMsg(logger, false, "Failed getting utf8 encoding", use);
            return null;
        }
    }

    static byte utf8Marker[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    /**
     * returns a writer.  pushes a utf8 marker out first
     */
    public static BufferedWriter getUTF8FileWriter(File file) throws IOException {
        return getUTF8WriterMarkedAsUTF8(new FileOutputStream(file));
    }

    /**
     * returns a writer.  pushes a utf8 marker out first
     */
    public static BufferedWriter getUTF8WriterMarkedAsUTF8(OutputStream os) throws IOException {
        try {
            os.write(utf8Marker);
            return new BufferedWriter(new OutputStreamWriter(os, "UTF8"));
        } catch (UnsupportedEncodingException use) {
            Debug.assertMsg(logger, false, "Failed getting utf8 encoding", use);
            return null;
        }
    }

    public static Reader getReaderFromBytesWithUnknownEncoding(byte b[], String nameOfResource) {
        return new StringReader(IPStringUtil.getStringFromBytesWithUnknownEncoding(b, nameOfResource));
    }

    public static interface ReaderFactory {
        public Reader getReader();

        public String getString();
    }

    public static class StringReaderFactory implements IPIOUtil.ReaderFactory {
        String s;

        public StringReaderFactory(String _s) {
            s = _s;
        }

        public String getString() {
            return s;
        }

        public Reader getReader() {
            return new StringReader(s);
        }
    }

    /**
     * the whole point of this class is to be able to pass around a reader
     * equivalent, that only performs a read when its reader is accessed.
     * (mainly used in the stylesheet caching realm).
     */
    public static class FileReaderFactory implements IPIOUtil.ReaderFactory {
        File file;
        String s;

        public FileReaderFactory(File f) throws FileNotFoundException {
            file = f;
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }

        public void init() {
            if (s != null) {
                return;
            }
            try {
                s = getStringFromStreamWithUnknownEncoding(new FileInputStream(file), file.getAbsolutePath());
            } catch (FileNotFoundException fnf) {
                Debug.assertMsg(logger, false, "Failed getting file", fnf);
            }
        }

        public String getString() {
            init();
            return s;
        }

        public Reader getReader() {
            init();
            return new StringReader(s);
        }
    }
    /*static QuotaHashtable.TimestampedQuotaHashtable qhGetMD5DigestForFile = (QuotaHashtable.TimestampedQuotaHashtable)
                                                                            QuotaHashtable.QuotaHashtableRegistry.registerQuotaHashtable(
                                                                                                                                        new QuotaHashtable.TimestampedQuotaHashtable("IPIOUtil.GetMD5DigestForFile(file)", 500, IPDateUtil.DAY_IN_MS));*/

    public static String getMD5DigestForFile(File f) {
        /*Object o = qhGetMD5DigestForFile.get("getMD5DigestForFile(file)" + f.getAbsolutePath()
                                             , f.lastModified());
        if(o!=null)
            return(String)o;*/

        // bl: we weren't getting the proper md5 from binary files when using this old method of first
        // converting to a string and then getting the md5 from that string. 
        /*String absPath = f.getAbsolutePath();
        String s = getStringFromStreamWithUnknownEncoding(new FileInputStream(f), absPath);
        String md5digest = IPStringUtil.getMD5DigestFromString(s);*/
        // instead, let's now just get the bytes directly from the file and then get the md5 from those bytes.
        byte[] bytes = getBytesFromFile(f);
        String md5digest = IPUtil.getMD5DigestFromBytes(bytes);
        //qhGetMD5DigestForFile.put("getMD5DigestForFile(file)" + f.getAbsolutePath(), md5digest);
        return md5digest;
    }

    public static class DefaultReaderFactory implements IPIOUtil.ReaderFactory {
        Reader r;
        String s;

        public DefaultReaderFactory(Reader _r) {
            r = _r;
        }

        public void init() {
            if (s != null) {
                return;
            }
            s = new String(getCharsFromReader(r));
        }

        public String getString() {
            init();
            return s;
        }

        public Reader getReader() {
            init();
            return new StringReader(s);
        }
    }

    public static void doSafeClose(InputStream i) {
        if (i == null) {
            return;
        }
        try {
            i.close();
        } catch (IOException ioex) {
            logger.error("Double closing an input stream", ioex);
        }
    }

    public static void doSafeClose(Reader i) {
        if (i == null) {
            return;
        }
        try {
            i.close();
        } catch (IOException ioex) {
            logger.error("Double closing reader", ioex);
        }
    }

    public static void doSafeClose(OutputStream i) {
        if (i == null) {
            return;
        }
        try {
            i.close();
        } catch (IOException ioex) {
            logger.error("Double closing an output stream", ioex);
        }
    }

    public static void doSafeClose(Writer i) {
        if (i == null) {
            return;
        }
        try {
            i.close();
        } catch (IOException ioex) {
            logger.error("Double closing writer", ioex);
        }
    }

    public static void doSafeClose(RandomAccessFile i) {
        if (i == null) {
            return;
        }
        try {
            i.close();
        } catch (IOException ioex) {
            logger.error("Double closing raf", ioex);
        }
    }

    /**
     * recursively delete a directory and its contents
     *
     * @returns true iff the directory and its contents were successfully deleted.  false otherwise.
     */
    public static boolean deleteDirectory(File f) {
        if (!f.exists() || !f.isDirectory()) {
            return false;
        }
        File items[] = f.listFiles();
        for (int i = 0; items != null && i < items.length; i++) {
            if (!items[i].isDirectory()) {
                items[i].delete();
                continue;
            }
            deleteDirectory(items[i]);
        }
        return (f.delete());
    }

    public static boolean copyDirectory(File source, File dest) {
        return copyDirectory(source, dest, false);
    }

    /**
     * recursively delete a directory and its contents
     *
     * @returns true iff the directory and its contents were successfully deleted.  false otherwise.
     */
    public static boolean copyDirectory(File source, File dest, boolean overwrite) {

        if (overwrite) {
            if (!deleteDirectory(dest)) {
                Debug.assertMsg(logger, !dest.exists(), "unable to delete destination directory: " + dest);
            }
        }

        if (!source.exists() || !source.isDirectory()) {
            return false;
        }
        Debug.assertMsg(logger, !dest.exists(), "dest exists already");
        IPIOUtil.mkdirs(dest);
        dest.setLastModified(source.lastModified());
        File items[] = source.listFiles();
        for (int i = 0; items != null && i < items.length; i++) {
            File s = items[i];
            File d = new File(dest, s.getName());
            if (!s.isDirectory()) {
                try {
                    IPIOUtil.doStreamFileToOut(s, new FileOutputStream(d), true);
                } catch (IOException ioex) {
                    Debug.assertMsg(logger, false, "Failed copying " + s.getAbsolutePath() + " to " + d.getAbsolutePath(), ioex);
                }
                d.setLastModified(s.lastModified());
                continue;
            }
            copyDirectory(s, d);
        }
        return true;
    }

    public static class CopyDir {
        public static void main(String args[]) {
            copyDirectory(new File(args[0]), new File(args[1]));
        }
    }

    public static String getSafeFilenameFromString(String filename, boolean allowSpaces) {
        if (IPStringUtil.isEmpty(filename)) {
            return "empty";
        }
        filename = filename.trim();
        filename = filename.replace('/', '_').replace(':', '_').replace('?', '_').replace('*', '_').replace('#', '_').replace('=', '_').replace('&', '_').replace('<', '_').replace('>', '_').replace('@', '_').replace(';', '_').replace('+', '_').replace('"', '_');
        if (!allowSpaces) {
            filename = filename.replace(' ', '_');
        }
        return filename;
    }

    public static byte[] getUnGzippedData(byte gzippedData[]) {
        if (gzippedData == null) {
            gzippedData = new byte[0];
        }
        long startTime = System.currentTimeMillis();
        try {
            ByteArrayOutputStream ret = new ByteArrayOutputStream(Math.max(1024, gzippedData.length * 3));
            BufferedOutputStream baBackToOriginal = new BufferedOutputStream(ret, 1024);
            BufferedInputStream gzi = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(gzippedData), 1024), 1024);
            IPIOUtil.doStreamInputToOutputWithBuffering(gzi, baBackToOriginal, true);
            return ret.toByteArray();
        } catch (IOException ioex) {
            Debug.assertMsg(logger, false, "Failed unzipping data", ioex);
            return null;
        } finally {
            IPUtil.logTaskTiming("IPIOUtil.getUnGZippedBytes", null, startTime);
        }
    }

    public static byte[] getGzippedData(byte uncompressedData[]) {
        if (uncompressedData == null || uncompressedData.length == 0) {
            return new byte[0];
        }
        long startTime = System.currentTimeMillis();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(uncompressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(uncompressedData.length + 100, (int) (uncompressedData.length * .3)));
            BufferedOutputStream gzo = new BufferedOutputStream(new GZIPOutputStream(baos, 1024), 1024);
            IPIOUtil.doStreamInputToOutputWithBuffering(bais, gzo, true);
            return baos.toByteArray();
        } catch (IOException ioex) {
            Debug.assertMsg(logger, false, "Failed unzipping data", ioex);
            return null;
        } finally {
            IPUtil.logTaskTiming("JAVA_IO_GET_GZIPPED_DATA", null, startTime);
        }
    }

    public static byte[] getInflatedData(byte deflatedData[]) {
        if (deflatedData == null) {
            deflatedData = new byte[0];
        }
        long startTime = System.currentTimeMillis();
        try {
            ByteArrayOutputStream ret = new ByteArrayOutputStream(Math.max(1024, deflatedData.length * 3));
            BufferedOutputStream baBackToOriginal = new BufferedOutputStream(ret, 1024);
            BufferedInputStream iis = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(deflatedData)), 1024);
            IPIOUtil.doStreamInputToOutputWithBuffering(iis, baBackToOriginal, true);
            return ret.toByteArray();
        } catch (IOException ioex) {
            Debug.assertMsg(logger, false, "Failed inflating data", ioex);
            return null;
        } finally {
            IPUtil.logTaskTiming("IPIOUtil.getInflatedData", null, startTime);
        }
    }

    public static byte[] getDeflatedData(byte uncompressedData[]) {
        if (uncompressedData == null || uncompressedData.length == 0) {
            return new byte[0];
        }
        long startTime = System.currentTimeMillis();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(uncompressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(uncompressedData.length + 100, (int) (uncompressedData.length * .3)));
            BufferedOutputStream dos = new BufferedOutputStream(new DeflaterOutputStream(baos), 1024);
            IPIOUtil.doStreamInputToOutputWithBuffering(bais, dos, true);
            return baos.toByteArray();
        } catch (IOException ioex) {
            Debug.assertMsg(logger, false, "Failed deflating data", ioex);
            return null;
        } finally {
            IPUtil.logTaskTiming("JAVA_IO_GET_DEFLATED_DATA", null, startTime);
        }
    }

    /**
     * tests that the getReaderFromInputStream method can detect utf-8 and
     * iso 8859 chars
     */
    public static void testGetReaderFromInputStream() throws IOException {
        System.out.println("Testing inputstream conversions");
        String testInput = "Test Input";
        byte utf8[] = new byte[]{
                // the following is "Test Input" in utf-8 the first 3 bytes are the utf-8
                // marker
                0xef - 0x100, 0xbb - 0x100, 0xbf - 0x100, 0x54, 0x65, 0x73, 0x74, 0x20, 0x49, 0x6e, 0x70, 0x75, 0x74
                //testInput.getBytes(IPUtil.getJavaEncoding(IPUtil.IANA_UTF8_ENCODING_NAME));
        };
        byte iso8859[] = testInput.getBytes(IPUtil.getJavaEncoding(IPUtil.IANA_ISO8859_ENCODING_NAME));
        System.out.println("utf8 bytes: " + IPStringUtil.getBytesAsHexStringDump(utf8));
        System.out.println("iso8859 bytes: " + IPStringUtil.getBytesAsHexStringDump(iso8859));
        Reader utf8Reader = IPIOUtil.getReaderFromInputStreamWithUnknownEncoding(new ByteArrayInputStream(utf8), "utf8");
        Reader iso8859Reader = IPIOUtil.getReaderFromInputStreamWithUnknownEncoding(new ByteArrayInputStream(iso8859), "iso8859");
        char utf8Chars[] = new char[testInput.length()];
        char iso8859Chars[] = new char[testInput.length()];
        utf8Reader.read(utf8Chars);
        iso8859Reader.read(iso8859Chars);
        String utf8String = new String(utf8Chars);
        String iso8859String = new String(iso8859Chars);
        if (!testInput.equals(utf8String)) {
            System.err.println("testInput not equal to utf 8 string '" + utf8String + "'");
        }
        if (!testInput.equals(iso8859String)) {
            System.err.println("testInput not equal to iso8859 string '" + iso8859String + "'");
        }
    }

    public static long getLastModifiedFileInDirectory(File directory) {
        if (directory == null) {
            return -1;
        }
        if (!directory.isDirectory()) {
            return -1;
        }
        String files[] = directory.list();
        long maxModified = -1;
        for (int i = 0; files != null && i < files.length; i++) {
            File f = new File(directory, files[i]);
            long time = 0;
            if (f.isDirectory()) {
                // get recursive
                time = getLastModifiedFileInDirectory(f);
            } else {
                time = f.lastModified();
            }
            maxModified = Math.max(maxModified, time);
        }
        return maxModified;
    }

    /**
     * Create the Path to where the a content file will be and return a reference to the last directory.
     * e.g. if the oid is 123456789 then the directory will be:
     * baseDirName/1/2
     *
     * @param contentOID  String - The OID of the file that we are working with
     * @param baseDirName an IPFileRegistry base directory name, not an actual file name
     * @param splitDepth  the number of chars to split
     * @return the directory name (you'll need to append a tailing '/').
     **/
    public static String getSplitDirName(String baseDirName, String contentOID, int splitDepth) {
        return getSplitDirName(baseDirName, contentOID, splitDepth, "/");
    }

    /**
     * Create the Path to where the a content file will be and return a reference to the last directory.
     * e.g. if the oid is 123456789 then the directory will be:
     * baseDirName/1/2
     *
     * @param contentOID  String - The OID of the file that we are working with
     * @param baseDirName an IPFileRegistry base directory name, not an actual file name
     * @param splitDepth  the number of chars to split
     * @param separator   the path separator to use
     * @return the directory name (you'll need to append a tailing '/').
     **/
    public static String getSplitDirName(String baseDirName, String contentOID, int splitDepth, String separator) {
        if (splitDepth == 0) {
            return baseDirName;
        }
        if (baseDirName == null) {
            baseDirName = "";
        }
        if (contentOID == null) {
            contentOID = "";
        }
        if (separator == null) {
            separator = "";
        }
        StringBuffer ret = new StringBuffer(baseDirName.length() + contentOID.length() + splitDepth * (2 + separator.length()) + 10);
        ret.append(baseDirName);
        int len = contentOID.length();
        for (int i = 0; i < splitDepth; i++) {
            ret.append(separator);
            String s = len > i ? contentOID.substring(i, i + 1) : "_";
            ret.append(s);
        }
        return ret.toString();
    }

    public static boolean mkdirs(File f) {
        if (f == null) {
            if (logger.isInfoEnabled()) {
                logger.info("mkdirs file is null");
            }
            return false;
        }
        boolean exists = f.exists();
        boolean isDir = f.isDirectory();
        if (exists && !isDir) {
            Debug.assertMsg(logger, false, "dir problem: it exists already but is not a directory: " + f.getAbsolutePath());
        }
        // already here
        if (exists) {
            return true;
        }
        // try creating it the 'normal' way 
        f.mkdirs();
        if (f.isDirectory()) {
            // done
            return true;
        }
        // else create the parent then create this dir
        boolean parentCreated = mkdirs(f.getParentFile());
        if (!parentCreated) {
            Debug.assertMsg(logger, false, "failed creating dir on parent file of : " + f.getAbsolutePath());
        }
        f.mkdir();
        if (!f.isDirectory()) {
            Debug.assertMsg(logger, false, "failed mkdirs, even when run recursively: " + f.getAbsolutePath());
            return false;
        }
        return true;
    }

    public static void streamPropertiesToFile(File propFile, Properties props) {
        try {
            FileOutputStream os = new FileOutputStream(propFile);
            props.store(os, " Properties Written at " + new Timestamp(System.currentTimeMillis()).toString());
        } catch (IOException ioe) {
            Debug.assertMsg(logger, false, "Failed writing out properties to file.");
        }
    }

    /**
     * delete a file, then delete its parent directories if empty.
     * e.g. delete("/1/2/3/4.gif", 2) would leave just /1.  i.e. 4.gif deleted,
     * /1/2/3 deleted, /1/2 deleted.  Presuming here the directories were empty.
     */
    public static void deleteFileAndClearOutEmptyParentDirectories(File fileToDelete, int parentLevelsToDelete) {
        if (!fileToDelete.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("File doesn't exist, nothing to delete");
            }
            return;
        }

        if (!fileToDelete.delete()) {
            logger.error("ignoring error deleting attachment file: " + fileToDelete.getAbsolutePath(), new Throwable());
        }
        File parent = fileToDelete.getParentFile();
        for (int i = 0; i < parentLevelsToDelete; i++) {
            if (parent == null) {
                break;
            }
            if (!parent.isDirectory()) {
                break;
            }
            String contents[] = parent.list();
            if (contents != null && contents.length > 0) {
                break;
            }
            if (!parent.delete()) {
                logger.error("ignoring error deleting parent dir when deleting attachment: " + parent.getAbsolutePath(), new Throwable());
                break;
            }
            parent = parent.getParentFile();
        }
    }

    public static void moveDirectory(File fromDir, File toDir) {
        if (fromDir.renameTo(toDir)) {
            // nice and simple
            return;
        }
        // get all the parent dirs in place
        IPIOUtil.mkdirs(toDir);
        // make sure the destination dir doesn't exist.  it shouldn't per the
        // rename command above, but just in case.
        if (toDir.exists()) {
            IPIOUtil.deleteDirectory(toDir);
        }
        // copy the directory and make sure the copy was a success
        boolean wasCopied = IPIOUtil.copyDirectory(fromDir, toDir);
        Debug.assertMsg(logger, wasCopied, "failed copying from " + fromDir.getAbsolutePath() + " to " + toDir.getAbsolutePath());
        // delete the temp index directory
        IPIOUtil.deleteDirectory(fromDir);
    }

    public static long getDirectorySize(File source) {
        if (source == null) {
            return 0;
        }
        if (!source.exists()) {
            return 0;
        }
        if (!source.isDirectory()) {
            return source.length();
        }
        File items[] = source.listFiles();
        long ret = 0;
        for (int i = 0; items != null && i < items.length; i++) {
            File s = items[i];
            ret += getDirectorySize(s);
        }
        return ret;
    }

    /**
     * Iterates over all files and directories in the provided file and removes any that have not been accessed
     * since the provided days-ago date. This will also remove empty directories as it is going
     * through cleaning everything up.
     *
     * @param dir                                 The directory that we are pruning files out of
     * @param removeFileLastAccessedBeforeDaysAgo The cutoff Date.  Any files not accessed since this date will
     *                                            be removed.
     */
    public static void pruneFilesByLastAccessedDateInDirectory(File dir, int removeFileLastAccessedBeforeDaysAgo, boolean useNice) {
        long startTime = System.currentTimeMillis();
        List<String> commands = new ArrayList<String>(2);
        commands.add("find " + dir.getAbsolutePath() + " -type f -atime +" + removeFileLastAccessedBeforeDaysAgo + " -exec rm {} ;");
        commands.add("find " + dir.getAbsolutePath() + " -depth -type d -empty -exec rmdir {} ;");
        // bl: if we are using nice (on non-dev machines), then we should prepend the command with nice appropriately.
        if (useNice) {
            for (int i = 0; i < commands.size(); i++) {
                commands.set(i, "nice -n 19 ionice -c3 " + commands.get(i));
            }
        }
        ObjectTriplet<Boolean, String, String> fileOutput = RuntimeUtils.exec(commands.get(0));
        ObjectTriplet<Boolean, String, String> dirOutput = RuntimeUtils.exec(commands.get(1));
        // bl: if it succeeded, then log out a message accordingly
        if (fileOutput.getOne() && dirOutput.getOne()) {
            if (logger.isInfoEnabled()) {
                logger.info("Successfully prune files last accessed over " + removeFileLastAccessedBeforeDaysAgo + " days ago in " + dir.getAbsolutePath() + " in " + (System.currentTimeMillis() - startTime) + "ms");
            }
            return;
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Failed to prune files last accessed over " + removeFileLastAccessedBeforeDaysAgo + " days ago in " + dir.getAbsolutePath() + " in " + (System.currentTimeMillis() - startTime) + "ms" + "\n\nFileOutputStdOut: " + fileOutput.getTwo() + "\n\nFileOutputStdErr: " + fileOutput.getThree() + "\n\nDirOutputStdOut: " + dirOutput.getTwo() + "\n\nDirOutputStdErr: " + dirOutput.getThree());
        }
    }
}
