package com.h2e.webservice.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class LicenseManagerImpl implements LicenseManager {
    private static String keys [] = new String []
    {
        "3d12c623-4bb6-40e7-b086-2fa87d7cfa34",
        "e9ecfdff-de03-46be-85e9-77f5e3820c20",
        "ef9fefe5-9765-4e56-bf35-284fa604ecc4",
        "83a274ae-8431-4821-8365-5d622581a866",
        "9e2012b7-51f6-4d3a-b992-36bad9d187c0",
        "c9f61484-19a2-49d9-a8f4-e14a67390f15",
        "1a23d0cd-2b9d-4629-98a2-057c326c255e",
        "748fe627-ad9a-4063-80dc-b36b947caa18",
        "2aeb2fb1-ed66-43da-8a36-7761a6bc3946",
        "d8dac290-1d93-46dd-a12f-f1d85bc59724"
    };



    @Value("${license.key}")
    private String license_key;

    private static final Logger logger = LoggerFactory.getLogger(LicenseManagerImpl.class);

    //expiry date constituents
    private final int year = 2018;
    private final int month = Calendar.SEPTEMBER;
    private final int date = 1;

    private Calendar expiry = Calendar.getInstance();

    private boolean isTrialExpired()
    {

        expiry.set(year, month, date);

        Calendar today = Calendar.getInstance();

        if (today.after(expiry))
        {
            logger.warn("Trial version has expired ");
            return true;
        }
        else
        {
            logger.error("Expiry date " + expiry.toString());
            return false;
        }

    }

    public  boolean isValid() throws Exception
    {


        boolean valid = false;

        for (String itr: keys)
        {
            if ( itr.equals(license_key) )
            {
                valid = true;
                break;
            }
        }

        if (!valid && isTrialExpired()) throw new Exception("Trial version has expired");

        else return valid;
    }
}
