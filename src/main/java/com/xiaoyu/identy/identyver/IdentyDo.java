package com.xiaoyu.identy.identyver;

import com.xiaoyu.identy.util.IdentyHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenxiaoyu
 * Author: chenxiaoyu
 * Date: 2018/5/4
 * Desc:
 */
public class IdentyDo {
    public static String rexString(String str) {
        String result = "";
        Pattern p = Pattern.compile("secret=([^&?]+)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            result = m.group(1);
        }
        return result;
    }

    public static void main(String[] args) {
        String rootPath = IdentyDo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String identyStr = "";
        String path = rootPath.replaceAll("identy-1.0-SNAPSHOT.jar", "") + "identyStr.txt";

        try {
            File file = new File(path);
            if (!file.exists()) {
                String originIdentyStr = IdentyHelper.readZxing(args[0]);
                identyStr = rexString(originIdentyStr);
                Files.write(Paths.get(path), identyStr.getBytes());
            } else {
                Object[] x = Files.lines(Paths.get(path), StandardCharsets.UTF_8).toArray();
                identyStr = x[0].toString();
            }
        } catch (Exception e) {
        }
        System.out.println(IdentyHelper.generate(identyStr));
    }
}
