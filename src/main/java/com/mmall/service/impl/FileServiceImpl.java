package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by hasee on 2017/5/29.
 */
@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {


    /**
     * 图片上传
     * @param file
     * @param path
     * @return
     */
    public String upload(MultipartFile file,String path){

        String fileName = file.getOriginalFilename();
        String targetFileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() + "." + targetFileExtensionName;
        log.info("开始上传文件，文件名为:{},上传路径为:{},新文件名为:{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);

        try {
            //上传到tomcatWebApp upload目录
            file.transferTo(targetFile);

            //上传到FTP服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

             //删除本地图片文件
            targetFile.delete();

        } catch (IOException e) {
            log.error("上传图片到ftp服务器异常！",e);
            return null;
        }
        return targetFile.getName();
    }
}
