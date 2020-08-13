import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Experiment2 {
	public static void main(String[] args) {
		try {
			LinkedBlockingQueue<String> entries = new LinkedBlockingQueue<String>();//使用阻塞队列，实现自动等待，自动通知
			
			Properties airport = new Properties();
			airport.load(new FileReader("airport.txt"));//将机场中文名称和三字码相匹配
			
			Tools tools = new Tools();
			Hashtable<String, Tools> planetable = new Hashtable<>();
			System.out.println("航班号               目的站               办票时间                    柜台号                                                                                                          备注");
			//控制输出格式头
			String machapcd = "ddtm=(\\d{14}).*\\Wflid=(\\d{7}).*ffid=(\\w{2})\\W(\\d{3,4}).*fptt=(\\d{14}).*arno=2, apcd=(\\w{3,4})";
			//正则表达式一，用以找到航班号，现在时间，航班ID，起飞时间和目的地
			String machtime = ".*CKLS\\Wflid=(\\d{7}).*code=(\\w{1}\\d{2})";
			//正则表达式二，用以找到柜台号
			
			Pattern papcd = Pattern.compile(machapcd);
			Pattern ptime = Pattern.compile(machtime);
			String lineString = null;

			// 生产者线程
			new Thread(()->{// 匿名函数λ表达式
				Path file = Paths.get("FDSdata.txt");
					try {
						Files.lines(file).forEach(line->{//判断文件是否到末尾
							
							Matcher mapcd = papcd.matcher(line);
								try {
									entries.put(line);//读取文件每一行，实现自动堵塞
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}		
								
								if (mapcd.find()) {//如果能够找到和正则表达式一相匹配的行
									tools.ffid = mapcd.group(3) + mapcd.group(4);
									tools.apcd = mapcd.group(6);
									tools.dealtime = tools.getTime(mapcd.group(5), mapcd.group(1));//给tools对象赋值
						            
									planetable.put(mapcd.group(2), tools);//将值放到hashtable中
								}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						try {
							entries.put("no data");//约定字符串作为线程结束标志
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			}).start();
					
				

			// 消费者线程
			new Thread(() -> {// 匿名函数λ表达式
				
					while (true) {
						String line = null;
						try {
							line = entries.take();//从阻塞队列中读取数据
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}// put和take是一对
						Matcher mapcd = papcd.matcher(line);
						Matcher mtime = ptime.matcher(line);
						
						if (line.equals("no data"))//若数据读取完毕，则退出
							break;
						
						StringBuffer codeBuffer = new StringBuffer();//定义缓冲区放柜台号
						
						if(mtime.find()) {//如果能够找到和正则表达式二相匹配的行
							String counterline = mtime.group();
							Pattern pforcode = Pattern.compile("code=(\\w{1}\\d{2})");
							Matcher mforcode = pforcode.matcher(counterline);
							while(mforcode.find()) {
								codeBuffer.append(mforcode.group(1) + " ");
							}
							if(planetable.containsKey(mtime.group(1))) {//搜索hashtable中是否包含该航班信息
								Tools tool = new Tools();
								tool = planetable.get(mtime.group(1));
								String airString = airport.getProperty(tool.apcd);
								System.out.println(tool.ffid + "        " + airString + "         " + tool.dealtime + "          " + codeBuffer.toString()
								+ "           " + tool.psString);//输出结果
							}
						}
						
					}
			}).start();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}