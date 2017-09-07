package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by hasee on 2017/5/29.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);

}
