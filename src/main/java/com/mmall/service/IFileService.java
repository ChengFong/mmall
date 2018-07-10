package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by 林成峰 on 2017/8/5.
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
