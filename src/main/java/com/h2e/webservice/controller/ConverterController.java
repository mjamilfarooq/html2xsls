package com.h2e.webservice.controller;


import com.h2e.webservice.FileStorageProperties;
import com.h2e.webservice.model.Html2Xsls;
import com.h2e.webservice.service.LicenseManager;
import com.h2e.webservice.service.FileStorageService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;



@RestController
@RequestMapping(value = "/html2excel")
public class ConverterController {

    private static final Logger logger = LoggerFactory.getLogger(ConverterController.class);


    @Autowired
    FileStorageProperties fileStorageProperties;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    LicenseManager licenseManager;

    @PostMapping("/converter")
    public ResponseEntity<Resource> converter(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {

        File html = fileStorageService.storeFile(file);

        Html2Xsls converter = new Html2Xsls();

        boolean islicensed = licenseManager.isValid();


        XSSFWorkbook workBook = converter.CreateExcelFromHtml(html, islicensed);
        String randomFileName = UUID.randomUUID().toString() + ".xlsx";
        FileOutputStream xlsx = fileStorageService.createFileOutputStream(randomFileName);
        workBook.write(xlsx);
        workBook.close();
        xlsx.close();

        html.delete(); // deleting input file

        // Try to determine file's content type
        String contentType = null;
        Resource resource = null;
        String resourceFilename = null;

        try {
            // Load file as Resource
            resource = fileStorageService.loadFileAsResource(randomFileName);
            String absPath = resource.getFile().getAbsolutePath();
            logger.info("Absolute Path for the file : " + absPath);
            contentType = request.getServletContext().getMimeType(absPath);
            resourceFilename = resource.getFilename();

        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceFilename + "\"")
                .body(resource);
    }


}