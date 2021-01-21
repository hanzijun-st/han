package com.qianlima.offline.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by Administrator on 2021/1/19.
 */
public class StrUtil {

    /**
     * 删除某个字符
     * @param str
     * @param delChar
     * @return
     */
    public static String deleteString(String str, char delChar) {
        StringBuffer stringBuffer = new StringBuffer("");
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != delChar) {
                stringBuffer.append(str.charAt(i));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 字符串判空
     * @param value
     * @return
     */
    public static final boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    //将中文转换为英文
    public static String getEname(String name) {
        HanyuPinyinOutputFormat pyFormat = new HanyuPinyinOutputFormat();
        pyFormat.setCaseType(HanyuPinyinCaseType. LOWERCASE);
        pyFormat.setToneType(HanyuPinyinToneType. WITHOUT_TONE);
        pyFormat.setVCharType(HanyuPinyinVCharType. WITH_V);

        try {
            return PinyinHelper. toHanyuPinyinString(name, pyFormat, "");
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
        }
        return null;
    }

    //姓、名的第一个字母需要为大写
    public static String getUpEname(String name) {
        char[] strs = name.toCharArray();
        String newname = null;

        //名字的长度
        if (strs.length == 2) {
            newname = toUpCase(getEname ("" + strs[0])) + " "
                    + toUpCase(getEname ("" + strs[1]));
        } else if (strs. length == 3) {
            newname = toUpCase(getEname ("" + strs[0])) + " "
                    + toUpCase(getEname ("" + strs[1] + strs[2]));
        } else if (strs. length == 4) {
            newname = toUpCase(getEname ("" + strs[0] + strs[1])) + " "
                    + toUpCase(getEname ("" + strs[2] + strs[3]));
        } else {
            newname = toUpCase(getEname (name));
        }

        return newname;
    }

    //首字母大写
    private static String toUpCase(String str) {
        StringBuffer newstr = new StringBuffer();
        newstr.append((str.substring(0, 1)).toUpperCase()).append(
                str.substring(1, str.length()));

        return newstr.toString();
    }

}
