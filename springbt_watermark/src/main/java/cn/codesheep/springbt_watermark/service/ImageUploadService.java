package cn.codesheep.springbt_watermark.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ImageUploadService {

    /**
     * 功能：上传图片
     * @param file 文件
     * @param uploadPath 服务器上上传文件的路径
     * @param physicalUploadPath  服务器上上传文件的物理路径
     * @return 上传文件的 URL相对地址
     */
    public String uploadImage( MultipartFile file, String uploadPath, String physicalUploadPath ) {

        String filePath = physicalUploadPath + file.getOriginalFilename();

        try {
            File targetFile=new File(filePath);
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uploadPath + "/" + file.getOriginalFilename();
    }
}
