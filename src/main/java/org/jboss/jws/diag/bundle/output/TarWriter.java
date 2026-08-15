package org.jboss.jws.diag.bundle.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class TarWriter implements AutoCloseable {

    private static final int BLOCK_SIZE = 512;
    private static final int NAME_LENGTH = 100;

    private final OutputStream out;

    TarWriter(OutputStream out) {
        this.out = out;
    }

    void writeFileEntry(String entryName, byte[] content) throws IOException {
        if (entryName.getBytes(StandardCharsets.US_ASCII).length >= NAME_LENGTH) {
            throw new IOException("Entry name too long for USTAR format (max 99 bytes): " + entryName);
        }

        byte[] header = buildHeader(entryName, content.length);
        out.write(header);
        out.write(content);

        int padding = (BLOCK_SIZE - (content.length % BLOCK_SIZE)) % BLOCK_SIZE;
        if (padding > 0) {
            out.write(new byte[padding]);
        }
    }

    void writeEndOfArchive() throws IOException {
        out.write(new byte[BLOCK_SIZE]);
        out.write(new byte[BLOCK_SIZE]);
    }

    private byte[] buildHeader(String entryName, long size) {
        byte[] header = new byte[BLOCK_SIZE];

        writeString(header, 0, NAME_LENGTH, entryName);
        writeOctal(header, 100, 8, 0644);
        writeOctal(header, 108, 8, 0);
        writeOctal(header, 116, 8, 0);
        writeOctal(header, 124, 12, size);
        writeOctal(header, 136, 12, System.currentTimeMillis() / 1000L);

        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }

        header[156] = '0';

        writeString(header, 257, 6, "ustar");
        header[263] = '0';
        header[264] = '0';

        long checksum = 0;
        for (byte b : header) {
            checksum += (b & 0xFF);
        }

        String checksumOctal = String.format("%06o", checksum);
        byte[] checksumBytes = (checksumOctal + "\0 ").getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(checksumBytes, 0, header, 148, 8);

        return header;
    }

    private void writeString(byte[] header, int offset, int length, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        int copyLength = Math.min(bytes.length, length);
        System.arraycopy(bytes, 0, header, offset, copyLength);
    }

    private void writeOctal(byte[] header, int offset, int length, long value) {
        String octal = Long.toOctalString(value);
        int maxDigits = length - 1;
        if (octal.length() > maxDigits) {
            octal = octal.substring(octal.length() - maxDigits);
        }
        StringBuilder padded = new StringBuilder();
        for (int i = 0; i < maxDigits - octal.length(); i++) {
            padded.append('0');
        }
        padded.append(octal);

        byte[] bytes = padded.toString().getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, header, offset, bytes.length);
        header[offset + length - 1] = 0;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    static byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}