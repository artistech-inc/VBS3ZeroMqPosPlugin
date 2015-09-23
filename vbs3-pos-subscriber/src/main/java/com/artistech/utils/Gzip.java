/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.utils;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author matta
 */
public final class Gzip {
    
    private Gzip() {
    }

    public final static void GunzipFile(final java.io.InputStream compressedFile, final java.io.OutputStream decompressedFile) throws IOException {

        final byte[] buffer = new byte[1024];

        final GZIPInputStream gZIPInputStream = new GZIPInputStream(compressedFile);
        int bytes_read;
        while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
            decompressedFile.write(buffer, 0, bytes_read);
        }

        decompressedFile.flush();
    }
}
