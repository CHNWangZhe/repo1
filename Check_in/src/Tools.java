import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Tools {
	
//	public static String flid = null;//航班号
	public static String ffid = null;//航班号
	public static String apcd = null;//目的站
	public static String dealtime = null;//办票时间
	public static String psString = null;//备注
	public static StringBuffer codeString = null;//柜台
	
		public static String getTime(String fptt, String ddtm) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");//日期匹配格式定义
			
			Date fptime = new Date();
			Date ddtime = new Date();//创建date类对象
			
			try {
				fptime = format.parse(fptt);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long beginTime = 90 * 60 * 1000;// -90
			long endTime = 30 * 60 * 1000;// -30
			Date beginDate = new Date(fptime.getTime() - beginTime);// 得到开始检票时间
			Date endDate = new Date(fptime.getTime() - endTime);// 得到结束检票时间

			SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm");//定义日期输出格式
			
			String endString = outFormat.format(beginDate) +"-" + outFormat.format(endDate);//定义办票时间存储格式
			
			try {
				ddtime = format.parse(ddtm);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int result = fptime.compareTo(ddtime);
			if(result < 0) {
				psString = "停止办票";
			}else {
				result = beginDate.compareTo(ddtime);
				if(result > 0) {
					psString = "暂未办票";
				}else {
					psString = "正在办票";
				}
			}//根据现在时间和起飞时间确定值机状态
			

			return endString;
			
		}
		

		public static void main(String[] args) {
//			String ffpt = "20191009205000";
//			String enString = getTime(ffpt);
//			System.out.println(enString);
		}
}
