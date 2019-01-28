package cn.codesheep.springbt_watermark.service;

import java.io.File;

public interface WatermarkService {

    /**
     * 功能：给上传的图片添加水印
     *
     * @param imageFile      待添加水印的文件
     * @param imageFileName  文件名称
     * @param uploadPath     文件在服务器上的相对路径
     * @param realUploadPath 文件在服务器上的物理路径
     * @return 添加水印后的文件的地址
     */
    String watermarkAdd( File imageFile, String imageFileName, String uploadPath, String realUploadPath );
}
