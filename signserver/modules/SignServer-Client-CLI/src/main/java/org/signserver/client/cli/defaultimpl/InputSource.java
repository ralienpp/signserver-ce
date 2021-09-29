/*************************************************************************
 *                                                                       *
 *  SignServer: The OpenSource Automated Signing Server                  *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.signserver.client.cli.defaultimpl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Input source encapsulting information about an input stream with an associated
 * size and optionally a file name.
 * 
 * @author Marcus Lundblad
 * @version $Id$
 */
public class InputSource {
    private static final Logger LOG = Logger.getLogger(InputSource.class);
    
    private final InputStream inputStream;
    private final long size;
    private final String fileName;
    private final Map<String, String> metadata;
    private byte[] hash;

    /**
     * Construct an instance of InputSource given "raw" input data as
     * a byte array.
     *
     * @param input data
     * @param fileName suggested file name
     * @param metadata request metadata
     */
    public InputSource(final byte[] input, final String fileName, final Map<String, String> metadata) {
        this.inputStream = new ByteArrayInputStream(input);
        this.size = input.length;
        this.fileName = fileName;
        this.metadata = metadata;

        // TODO: should not always hash the input, only when signed requests
        // required, but this is a PoC...
        try {
            hash = calculateHash(inputStream, metadata);
            inputStream.reset();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 IOException ex) {
            LOG.error("Unable to calculate hash", ex);
        }
    }
    
    /**
     * Construct an instance of InputSource given a file.
     * Note: caller must remember to call .close() when done with the
     * input source.
     * 
     * @param file source file
     * @param size size of the input in bytes
     * @param fileName suggested file name
     * @param metadata request metadata
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public InputSource(final File file, final long size,
                       final String fileName,
                       final Map<String, String> metadata) throws FileNotFoundException, IOException {        
        this.size = size;
        this.fileName = fileName;
        this.metadata = metadata;

        // TODO: should not always hash the input, only when signed requests
        // required, but this is a PoC...
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            hash = calculateHash(bis, metadata);         // XXX TODO: Handle large files !!!!!
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            LOG.error("Unable to calculate hash", ex);
        }

        this.inputStream = new FileInputStream(file);
    }

    public InputSource(final byte[] input, final Map<String, String> metadata) {
        this(input, null, metadata);
    }

    public InputSource(final byte[] input, final long size,
                       final String fileName) {
        this(input, fileName, null);
    }
    
    public InputSource(final byte[] input) {
        this(input, null, null);
    }
    
    public InputSource(final File file, final long size) throws IOException {
        this(file, size, null, null);
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getFileName() {
        return fileName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public byte[] getHash() {
        return hash;
    }

    void close() throws IOException {
        inputStream.close();
    }
    
    private byte[] calculateHash(InputStream input,
                                 final Map<String, String> metadata) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        final MessageDigest md =
                MessageDigest.getInstance("SHA-256",
                                          BouncyCastleProvider.PROVIDER_NAME);
        return digest(input, md);
    }
    
    public static byte[] digest(InputStream input, MessageDigest md) throws IOException {
        final byte[] buffer = new byte[4096]; 
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            md.update(buffer, 0, n);
        }
        return md.digest();
    }
}