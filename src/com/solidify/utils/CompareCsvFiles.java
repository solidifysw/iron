package com.solidify.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by jrobins on 1/21/15.
 */
public class CompareCsvFiles {

    public static void main(String[] args) {
        BufferedReader brOld = null;
        BufferedReader brNew = null;
        BufferedWriter out = null;
        System.out.println("started");
        try {
            out = new BufferedWriter(new FileWriter("/Users/jrobins/Desktop/out.csv"));
            out.write("\"EE Name\",\"EE dob\",\"EE SSN\",\"2014 Plan\",\"2014 Benefit\",\"2015 Plan\",\"2015 Benefit\"");
            out.newLine();

            brNew = new BufferedReader(new FileReader("/Users/jrobins/Desktop/2015.csv"));

            String line = brNew.readLine(); // column headings
            line = brNew.readLine();
            HashMap<String,CoverageRow> covs = new HashMap<String,CoverageRow>();


            while (line != null) {
                StringTokenizer in = new StringTokenizer(line, "|");

                String memberId = in.nextToken();
                String eeName = in.nextToken();
                String dob = in.nextToken();
                String ssn = in.nextToken();
                String date = in.nextToken();
                String orderId = in.nextToken();
                String product = in.nextToken();
                if (!product.equals("defaultMedical")) {
                    line = brNew.readLine();
                    continue;
                }
                String planName = in.nextToken();
                String benefit = in.nextToken();
                if (benefit.equals("Decline")) {
                    planName = "";
                }

                CoverageRow cr = new CoverageRow(eeName,memberId,dob,ssn,orderId,product,planName,benefit,date);
                String key = ssn + "_" + dob;
                if (covs.containsKey(key)) {
                    if (!benefit.equals("Decline")) {
                        CoverageRow saved = covs.get(key);
                        if (saved.getBenefit().equals("Decline") || cr.isAfter(saved)) {
                            covs.remove(key);
                            covs.put(key, cr);
                        }
                    }
                } else {
                    covs.put(key, cr);
                }
                line = brNew.readLine();
            }

            brOld = new BufferedReader(new FileReader("/Users/jrobins/Desktop/2014.csv"));
            String oldLine = brOld.readLine(); // column headings
            oldLine = brOld.readLine();
            HashMap<String,CoverageRow> oldCovs = new HashMap<String,CoverageRow>();

            while(oldLine != null) {
                StringTokenizer inOld = new StringTokenizer(oldLine, "|");

                String memberIdOld = inOld.nextToken();
                String eeNameOld = inOld.nextToken();
                String dobOld = inOld.nextToken();
                String ssnOld = inOld.nextToken();
                String dateOld = inOld.nextToken();
                String orderIdOld = inOld.nextToken();
                String productOld = inOld.nextToken();
                if (!productOld.equals("defaultMedical")) {
                    oldLine = brOld.readLine();
                    continue;
                }
                String planNameOld = inOld.nextToken();
                String benefitOld = inOld.nextToken();

                if (benefitOld.equals("Decline")) {
                    planNameOld = "";
                }
                CoverageRow ocr = new CoverageRow(eeNameOld, memberIdOld, dobOld, ssnOld, orderIdOld, productOld, planNameOld, benefitOld,dateOld);

                String oldKey = ssnOld + "_" + dobOld;
                if (oldCovs.containsKey(oldKey)) {
                    if (!benefitOld.equals("Decline")) {
                        CoverageRow saved = oldCovs.get(oldKey);
                        if (saved.getBenefit().equals("Decline") || ocr.isAfter(saved)) {
                            oldCovs.remove(oldKey);
                            oldCovs.put(oldKey, ocr);
                        }
                    }
                } else {
                    oldCovs.put(oldKey,ocr);
                }
                oldLine = brOld.readLine();
            }

            for (String key : covs.keySet()) {
                CoverageRow latest = covs.get(key);
                CoverageRow old = null;
                if (oldCovs.containsKey(key)) {
                    old = oldCovs.get(key);
                    if (!latest.equals(old)) {
                        out.write("\"" + latest.getName() + "\",\"" + latest.getDob() + "\",\"" + latest.getSsn() + "\",\"" + old.getPlanName() + "\",\"" + old.getBenefit() + "\",\"" + latest.getPlanName() + "\",\"" + latest.getBenefit() + "\"");
                        out.newLine();
                    }
                    oldCovs.remove(key);
                } else {
                    if (!latest.getBenefit().equals("Decline")) {
                        out.write("\"" + latest.getName() + "\",\"" + latest.getDob() + "\",\"" + latest.getSsn() + "\",\" \",\" \",\"" + latest.getPlanName() + "\",\"" + latest.getBenefit() + "\"");
                        out.newLine();
                    }
                }
            }

            // are there coverages in old that are not in new?
            for (String key : oldCovs.keySet()) {
                CoverageRow old = oldCovs.get(key);
                if (!covs.containsKey(key)) {
                    out.write("\"" + old.getName() + "\",\"" + old.getDob() + "\",\"" + old.getSsn() + "\",\"" + old.getPlanName() + "\",\"" + old.getBenefit() + "\",,");
                    out.newLine();
                }
            }
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                brNew.close();
                brOld.close();
                out.close();
            } catch (Exception e ) {}
        }
    }
}
