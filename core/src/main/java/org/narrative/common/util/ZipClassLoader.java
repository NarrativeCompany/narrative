package org.narrative.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * class to load java from directories, zip files, input streams
 *
 * @author Peter Bryant(pbryant@bigfoot.com)
 */
public final class ZipClassLoader extends ClassLoader {

    private static final NarrativeLogger logger = new NarrativeLogger(ZipClassLoader.class);

    Hashtable dataBytes = new Hashtable();
    Hashtable classes = new Hashtable();
    Hashtable alreadyLoaded = new Hashtable();
    static ZipClassLoader instance;

    /**
     * get a singleton instance
     */
    public static ZipClassLoader getInstance() {
        if (instance == null) {
            instance = new ZipClassLoader();
        }
        return instance;
    }

    /**
     * is the location loaded (location is http://... or a file's absolute path)
     */
    public boolean isLoaded(String location) {
        if (location.indexOf("http://") == 0) {
            return alreadyLoaded.contains(location);
        }
        return isLoaded(new File(location));
    }

    /**
     * has the tempFile already been loaded?  based on file's absolute path.
     */
    public boolean isLoaded(File f) {
        return alreadyLoaded.contains(f.getAbsolutePath());
    }

    /**
     * from string figure out whether we're loading from an URL,
     * a file or directory
     */
    public void load(String location) throws Exception {
        if (location.indexOf("http://") == 0) {
            if (isLoaded(location)) {
                return;
            }
            URL content = new URL(location);
            content.openConnection().setAllowUserInteraction(true);
            URLConnection c = content.openConnection();
            c.connect();
            ZipInputStream is = null;
            try {
                is = new ZipInputStream(c.getInputStream());
                loadZipInputStream(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            alreadyLoaded.put(location, location);
            return;
        }
        File f = new File(location);
        //if(alreadyLoaded.contains(f.getAbsolutePath()))
        //  throw UnexpectedError.getInstance("'" + f.getAbsolutePath() + "' is already loaded");
        load(f);
    }

    /**
     * Load classes from a zip input stream
     */
    public void loadZipInputStream(ZipInputStream zr) throws IOException {
        // We might be reading the zip file from some arbitrary "distance"
        // away, so be prepared to stop graceuflly upon the user's request.
        Thread thread = Thread.currentThread();

        ZipEntry ze;
        while (!thread.isInterrupted() && null != (ze = zr.getNextEntry())) {
            String path = ze.getName();
            byte data[] = readZipEntry(zr, ze);
            if (thread.isInterrupted()) {
                break;
            }
            if (path.endsWith(".class")) {
                path = path.substring(0, path.length() - ".class".length());
            }
            path = path.replace('/', '.');
            classes.remove(path);
            dataBytes.put(path, data);
        }

        if (thread.isInterrupted()) {
            // Repair any corrupted state
            dataBytes = new Hashtable();
            throw new IOException("zip loading interrupted");
        }
    }

    /**
     * load resources from a file (or directory).  If a directory is specified, it
     * recursively sucks in everything under it
     */
    public void load(File f) throws IOException {
        if (isLoaded(f)) {
            return;
        }
        if (!f.exists()) {
            throw UnexpectedError.getRuntimeException("'" + f.getAbsolutePath() + "' does not exist");
        }
        if (f.isDirectory()) {
            loadDirectory0(f, null);
            alreadyLoaded.put(f.getAbsolutePath(), f.getAbsolutePath());
            return;
        }
        String fname = f.getName();
        if (fname.indexOf(".zip") > 0 || fname.indexOf(".jar") > 0) {
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new FileInputStream(f));
                loadZipInputStream(zis);
            } finally {
                if (zis != null) {
                    zis.close();
                }
            }
            alreadyLoaded.put(f.getAbsolutePath(), f.getAbsolutePath());
            return;
        }
        loadFile0(f, null);
        alreadyLoaded.put(f.getAbsolutePath(), f.getAbsolutePath());
    }

    private void loadFile0(File f, String packageName) throws IOException {
        FileInputStream fis = null;
        byte data[] = null;
        try {
            fis = new FileInputStream(f);
            data = readInputStream(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        String path = f.getName();
        if (path.endsWith(".class")) {
            path = path.substring(0, path.length() - ".class".length());
        }
        path = (packageName == null ? "" : packageName + '.') + path;
        dataBytes.put(path, data);
        classes.remove(path);
    }

    private void loadDirectory0(File dir, String packageName) throws IOException {
        String children[] = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                // ensure the package name is correct relative to directory we're loading
                loadDirectory0(f, packageName == null ? f.getName() : packageName + '.' + f.getName());
                continue;
            }
            loadFile0(f, packageName);
        }
    }

    /**
     * Get an InputStream on a given resource.  Will return null if no
     * resource with this name is found.
     * <p>
     * name is in the form "dir/dir/name.ext";
     */
    public InputStream getResourceAsStream(String name) {
        byte bytes[] = (byte[]) dataBytes.get(name);
        if (bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Load a class from this class loader.
     *
     * @author Peter Bryant(pbryant@bigfoot.com)
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    /**
     * Load and resolve a class.
     */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class cl = (Class) classes.get(name);
        if (cl == null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                int i = name.lastIndexOf('.');
                if (i >= 0) {
                    security.checkPackageAccess(name.substring(0, i));
                }
            }
            try {
                return findSystemClass(name);
            } catch (ClassNotFoundException e) {
            }
            cl = zuFindClass(name);
        }
        if (cl == null) {
            throw new ClassNotFoundException(name);
        }
        if (resolve) {
            resolveClass(cl);
        }
        return cl;
    }

    /**
     * This method finds a class, and must be synchronized to avoid two
     * threads loading the same class at the same time. The returned class
     * may be unresolved.
     * changed name to resolve conflict with jdk2
     */
    private synchronized Class zuFindClass(String name) {
        Class cl = (Class) classes.get(name);
        if (cl == null) {
            SecurityManager security = System.getSecurityManager();
            if (logger.isInfoEnabled()) {
                logger.info(Thread.currentThread().getName() + " find class " + name);
            }

            if (security != null) {
                int i = name.lastIndexOf('.');
                if (i >= 0) {
                    security.checkPackageDefinition(name.substring(0, i));
                }
            }
            byte data[] = (byte[]) dataBytes.get(name);
            if (data != null) {
                try {
                    cl = defineClass(name, data, 0, data.length);
                    if (!name.equals(cl.getName())) {
                        throw new ClassFormatError();
                    }
                } catch (ClassFormatError e) {
                    logger.error("Class format error: " + name);
                    return null;
                }
                classes.put(name, cl);
                dataBytes.remove(name);
            }
        }
        return cl;
    }

    /**
     * return as a byte array the given input stream
     */
    static public byte[] readInputStream(InputStream is) throws IOException {
        return readInputStream(is, -1);
    }

    /**
     * return as a byte array the given zip entry
     */
    static public byte[] readZipEntry(ZipInputStream zipStream, ZipEntry zipEntry) {
        return readInputStream(zipStream, (int) zipEntry.getSize());
    }

    static byte[] readInputStream(InputStream is, int iEntrySize) {
        return readInputStream(is, iEntrySize, false);
    }

    static byte[] readInputStream(InputStream is, int iEntrySize, boolean rethrowExceptions) {
        int iTotalRead = 0, iBytesRead = 0;

        // Establish a buffer for the entry to decompress into    
        int iBuffersize = (iEntrySize < 0) ? 10240 : iEntrySize;
        byte[] bBuffer = new byte[iBuffersize];

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
                        bBuffer = resizeBuffer(bBuffer, 2 * iTotalRead);
                    } else {
                        // Valid entry size, done reading    
                        break;
                    }
                }
            }

        } catch (IOException e) {
            if (rethrowExceptions) {
                throw UnexpectedError.getRuntimeException(null, e);
            }
            logger.error("IOException in JZipLoader.readZipEntry ", e);
            return null;
        }
        // Are we working with a valid entry size?    
        if (iEntrySize < 0) {
            // The entry was read and the exact size is now known -- strip any unused bytes    
            bBuffer = resizeBuffer(bBuffer, iTotalRead);
        }
        return bBuffer;
    }

    /**
     * read the input stream from a stream reader.  if the entry size is unknown,
     * make it -1
     */
    static public char[] readInputStreamReader(InputStreamReader is, int iEntrySize) {
        int iTotalRead = 0, iBytesRead = 0;

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
            logger.error("IOException in JZipLoader.readZipEntry ", e);
            return bBuffer;
        }
    }

    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public byte[] resizeBuffer(byte[] bBuffer, int iNewBufferSize) {
        if (bBuffer.length == iNewBufferSize) {
            return bBuffer;
        }
        byte[] bTempBuffer = new byte[iNewBufferSize];
        System.arraycopy(bBuffer, 0, bTempBuffer, 0, Math.min(bBuffer.length, iNewBufferSize));
        return bTempBuffer;
    }
}
