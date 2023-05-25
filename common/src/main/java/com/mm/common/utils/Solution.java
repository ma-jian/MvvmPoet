package com.mm.common.utils;


import android.text.TextUtils;

/**
 * 服务器版本号比较
 *  如果 version1 > version2 返回 1
 *  如果 version1 < version2 返回 -1
 *  除此之外返回 0
 *  输入：version1 = "1.01", version2 = "1.001"
 *  输出：0
 *  解释：忽略前导零，"01" 和 "001" 都表示相同的整数 "1"
 *  输入：version1 = "1.0", version2 = "1.0.0"
 *  输出：0
 *  解释：version1 没有指定下标为 2 的修订号，即视为 "0"
 *  输入：version1 = "0.1", version2 = "1.1"
 *  输出：-1
 *  解释：version1 中下标为 0 的修订号是 "0"，version2 中下标为 0 的修订号是 "1" 。0 < 1，所以 version1 < version2
 */
public class Solution {

    public static int compareVersion(String version1, String version2) {
        if (TextUtils.isEmpty(version1) && TextUtils.isEmpty(version2)) {
            return 0;
        }
        if (TextUtils.isEmpty(version1)) {
            return -1;
        }
        if (TextUtils.isEmpty(version2)) {
            return 1;
        }
        String[] spiltStr1 = version1.split("\\."); // [1 01]}
        int len1 = spiltStr1.length;
        String[] spiltStr2 = version2.split("\\."); // [1 001]
        int len2 = spiltStr2.length;
        int len = Math.min(len1, len2);
        int i = 0, j = 0;
        while (i < len) {// 比较相同长度的子串的大小
            int num1 = Integer.parseInt(spiltStr1[i]);
            int num2 = Integer.parseInt(spiltStr2[j]);
            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return -1;
            } else {//相同则后移
                i++;
                j++;
            }
        }
        if (i < len1) {//说明version2后面已经没有字符串
            while (i < len1) {
                int num1 = Integer.parseInt(spiltStr1[i]);
                if (num1 > 0) {
                    return 1;
                } else { // 此处只能是 =
                    if (i == len1 - 1) { //最后一位相同
                        return 0;
                    }
                    i++;
                }
            }
        }
        if (j < len2) {//说明version1后面已经没有字符串
            while (j < len2) {
                int num2 = Integer.parseInt(spiltStr2[j]);
                if (num2 > 0) {
                    return -1;
                } else { // 此处只能是 =
                    if (j == len2 - 1) {// 最后一位相同
                        return 0;
                    }
                    j++;
                }
            }
        }
        return 0;//此处是版本的子串数相同，相同版本的情况
    }
}
