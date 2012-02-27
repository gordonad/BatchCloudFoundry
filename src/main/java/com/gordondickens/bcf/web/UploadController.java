package com.gordondickens.bcf.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@Controller
@RequestMapping("/upload")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    //TODO Externalize Strings

    @Autowired
    @Qualifier("fileTypes")
    ArrayList<String> fileTypes;

    @RequestMapping(method = RequestMethod.GET)
    public String getUploadForm(Model model) {
        logger.debug(" **** UPLOAD GET REQUEST *****");
        model.addAttribute("uploadfile", new UploadFile());
        model.addAttribute("filetypes", fileTypes);
        return "upload/uploadfile";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@ModelAttribute("uploadfile") UploadFile uploadFile, BindingResult result) {
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                logger.error("Error ({}) - {}", error.getCode(), error.getDefaultMessage());
            }
            return "upload/uploadFile";
        }

        MultipartFile multipartFile = uploadFile.getMultipartFile();
        String fileType = uploadFile.getFileType();

        logger.debug("********* File Type {}", fileType);

        if (!multipartFile.isEmpty()) {
        try {
            byte[] bytes = uploadFile.getMultipartFile().getBytes();
            logger.debug("********* File Contents {}", new String(bytes));
            // TODO - Write the File?
            // TODO - Invoke Spring Integration with Claim Check

        } catch (IOException ioe) {
            logger.error("Error Locating or Reading File!");
            return "upload/uploadFile";
        }

        // Some type of file processing...
        logger.info("*******************************************");
        logger.info("Upload File {}", multipartFile.getName());
        logger.info("Original File {}", multipartFile.getOriginalFilename());
        logger.info("*******************************************");

        }
        return "redirect:/index";
    }
}
