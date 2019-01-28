
# 《基于Spring Boot实现图片上传/加水印一把梭操作》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---

---

## 概述

很多网站的图片为了版权考虑都加有水印，尤其是那些图片类网站。自己正好最近和图片打交道比较多，因此就探索了一番基于 Spring Boot这把利器来实现从 **图片上传 → 图片加水印** 的一把梭操作！

>**注：** 本文首发于  [**My Personal Blog：程序羊**](http://www.codesheep.cn)，欢迎光临 [**小站**](http://www.codesheep.cn)

本文内容脑图如下：

![本文内容脑图](https://upload-images.jianshu.io/upload_images/9824247-0c7821414a4de19f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


---

## 搭建 Spring Boot基础工程

过程不再赘述了，这里给出 pom中的关键依赖：

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.5</version>
        </dependency>
    </dependencies>
```

---

## 编写文件上传服务

- 主要就是编写 **ImageUploadService** 服务

里面仅一个上传图片的方法：`uploadImage ` 方法

```java
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
```

---

## 编写图片加水印服务

- 编写 **ImageWatermarkService** 服务

里面就一个主要的 `watermarkAdd`方法，代码后面写有详细解释

```java
@Service
public class ImageWatermarkService {

    /**
     * imgFile 图像文件
     * imageFileName 图像文件名
     * uploadPath 服务器上上传文件的相对路径
     * realUploadPath 服务器上上传文件的物理路径
     */
    public String watermarkAdd( File imgFile, String imageFileName, String uploadPath, String realUploadPath ) {

        String imgWithWatermarkFileName = "watermark_" + imageFileName;
        OutputStream os = null;

        try {
            Image image = ImageIO.read(imgFile);

            int width = image.getWidth(null);
            int height = image.getHeight(null);

            BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);  // ①
            Graphics2D g = bufferedImage.createGraphics();  // ②
            g.drawImage(image, 0, 0, width,height,null);  // ③

            String logoPath = realUploadPath + "/" + Const.LOGO_FILE_NAME;  // 水印图片地址
            File logo = new File(logoPath);        // 读取水印图片
            Image imageLogo = ImageIO.read(logo);

            int markWidth = imageLogo.getWidth(null);    // 水印图片的宽度和高度
            int markHeight = imageLogo.getHeight(null);

            g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, Const.ALPHA) );  // 设置水印透明度
            g.rotate(Math.toRadians(-10), bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);  // 设置水印图片的旋转度

            int x = Const.X;
            int y = Const.Y;

            int xInterval = Const.X_INTERVAL;
            int yInterval = Const.Y_INTERVAL;

            double count = 1.5;
            while ( x < width*count ) {  // 循环添加多个水印logo
                y = -height / 2;
                while( y < height*count ) {
                    g.drawImage(imageLogo, x, y, null);  // ④
                    y += markHeight + yInterval;
                }
                x += markWidth + xInterval;
            }

            g.dispose();

            os = new FileOutputStream(realUploadPath + "/" + imgWithWatermarkFileName);
            JPEGImageEncoder en = JPEGCodec.createJPEGEncoder(os); // ⑤
            en.encode(bufferedImage); // ⑥

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return uploadPath + "/" + imgWithWatermarkFileName;
    }

}
```

代码思路解释如下：

>可以对照代码中的标示数字和下面的解释进行理解：

① 创建缓存图片
② 创建绘图工具
③ 将原图绘制到缓存图片
④ 将水印logo绘制到缓存图片
⑤ 创建图像编码工具类
⑥ 编码缓存图像生成目标图片

可见思路清晰易懂！

---

## 编写 图片上传/处理 控制器

我们在该控制器代码中将上述的 **图片上传服务** 和 **图片加水印服务** 给用起来：

```
@RestController
public class WatermarkController {

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ImageWatermarkService watermarkService;

    @RequestMapping(value = "/watermarktest", method = RequestMethod.POST)
    public ImageInfo watermarkTest( @RequestParam("file") MultipartFile image ) {

        ImageInfo imgInfo = new ImageInfo();

        String uploadPath = "static/images/";  // 服务器上上传文件的相对路径
        String physicalUploadPath = getClass().getClassLoader().getResource(uploadPath).getPath();  // 服务器上上传文件的物理路径

        String imageURL = imageUploadService.uploadImage( image, uploadPath, physicalUploadPath );
        File imageFile = new File(physicalUploadPath + image.getOriginalFilename() );

        String watermarkAddImageURL = watermarkService.watermarkAdd(imageFile, image.getOriginalFilename(), uploadPath, physicalUploadPath);

        imgInfo.setImageUrl(imageURL);
        imgInfo.setLogoImageUrl(watermarkAddImageURL);
        return imgInfo;
    }
}
```

---

## 实际实验与效果展示

我们用 Postman工具来辅助我们发出 `localhost:9999/watermarktest` 请求，进行图片上传的操作：

![Postman发请求进行图片上传](https://upload-images.jianshu.io/upload_images/9824247-2760c53c1360341e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

之后我们再去项目的资源目录下查看上传的**原图** 和 **加完水印后**图片的效果如下：

![原图](https://upload-images.jianshu.io/upload_images/9824247-4bdebb0778a49977.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![加完水印后的图片](https://upload-images.jianshu.io/upload_images/9824247-bd1be3c8e0435fc8.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

喔唷，这水印 Logo是不是打的有点多... 

不过这下终于不用害怕别人对您的图片侵权啦 ！

---

## 后记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---


