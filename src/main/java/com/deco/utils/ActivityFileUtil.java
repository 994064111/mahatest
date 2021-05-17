package com.deco.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class ActivityFileUtil {
	private String UPLOAD_DIR = "";

	@Value("${file.files.path}")
	private String paths;
	@Value("${file.image}")
	private String image;
	@Value("${file.audio}")
	private String audio;
	@Value("${file.video}")
	private String video;

	/**
	 * 文件上传
	 * 
	 * @param file
	 * @param Route
	 * @return
	 */
	public Map<String, Object> uploadingFile(MultipartFile file, int type) {
		Map<String, Object> map = new HashMap<>();
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd"); // 这里的格式可以自己设置
		// format()方法是用来格式化时间的方法

		String date = from.format(new Date());
		try {
			// 图片上传到服务器
			
			String filename = file.getOriginalFilename();// 获得原始文件名
			String string = filename.substring(filename.lastIndexOf(".") + 1);
			String newfileName = System.currentTimeMillis() + (int) (Math.random() * 10000) + "." + string;
			String path = "";
			if (type==0) {
				UPLOAD_DIR = paths+image+"/";
			} else if (type==1) {
				UPLOAD_DIR = paths+audio+"/";
			} else if (type==2) {
				UPLOAD_DIR = paths+video+"/";
			}
			// if (Route == null && Route == "") {
			path = UPLOAD_DIR + date+"/";
			/*
			 * } else { path = Route + date; }
			 */
			File f = new File(path);
			if (!f.exists() && !f.isDirectory()) {
				f.mkdirs();
			}

			if (!file.isEmpty()) {
				try {
					// file.transferTo(f);
					FileOutputStream fos = new FileOutputStream(path + File.separator + newfileName);
					InputStream in = file.getInputStream();
					byte[] bts = new byte[102400];
					while (in.read(bts) != -1) {
						fos.write(bts);
					}
					fos.close();
					in.close();
					map.put("boolean", true);
					map.put("Route", path + File.separator + newfileName);
					map.put("path",UPLOAD_DIR);
					map.put("name", newfileName);
					map.put("filename", filename);
				} catch (IOException e) {
					e.printStackTrace();
					map.put("boolean", false);
				} catch (Exception e) {
					e.printStackTrace();
					map.put("boolean", false);
				}
			} else {
				map.put("boolean", false);
			}
			// jsonResult.setMessage("上传成功");
			// jsonResult.setObject(path + File.separator + newfileName);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("boolean", false);

			/* jsonResult.setMessage("上传失败"); jsonResult.setSuccess(false); */

		}
		return map;
	}
	/**
	 * 删除文件
	 * @param path
	 * @param request
	 * @return
	 */

	public int deleteFile(String path) {

		int value = 0;
		try {
			/*String a = request.getSession().getServletContext().getRealPath("/");
			String b = a.replaceAll("\\\\", "/");*/
			File file = new File(path);
			file.delete();
			value = 1;
		} catch (Exception e) {
			value = -1;

		}
		return value;
	}

}
