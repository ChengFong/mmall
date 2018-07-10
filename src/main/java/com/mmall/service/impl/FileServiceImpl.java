package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by 林成峰 on 2017/8/5.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){

        String fileName = file.getOriginalFilename();

        //擴展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);

        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("開始上傳文件，上傳文件的文件名:{},上傳的路徑:{},新文件名:{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){

            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path, uploadFileName);

        try {

            //MultipartFile轉換為File
            file.transferTo(targetFile);

            //文件上傳，將targetFile上傳到我們的FTP服務器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上傳完之後，刪除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.error("上完文件異常", e);
            return null;
        }

        return targetFile.getName();
    }
}
