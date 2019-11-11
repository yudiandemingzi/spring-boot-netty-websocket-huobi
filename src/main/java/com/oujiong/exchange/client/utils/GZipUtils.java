package com.oujiong.exchange.client.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @Description: GZip 压缩辅助类（火币网数据需要解压）
 *
 * @author xub
 * @date 2019/7/30 下午7:07
 */
public class GZipUtils {

    public static final int BUFFER = 1024;

    /**
     * 数据解压缩
     *
     * @param data
     */
    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩
        decompress(bais, baos);
        data = baos.toByteArray();
        baos.flush();
        baos.close();
        bais.close();
        return data;
    }


    /**
     * 数据解压缩
     *
     * @param is
     * @param os
     */
    public static void decompress(InputStream is, OutputStream os) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = gis.read(data, 0, BUFFER)) != -1) {
            os.write(data, 0, count);
        }
        gis.close();
    }

}
