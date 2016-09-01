package com.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 获取国内天气
 * @author running@vip.163.com
 * @since 数据接口抓取来源：中国气象频道（http://3g.tianqi.cn/）
 * @version 1.0
 */
public final class WeatherUtil {
	/**
	 * 城市编码映射表，默认放src目录下，编码表目录来源：http://3g.tianqi.cn/getAllCitys.do
	 */
	private static final String codeFileName = "cityCode.json";
	
	/**
	 * 接口地址
	 */
	private static final String apiURL="http://3g.tianqi.cn/loginSk.do";
	private static WeatherUtil me;
	private JSONArray jsonArray = null;
	
	private static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

	private WeatherUtil() {
		initEnv();
	}
	
	/**
	 * 配置管理器构造工厂(单例)
	 * @return 天气工具类实例或者null
	 */
	public static WeatherUtil getInstance() {
		try {
			return me==null?new WeatherUtil():me;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	
	private void initEnv() {
		//加载城市编码映射表
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream input = classLoader.getResourceAsStream(WeatherUtil.codeFileName);
		if (null == input) {
			throw new RuntimeException("未找到文件："+WeatherUtil.codeFileName);
		}

		try {
			//映射表解析为JSONArray对象
			String configContent = IOUtils.toString(input);
			JSONArray jsonArray = JSONArray.parseArray(this.filter(configContent));
			this.jsonArray = jsonArray;
		} catch (Exception e) {
			this.jsonArray = null;
		}
	}
	
	/**
	 *过滤输入字符串, 剔除多行注释以及替换掉反斜杠 
	 */
	private String filter(String input) {
		return input.replaceAll("/\\*[\\s\\S]*?\\*/", "");
	}
	
	/**
	 * 检查字符串是否为null或者空字符串
	 * @param str
	 * @return
	 */
	private boolean checkIsEmpty(String str){
		return str==null||str.isEmpty();
	}
	
	/**
	 * 遍历获取城市code,
	 * @param province  省名（比如"湖南"，"上海"）
	 * @param city	城市名（比如"长沙"，"上海"）
	 * @param district 	区县名(比如"浦东"，可为null或空字符串)
	 * @return 城市code
	 */
	private String getCode(String province,String city,String district){
		String code=null;
		Iterator<Object> iter = this.jsonArray.iterator();
		flag:
		while (iter.hasNext()) {
			JSONObject json = (JSONObject) iter.next();
			//System.out.println("省"+json.get("ch"));
			//匹配省
			if(json.getString("ch").indexOf(province)>-1){
				//向下匹配市
				Iterator<Object> iter2=json.getJSONArray("beans").iterator();
				while(iter2.hasNext()){
					JSONObject json2 = (JSONObject) iter2.next();
					//System.out.println("市"+json2.getString("ch"));
					if(json2.getString("ch").indexOf(city)>-1){
						//若区县参数为空或者未匹配到，则取城市的code
						code=json2.getString("id");
						System.out.println("城市［"+city+"］的code是："+code);
						if(checkIsEmpty(district)){
							break flag;
						}
						//向下匹配区县
						Iterator<Object> iter3=json2.getJSONArray("beans").iterator();
						while(iter3.hasNext()){
							JSONObject json3 = (JSONObject) iter3.next();
							//System.out.println("区县："+json3.getString("ch"));
							if(json3.getString("ch").indexOf(district)>-1){
								code=json3.getString("id");
								System.out.println("区县［"+district+"］的code是："+code);
								break flag;
							}
						}
					}
				}
			}
		}
		return code;
	}
	
	/**
	 *
	 * @param strUrl 请求地址
	 * @param params 请求参数
	 * @param method 请求方法
	 * @return 网络请求字符串
	 * @throws Exception
	 */
	private String net(String strUrl, Map<String, String> params, String method) throws Exception {
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		String rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			if (method == null || method.equals("GET")) {
				strUrl += params == null ? "" : ((strUrl.indexOf("?") > -1 ? "&" : "?") + urlencode(params));
			}
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			if (method == null || method.equals("GET")) {
				conn.setRequestMethod("GET");
			} else {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
			}
			conn.setRequestProperty("User-agent", userAgent);
			conn.setUseCaches(false);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			if (params != null && method.equals("POST")) {
				try {
					DataOutputStream out = new DataOutputStream(conn.getOutputStream());
					out.writeBytes(urlencode(params));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 如果遇到重定向状态码，则调用重定向后的地址
			if (conn.getResponseCode() == 302) {
				return net(conn.getHeaderField("Location"), null, method);
			}
			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sb.append(strRead);
			}
			rs = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return rs;
	}

	// 将map型转为请求参数型
	private String urlencode(Map<String, String> data) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> i : data.entrySet()) {
			try {
				sb.append(i.getKey()).append("=")
						.append(URLEncoder.encode(i.getValue() + "", "UTF-8"))
						.append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	/**
	 * 抓取指定地点的天气数据（省、市、区、县后缀可省略）
	 * @param province  省名（比如"湖南"，"上海"）
	 * @param city	城市名（比如"长沙"，"上海"）
	 * @param district 	区县名(比如"浦东"，可为null或空字符串)
	 * @return json字符串
	 */
	public String fetchWeatherInfo(String province,String city,String district){
		String result = null;
		if(checkIsEmpty(province)||checkIsEmpty(city)){
			throw new RuntimeException("省名、城市名参数必填");
		}
		province=province.replaceAll("省", "");
		city=city.replaceAll("市|区", "");
		if(!checkIsEmpty(district)){
			district=district.replaceAll("县", "");
		}
		String code=this.getCode(province, city, district);
		Map<String, String> params = new HashMap<String, String>();// 请求参数
		params.put("cityCode", code);
		try {
			result = net(apiURL, params, "GET");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 抓取指定地点的天气数据
	 * @param province  省名（比如"湖南"，"上海"）
	 * @param city	城市名（比如"长沙"，"上海"）
	 * @return json字符串
	 */
	public String fetchWeatherInfo(String province,String city){
		return fetchWeatherInfo(province,city,null);
	}
	
	
	public static void main(String[] args) {
		String province = "上海";
		String city = "上海";
		String district = "普陀";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city, district);
		System.out.println("天气数据："+info);;
	}
	
}
