package me.getwallpaper.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Interfacelift_Downloader {

	/**
	 * 页数
	 */
	public static Integer pageNo = 0;
	
	/**
	 * 保存的路径
	 */
	public static String path = "";
	
	/**
	 * 分辨率
	 */
	public static String resolution = "";
	
	
	public static void main(String[] args) throws Exception {
		init(args);
	}

	/**
	 * 程序启动的初始化操作 
	 * 	参数读取
	 * @param args
	 */
	public static void init(String[] args){
		if(args.length % 2 > 0){
			System.out.println("参数不正确 , 每个参数名后面都要跟对应的值 ");
			System.exit(1);
		}
		//如果有获取帮助的参数.直接结束程序
		for (String arg : args) {
			if(arg.equals("-?") || arg.equals("-h") || arg.equals("-help")){
				System.out.println("可以指定的参数  : \n -i \t 指定从第几页开始下载(default/min : 1) \n -r \t 指定分辨率大小(default: 1920x1080 ) \n -p \t 指定保存的路径(default : C:\\Wallpapers)");
				System.exit(1);
			}
		}
		//得到参数内容
		for (int i = 0; i < args.length; i+=2) {
			if(args[i].equals("-r") ){
				resolution = args[i+1];
			}else if(args[i].equals("-p")){
				path = args[i+1];
			}else if(args[i].equals("-i")){
				if(isInteger(args[i+1])){
					try {
						pageNo = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("指定的页数不正确 , 请使用整数 ");
					}
				}
			}
		}
		
		if(path.equals("")){
			System.out.println("没有指定保存目录 , 程序将自动把图片保存在  C:\\Wallpapers ");
			path = "C:\\Wallpapers\\";
			File file = new File("C:\\Wallpapers\\");
			file.mkdirs();
		}
		
		if(pageNo == 0){
			System.out.println("没有指定起始页数 , 程序默认从第一页开始");
			pageNo = 1;
		}
		
		if(resolution.equals("")){
			resolution = "1920x1080";
			System.out.println("没有指定分辨率 , 程序将自动默认分辨率为 1920x1080 ");
		}
		
		//如果路径后面没有添加斜杠
		if(!path.substring(path.length()-1, path.length()).equals("\\")){
			path+="\\";
		}
		
		//没有这个目录
		File file = new File(path);
		if(!file.exists())
			file.mkdirs();
		
		//程序开始执行
		run();
	}
	
	/**
	 * 开始爬网页
	 */
	public static void run () {
		System.out.println("------------------------------------------");
		System.out.println("pageNo : " + pageNo);
		System.out.println("------------------------------------------");
		String html = getHTML("https://interfacelift.com/wallpaper/downloads/downloads/wide_16:9/"+resolution+"/index"+pageNo+".html");
		getImgUrl(html);
	}
	
	/**
	 * 正则匹配图片URL
	 * @param html
	 */
	public static void getImgUrl(String html){
		String regx = "/wallpaper/[^\u4e00-\u9fa5\\s]{0,}_"+resolution+".jpg";
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			String url = "http://interfacelift.com/"+matcher.group(0);
			downloadImage(url);
		}
		pageNo++;
		run();
	}
	
	/**
	 * 得到HTML
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public static String getHTML(String urlStr) {
		String buf = "";
		try {
			URL url = new URL(urlStr);
			HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
			httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0");
			
			InputStreamReader input = new InputStreamReader(
					httpConn.getInputStream());
			
			BufferedReader bufReader = new BufferedReader(input);
			String line = "";
			StringBuilder contentBuf = new StringBuilder();
			while ((line = bufReader.readLine()) != null) {
				contentBuf.append(line);
			}
			buf = contentBuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf;
	}
	
	/**
	 * 下载图片
	 * @param urlStr
	 */
	public static void downloadImage(String urlStr) {
		String format = urlStr.substring(urlStr.lastIndexOf("."),urlStr.length());
		String fileName = urlStr.substring(urlStr.indexOf("_")+1,urlStr.lastIndexOf("_"))+format;
		System.out.print("downloading "+fileName+"");
		try {
			
			URL url = new URL(null, urlStr ,new sun.net.www.protocol.https.Handler());
			HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
			httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0");
			InputStream inStream = httpConn.getInputStream();
            byte[] btImg = readInputStream(inStream);//得到图片的二进制数据   
            
            File file = new File(path + fileName);   
            FileOutputStream fops = new FileOutputStream(file);   
            fops.write(btImg);   
            fops.flush();   
            fops.close();
            System.out.print("\t\t\t\t\t successful \n");   
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**  
     * 从输入流中获取数据  
     * @param inStream 输入流  
     * @return  
     * @throws Exception  
     */  
    public static byte[] readInputStream(InputStream inStream) throws Exception{   
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();   
        byte[] buffer = new byte[1024];   
        int len = 0;   
        while( (len=inStream.read(buffer)) != -1 ){   
            outStream.write(buffer, 0, len);   
        }   
        inStream.close();   
        return outStream.toByteArray();   
    }  

    /**
     * 判断是不是数字
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {    
    	Pattern pattern = Pattern.compile("[0-9]*");  
        return pattern.matcher(str).matches(); 
      } 
}
