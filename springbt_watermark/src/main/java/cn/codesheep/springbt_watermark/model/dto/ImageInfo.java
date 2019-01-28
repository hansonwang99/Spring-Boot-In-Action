package cn.codesheep.springbt_watermark.model.dto;

public class ImageInfo {

    private String imageUrl;  // 上传文件的 URL相对地址
    private String logoImageUrl;  // 添加了水印后的文件的 URL相对地址

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLogoImageUrl() {
        return logoImageUrl;
    }

    public void setLogoImageUrl(String logoImageUrl) {
        this.logoImageUrl = logoImageUrl;
    }
}
