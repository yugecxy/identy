package com.xiaoyu.identy.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by chenxiaoyu
 * Author: chenxiaoyu
 * Date: 2018/5/4
 * Desc:
 */
public class IdentyHelper {

    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }

    private static byte[] hmac_sha(String crypto, byte[] keyBytes,
                                   byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey =
                    new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits,
                                      String crypto) {
        int codeDigits = Integer.decode(returnDigits).intValue();
        String result = null;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        while (time.length() < 16)
            time = "0" + time;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);

        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }

    public static String generate(String secretBase32) {

        String secretHex = "";
        try {
            secretHex = HexEncoding.encode(Base32String.decode(secretBase32));
        } catch (Base32String.DecodingException e) {
            throw new RuntimeException("����Base32����");
        }

        long X = 30;

        String steps = "0";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        long currentTime = System.currentTimeMillis() / 1000L;
        try {
            long t = currentTime / X;
            steps = Long.toHexString(t + 1).toUpperCase();
            while (steps.length() < 8) steps = "0" + steps;

            return generateTOTP(secretHex, steps, "6",
                    "HmacSHA1");
        } catch (final Exception e) {
            throw new RuntimeException("���ɶ�̬�������");
        }
    }

    public static String readZxing(String imgPath) {
        try {
            MultiFormatReader read = new MultiFormatReader();
            File imageFile = new File(imgPath);
            BufferedImage image = ImageIO.read(imageFile);
            Binarizer binarizer = new HybridBinarizer(new BufferedImageLuminanceSource(image));
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Result res = read.decode(binaryBitmap);
            return res.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
