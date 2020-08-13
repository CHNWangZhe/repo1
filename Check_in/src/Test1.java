import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test1 {
	public static void main(String[] args) {
		try (BufferedReader bReader = new BufferedReader(new FileReader("FDSdata.txt"))) {//定义缓冲器
			
			Properties airport = new Properties();
			airport.load(new FileReader("airport.txt"));//将机场中文名称和三字码相匹配
			

			Tools tools = new Tools();
			Hashtable<String, Tools> planetable = new Hashtable<>();
			System.out.println("航班号               目的站               办票时间                    柜台号                                                                                                          备注");
			//控制输出格式头
//			String machapcd = "ddtm=(\\d{14}).*AIRL\\Wflid=(\\d{7}).*ffid=(\\w{2})\\W(\\d{1,4}).*-D.*fptt=(\\d{14}).*arno=2, apcd=(\\w{3})"; 
			String machapcd = "ddtm=(\\d{14}).*AIRL\\Wflid=(\\d{7}).*ffid=(\\w{2})\\W(\\d{3,4}).*-D.*fptt=(\\d{14}).*arno=2, apcd=(\\w{3,4})";
			//正则表达式一，用以找到航班号，现在时间，航班ID，起飞时间和目的地
			String machtime = ".*CKLS\\Wflid=(\\d{7}).*code=(\\w{1}\\d{2})";
			//正则表达式二，用以找到柜台号
			Pattern papcd = Pattern.compile(machapcd);
			Pattern ptime = Pattern.compile(machtime);
			String line = null;

			while ((line = bReader.readLine()) != null) {
				Matcher mapcd = papcd.matcher(line);
				Matcher mtime = ptime.matcher(line);
				if (mapcd.find()) {//如果能够找到和正则表达式一相匹配的行
					tools.ffid = mapcd.group(3) + mapcd.group(4);
					tools.apcd = mapcd.group(6);
					tools.dealtime = tools.getTime(mapcd.group(5), mapcd.group(1));//给tools对象赋值
					
//					System.out.println("ljcfdrjfcfjnirdgi");
					
					planetable.put(mapcd.group(2), tools);//将值放到hashtable中
				}
				
				StringBuffer codeBuffer = new StringBuffer();//定义缓冲区放柜台号
				
				if(mtime.find()) {//如果能够找到和正则表达式二相匹配的行
					String counterline = mtime.group();
					Pattern pforcode = Pattern.compile("code=(\\w{1}\\d{2})");
					Matcher mforcode = pforcode.matcher(counterline);
					while(mforcode.find()) {
						codeBuffer.append(mforcode.group(1) + " ");
					}
					if(planetable.containsKey(mtime.group(1))) {//搜索hashtable中是否包含该航班信息
						tools = planetable.get(mtime.group(1));
						String airString = airport.getProperty(tools.apcd);
						System.out.println(tools.ffid + "        " + airString + "         " + tools.dealtime + "          " + codeBuffer.toString()
						+ "           " + tools.psString);//输出结果
					}//else {
//						System.out.println("null        null         null          " + codeBuffer.toString() + "           null");
//					}
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
