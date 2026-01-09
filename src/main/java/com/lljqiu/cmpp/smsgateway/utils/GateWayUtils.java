/**
 * Project Name smsgateway
 * File Name package-info.java
 * Package Name com.lljqiu.cmpp.smsgateway.utils
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 * Copyright © 2015, 2017, www.lljqiu.com. All rights reserved.
 */
package com.lljqiu.cmpp.smsgateway.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信接口辅助工具类
 */
public class GateWayUtils {
    private static Logger logger     = LoggerFactory.getLogger(GateWayUtils.class);
    private static int    sequenceId = 0;                                          //序列编号

    /**
     * 序列 自增
     */
    public static int getSequence() {
        ++sequenceId;
        if (sequenceId > 255) {
            sequenceId = 0;
        }
        return sequenceId;
    }

    /**
     * 时间戳的明文,由客户端产生,格式为MMDDHHMMSS，即月日时分秒，10位数字的整型，右对齐 。
     */
    public static String getTimestamp() {
        DateFormat format = new SimpleDateFormat("MMddhhmmss");
        return format.format(new Date());
    }

    /**
     * 用于鉴别源地址。其值通过单向MD5 hash计算得出，表示如下：
     * AuthenticatorSource =
     * MD5（Source_Addr+9 字节的0 +shared secret+timestamp）
     * Shared secret 由中国移动与源地址实体事先商定，timestamp格式为：MMDDHHMMSS，即月日时分秒，10位。
     * @return
     */
    public static byte[] getAuthenticatorSource(String spId, String secret) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = (spId + "\0\0\0\0\0\0\0\0\0" + secret + GateWayUtils.getTimestamp()).getBytes();
            return md5.digest(data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SP链接到ISMG拼接AuthenticatorSource失败：" + e.getMessage());
            return null;
        }
    }

    /** 
     * Description：校验源地址
     * @param spId
     * @param secret
     * @param timestamp
     * @return
     * @return boolean
     * @author name：liujie <br>email: liujie@lljqiu.com
     * @param bs 
     **/
    public static boolean checkAuthenticatorSource(String spId, String secret, String timestamp, byte[] bs) {
        boolean result = false;
        try {
            logger.debug("spId={},secret={},timestamp={}", spId, secret , timestamp);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = (spId + "\0\0\0\0\0\0\0\0\0" + secret + timestamp).getBytes();
            byte[] digest = md5.digest(data);
            logger.debug("digest={},bs={}", digest, bs);
            result = Arrays.equals(digest, bs);
            logger.debug("result={}", result);

        } catch (Exception e) {
            logger.error("校验AuthenticatorSource失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 向流中写入指定字节长度的字符串，不足时补0
     * @param dous:要写入的流对象
     * @param s:要写入的字符串
     * @param len:写入长度,不足补0
     */
    public static void writeString(DataOutputStream dous, String s, int len) {

        try {
            byte[] data = s.getBytes("gb2312");
            if (data.length > len) {
                logger.error("向流中写入的字符串超长！要写长度" + len + " 字符串是:" + s);
            }
            int srcLen = data.length;
            dous.write(data);
            while (srcLen < len) {
                dous.write('\0');
                srcLen++;
            }
        } catch (IOException e) {
            logger.error("向流中写入指定字节长度的字符串失败：" + e.getMessage());
        }
    }

    /**
     * 从流中读取指定长度的字节，转成字符串返回
     * @param ins:要读取的流对象
     * @param len:要读取的字符串长度
     * @return:读取到的字符串
     */
    public static String readString(java.io.DataInputStream ins, int len) {
        byte[] b = new byte[len];
        try {
            ins.read(b);
            String s = new String(b);
            s = s.trim();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 截取字节
     * @param msg
     * @param start
     * @param end
     * @return
     */
    public static byte[] getMsgBytes(byte[] msg, int start, int end) {
        byte[] msgByte = new byte[end - start];
        int j = 0;
        for (int i = start; i < end; i++) {
            msgByte[j] = msg[i];
            j++;
        }
        return msgByte;
    }

    /**  
     * UCS2解码  
     *   
     * @param src  
     *            UCS2 源串  
     * @return 解码后的UTF-16BE字符串  
     */
    public static String DecodeUCS2(String src) {
        byte[] bytes = new byte[src.length() / 2];
        for (int i = 0; i < src.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
        }
        String reValue = "";
        try {
            reValue = new String(bytes, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            reValue = "";
        }
        return reValue;

    }

    /**  
     * UCS2编码  
     *   
     * @param src  
     *            UTF-16BE编码的源串  
     * @return 编码后的UCS2串  
     */
    public static String EncodeUCS2(String src) {
        byte[] bytes;
        try {
            bytes = src.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            bytes = new byte[0];
        }
        StringBuffer reValue = new StringBuffer();
        StringBuffer tem = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            tem.delete(0, tem.length());
            tem.append(Integer.toHexString(bytes[i] & 0xFF));
            if (tem.length() == 1) {
                tem.insert(0, '0');
            }
            reValue.append(tem);
        }
        return reValue.toString().toUpperCase();
    }

    /** 
     * Description：string to list
     * @param delimiter
     * @return
     * @return List
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    @SuppressWarnings("rawtypes")
    public static List stringToList(String delimiter) {
        if (delimiter == null) {
            return null;
        }
        String[] array = delimiter.split(",");
        List<String> list = new ArrayList<String>();
        for (String str : array) {
            list.add(str);
        }
        return list;
    }

    public static long Bytes8ToLong(byte abyte0[]) {
        long ret = 0;

        ret = (0xffL & abyte0[0]) << 56 | (0xffL & abyte0[1]) << 48 | (0xffL & abyte0[2]) << 40
                | (0xffL & abyte0[3]) << 32 | (0xffL & abyte0[4]) << 24 | (0xffL & abyte0[5]) << 16
                | (0xffL & abyte0[6]) << 8 | 0xffL & abyte0[7];

        return ret;
    }

    public static int bytes4ToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                |  (bytes[3] & 0xFF);
    }

    public static int bytes4ToInt(byte[] b, int offset) {
        return ((b[offset] & 0xFF) << 24)
                | ((b[offset + 1] & 0xFF) << 16)
                | ((b[offset + 2] & 0xFF) << 8)
                |  (b[offset + 3] & 0xFF);
    }

    public static long bytes8ToLong(byte[] b, int offset) {
        return ((b[offset] & 0xFFL) << 56)
                | ((b[offset + 1] & 0xFFL) << 48)
                | ((b[offset + 2] & 0xFFL) << 40)
                | ((b[offset + 3] & 0xFFL) << 32)
                | ((b[offset + 4] & 0xFFL) << 24)
                | ((b[offset + 5] & 0xFFL) << 16)
                | ((b[offset + 6] & 0xFFL) << 8)
                |  (b[offset + 7] & 0xFFL);
    }


    public static int byteToInt(byte byte0) {
        return byte0;
    }

    public static int readSequenceId(byte[] message) {
        if (message == null || message.length < 12) {
            return -1; // 消息太短
        }

        // CMPP 采用大端（网络字节序）
        int seqId = ((message[8] & 0xFF) << 24)
                | ((message[9] & 0xFF) << 16)
                | ((message[10] & 0xFF) << 8)
                | (message[11] & 0xFF);

        return seqId;
    }

    public static String toHex(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(data.length * 3);
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
