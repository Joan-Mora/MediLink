package gui.util;

import javax.imageio.ImageIO;

import java.awt.Image;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.ByteOrder;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;

import java.util.List;

public class IcoUtil {

    public static Image readIco(Path path) {

        try {

            byte[] all = Files.readAllBytes(path);

            if (all.length < 6) return null;

            ByteBuffer buf = ByteBuffer.wrap(all).order(ByteOrder.LITTLE_ENDIAN);

            int reserved = Short.toUnsignedInt(buf.getShort());

            int type = Short.toUnsignedInt(buf.getShort());

            int count = Short.toUnsignedInt(buf.getShort());

            if (reserved != 0 || type != 1 || count <= 0) return null;

            int bestOffset = -1;

            int bestSize = -1;

            int bestW = -1;

            for (int i = 0; i < count; i++) {

                int width = Byte.toUnsignedInt(buf.get());

                int height = Byte.toUnsignedInt(buf.get());

                buf.get(); // colors

                buf.get(); // reserved

                buf.getShort(); // planes

                buf.getShort(); // bitCount

                int size = buf.getInt();

                int offset = buf.getInt();

                int w = (width == 0 ? 256 : width);

                int h = (height == 0 ? 256 : height);

                if (Math.max(w, h) > bestW) { bestW = Math.max(w, h); bestSize = size; bestOffset = offset; }

            }

            if (bestOffset < 0 || bestSize <= 0 || bestOffset + bestSize > all.length) return null;

            byte[] data = new byte[bestSize];

            System.arraycopy(all, bestOffset, data, 0, bestSize);

            if (bestSize >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {

                return ImageIO.read(new ByteArrayInputStream(data));

            }

            return decodeDib(data);

        } catch (IOException e) {

            return null;

        } catch (Throwable t) {

            return null;

        }

    }

    public static List<Image> readIcoAll(Path path) {

        List<Image> images = new ArrayList<>();

        try {

            byte[] all = Files.readAllBytes(path);

            if (all.length < 6) return images;

            ByteBuffer buf = ByteBuffer.wrap(all).order(ByteOrder.LITTLE_ENDIAN);

            int reserved = Short.toUnsignedInt(buf.getShort());

            int type = Short.toUnsignedInt(buf.getShort());

            int count = Short.toUnsignedInt(buf.getShort());

            if (reserved != 0 || type != 1 || count <= 0) return images;

            int[] offsets = new int[count];

            int[] sizes = new int[count];

            for (int i = 0; i < count; i++) {

                buf.get(); // width

                buf.get(); // height

                buf.get(); // colors

                buf.get(); // reserved

                buf.getShort(); // planes

                buf.getShort(); // bitCount

                int size = buf.getInt();

                int offset = buf.getInt();

                sizes[i] = size; offsets[i] = offset;

            }

            for (int i = 0; i < count; i++) {

                int off = offsets[i], len = sizes[i];

                if (off < 0 || len <= 0 || off + len > all.length) continue;

                byte[] data = new byte[len];

                System.arraycopy(all, off, data, 0, len);

                Image img = null;

                if (len >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {

                    try { img = ImageIO.read(new ByteArrayInputStream(data)); } catch (Exception ignore) {}

                } else {

                    try { img = decodeDib(data); } catch (Exception ignore) {}

                }

                if (img != null) images.add(img);

            }

        } catch (Exception ignore) {}

        return images;

    }

    private static BufferedImage decodeDib(byte[] data) {

        ByteBuffer dib = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        if (dib.remaining() < 40) return null;

        int biSize = dib.getInt();

        if (biSize < 40 || biSize > dib.remaining() + 4) return null;

        int biWidth = dib.getInt();

        int biHeightAll = dib.getInt();

    /*int biPlanes =*/ Short.toUnsignedInt(dib.getShort());

        int biBitCount = Short.toUnsignedInt(dib.getShort());

        int biCompression = dib.getInt();

    /*int biSizeImage =*/ dib.getInt();

        dib.getInt();

        dib.getInt();

    /*int biClrUsed =*/ dib.getInt();

        dib.getInt();

        int width = biWidth;

        int height = Math.abs(biHeightAll) / 2;

        boolean topDown = (biHeightAll < 0);

        if (biCompression != 0 && biCompression != 3) return null;

        if (!(biBitCount == 32 || biBitCount == 24)) return null;

        int rowSizeBytes = ((width * biBitCount + 31) / 32) * 4;

        int pixelDataOffset = biSize;

        int pixelArraySize = rowSizeBytes * height;

        if (pixelDataOffset + pixelArraySize > data.length) return null;

        int maskRowSizeBytes = ((width + 31) / 32) * 4;

        int maskOffset = pixelDataOffset + pixelArraySize;

        boolean hasMask = (maskOffset + maskRowSizeBytes * height) <= data.length;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {

            int srcY = topDown ? y : (height - 1 - y);

            int rowStart = pixelDataOffset + srcY * rowSizeBytes;

            int mrowStart = hasMask ? (maskOffset + srcY * maskRowSizeBytes) : -1;

            for (int x = 0; x < width; x++) {

                int pxOffset = rowStart + (x * biBitCount) / 8;

                int b = data[pxOffset] & 0xFF;

                int g = data[pxOffset + 1] & 0xFF;

                int r = data[pxOffset + 2] & 0xFF;

                int a = 0xFF;

                if (biBitCount == 32) {

                    a = data[pxOffset + 3] & 0xFF;

                } else if (hasMask) {

                    int bit = (data[mrowStart + (x >> 3)] >> (7 - (x & 7))) & 0x1;

                    if (bit == 1) a = 0;

                }

                int argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

                img.setRGB(x, y, argb);

            }

        }

        return img;

    }

}

