/*
 * Copyright 2016 Zhang Jingye
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.zjy.zxcardumper;

import java.nio.charset.Charset;

import android.util.Log;
/**
 * Created by ZJY on 10/15/2016.
 */

public class zxcard {
    public String UID;
    public String defaultSignature;
    // Sector 1 block 0
    short countA;
    float balanceA;
    short seqPayCountA;
    short payMonthA;
    short checksumA;
    // Sector 1 block 1
    short countB;
    float balanceB;
    short seqPayCountB;
    short payMonthB;
    short checksumB;
    // Sector 1 block 2
    public short cardNum;
    public short distributionTime; // ?
    // Sector 2
    public String cardOwnerName;
    // flag
    boolean AorB = false;
    boolean justDeposit = false;

    public zxcard (String[] s0, String[] s1, String[] s2) {
        // process s0
        this.UID = cut(s0[0], 0, 8);
        this.defaultSignature = cut(s0[0], 16, 32);
        // process s1
        // zone A
        this.countA = Short.parseShort(cut(s1[0], 0, 4), 16);
        this.balanceA = Integer.parseInt(cut(s1[0], 8, 14)) / 100f;
        this.seqPayCountA = Short.parseShort(cut(s1[0], 16, 18), 16);
        this.payMonthA = Short.parseShort(cut(s1[0], 18, 20));
        this.checksumA = checksum(s1[0]);
        // zone B
        this.countB = Short.parseShort(cut(s1[1], 0, 4), 16);
        this.balanceB = Integer.parseInt(cut(s1[1], 8, 14)) / 100f;
        this.seqPayCountB = Short.parseShort(cut(s1[1], 16, 18), 16);
        this.payMonthB = Short.parseShort(cut(s1[1], 18, 20));
        this.checksumB = checksum(s1[1]);
        // process s2
        this.cardNum = Short.parseShort(cut(s2[0], 4, 8), 16);
        this.distributionTime = Short.parseShort(cut(s1[2],22,24), 10);
        this.cardOwnerName = cut(s2[0],8,32);
        // Misc
        if (this.countA > this.countB) this.AorB = true;
        if (this.countA == this.countB) this.justDeposit = true;
    }

    public short getCount() {
        return (AorB) ? countA : countB;
    }

    public float getBalance() {
        return (AorB) ? balanceA : balanceB;
    }

    public float getPurchase() {
        return Math.abs(balanceA - balanceB);
    }

    public short getSeqPayCount() {
        return (AorB) ? seqPayCountA : seqPayCountB;
    }

    public short getPayMonth() {
        return (AorB) ? payMonthA : payMonthB;
    }

    public String getCardOwnerName() {
        String iout = convertHexToString(this.cardOwnerName);
        Log.w("zxcard", iout);
        // plain hex recognized as gbk
        String gout = new String(iout.getBytes(Charset.forName("ISO-8859-1")), Charset.forName("gbk"));
        Log.w("zxcard", gout);
        return gout;
    }

    public boolean verifyChecksum(short sum) {
        int all = 0;
        for (int i = 0; i < 0x7f; i++) {
            all = i * 0x100 + 0xff;
            if (sum == all) return true;
        }
        return false;
    }

    private String cut (String in, int start, int end) {
        return in.substring(start, end);
    }

    public short checksum (String line) {
        short sum = 0;
        for (int i = 0; i < 16; i++) {
            String tmp = cut(line, 2 * i, 2 * i + 1) + cut(line, 2 * i + 1, 2 * i + 2);
            short tmpi = Short.parseShort(tmp,16);
            sum += tmpi;
        }
        return sum;
    }
    public String convertStringToHex(String str){

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }

    public String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //if 0 then drop it in case it becomes an strange char
            if (decimal == 0) continue;
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }
        //System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

}
