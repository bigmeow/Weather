package com.test;

import com.util.WeatherUtil;

/**
 * 测试用例
 * @author running@vip.163.com
 *
 */
public class Test {

	@org.junit.Test
	public void test1() {
		System.out.println("测试1－－－－－－精确到区－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "湖南省";
		String city = "长沙市";
		String district = "天心区";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city, district);
		System.out.println("天气数据："+info);
	}
	
	@org.junit.Test
	public void test2() {
		System.out.println("测试2－－－－－－精确到县－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "湖南省";
		String city = "永州市";
		String district = "双牌县";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city, district);
		System.out.println("天气数据："+info);
	}
	
	@org.junit.Test
	public void test3() {
		System.out.println("测试3－－－－－－精确到市－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "湖南";
		String city = "永州";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city);
		System.out.println("天气数据："+info);
	}
	
	@org.junit.Test
	public void test4() {
		System.out.println("测试4－－－－－－直辖市－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "上海";
		String city = "上海";
		String district = "浦东";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city,district);
		System.out.println("天气数据："+info);
	}
	
	
	@org.junit.Test
	public void test5() {
		System.out.println("测试5－－－－－－省直辖县－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "河南";
		String city = "济源";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city);
		System.out.println("天气数据："+info);
	}
	
	@org.junit.Test
	public void test6() {
		System.out.println("测试6－－－－－－省直辖县－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－");
		String province = "河南";
		String city = "省直辖";
		String district = "济源";
		WeatherUtil weather = WeatherUtil.getInstance();
		String info=weather.fetchWeatherInfo(province, city,district);
		System.out.println("天气数据："+info);
	}
}
