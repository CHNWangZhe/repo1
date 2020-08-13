import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Experiment3 extends JFrame {

	private JPanel contentPane;
	private JTable table;
	private JProgressBar progressBar;
	private JProgressBar progressBar1;// 全局变量用于控制进度条
	private ReceiveAndProcessData receiveAndProcessData;
	private DefaultTableModel tModel;// 表格的数据部分
	private int count;// 用于计算已经读取多少行数据，并以此来控制进度条

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Experiment3 frame = new Experiment3();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
//缩放图片函数，用以控制图片的显示大小
	public ImageIcon change(ImageIcon image, double i) {//   i 为放缩的倍数
		int width = (int) (image.getIconWidth() * i);
		int height = (int) (image.getIconHeight() * i);
		Image img;
		if (width != 0 && height != 0) {
			img = image.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);// 第三个值可以去查api是图片转化的方式
			ImageIcon image2 = new ImageIcon(img);
			return image2;
		}
		return image;
	}

	public Experiment3() {
		String[] title = { "航空公司图标", "航班号", "目的站", "办票时间", "柜台号", "备注" };// 表格头
		tModel = new DefaultTableModel(title, 0) {// 控制字符串和第一列图片对象的分别输出
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) {
					return ImageIcon.class;
				} else {
					return String.class;
				}
			}
		};

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		JButton btnStart = new JButton("开始");
		btnStart.setFont(new Font("幼圆", Font.PLAIN, 20));
		btnStart.setBackground(Color.WHITE);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tModel.setRowCount(0);
				receiveAndProcessData = new ReceiveAndProcessData();
				receiveAndProcessData.execute();
				btnStart.setEnabled(false);
			}
		});

		progressBar1 = new JProgressBar();
		progressBar1.setStringPainted(true);
		progressBar1.setPreferredSize(new Dimension(200, 20));
		panel.add(progressBar1);
		progressBar1.setMaximum(18018);// 设置进度条的最小值和最大值
		progressBar1.setMinimum(0);
		panel.add(btnStart);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut);

		JButton btnStop = new JButton("停止");
		btnStop.setBackground(Color.WHITE);
		btnStop.setFont(new Font("幼圆", Font.PLAIN, 20));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				receiveAndProcessData.cancel(false);
				btnStop.setEnabled(false);
			}
		});
		panel.add(btnStop);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		table = new JTable(tModel);
		table.setForeground(new Color(255, 255, 255));
		table.setFont(new Font("宋体", Font.BOLD, 17));
		table.getTableHeader().setFont(new Font("宋体", Font.PLAIN, 19));
		scrollPane.setViewportView(table);

		Dimension size = table.getTableHeader().getPreferredSize();
		size.height = 33;// 设置新的表头高度32
		table.getTableHeader().setPreferredSize(size);
		table.setBackground(new Color(0, 102, 153));// 设置表格背景颜色

		table.getTableHeader().setBackground(new Color(0, 51, 102));
		table.getTableHeader().setForeground(new Color(255, 255, 255));
		table.setRowHeight(55);// 设置行高

		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(tModel);
		table.setRowSorter(sorter);// 完成表格排序功能

		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				setBackground(new Color(0, 102, 153));
				if ("停止办票".equals(value) && column == 5) {// 通过备注的字符串值来设置备注的字体颜色
					setForeground(new Color(255, 51, 0));
				} else {
					if ("正在办票".equals(value) && column == 5) {
						setForeground(new Color(0, 255, 0));
					} else
						setForeground(new Color(255, 255, 255));
				}
				setHorizontalAlignment(SwingConstants.CENTER);// 表格字体均设置为居中
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};

		for (int i = 1; i < title.length; i++) {
			table.getColumn(title[i]).setCellRenderer(cellRenderer);
		} // 遍历表格设置

		table.setRowHeight(55);
	}

	class ReceiveAndProcessData extends SwingWorker<String, Object[]> {
		@Override
		protected String doInBackground() throws Exception {// 当前线程只完成数据分析
			// TODO Auto-generated method stub
			try ( Socket socket = new Socket("192.168.174.1", 6666);// 打开服务器端口
					Scanner scanner = new Scanner(socket.getInputStream())) {
				count = 0;
				Tools tool = new Tools();
				Properties airport = new Properties();
				airport.load(new FileReader("airport.txt"));// 将机场中文名称和三字码相匹配

				String machapcd = "ddtm=(\\d{14}).*AIRL\\Wflid=(\\d{7}).*ffid=(\\w{2})\\W(\\d{3,4}).*-D.*fptt=(\\d{14}).*arno=2, apcd=(\\w{3,4})";
				// 正则表达式一，用以找到航班号，现在时间，航班ID，起飞时间和目的地
				String machtime = ".*CKLS\\Wflid=(\\d{7}).*ffid=(\\w{2})\\W(\\d{3,4}).*code=(\\w{1}\\d{2})";
				// 正则表达式二，用以找到柜台号
				Pattern papcd = Pattern.compile(machapcd);
				Pattern ptime = Pattern.compile(machtime);
				String line = null;
				while (!(line = scanner.nextLine()).equals("no data!")) {
					if (isCancelled()) {
						progressBar.setString("数据更新中止！");
						return null;
					}
					count += 1;
					Matcher mapcd = papcd.matcher(line);
					Matcher mtime = ptime.matcher(line);
					Object[] lineObjects = new Object[6];// 定义数组来放置中间值
					lineObjects[0] = null;
					lineObjects[1] = null;
					lineObjects[2] = null;
					lineObjects[3] = null;
					lineObjects[4] = null;
					lineObjects[5] = null;

					if (mapcd.find()) {
						lineObjects[1] = (mapcd.group(3) + mapcd.group(4));
						lineObjects[2] = (airport.getProperty(mapcd.group(6)));
						lineObjects[3] = tool.getTime(mapcd.group(5), mapcd.group(1));
						lineObjects[5] = tool.psString;
						publish(lineObjects);// 若字符串匹配，则将值送往process中完成更新
					}
					if (mtime.find()) {
						lineObjects[1] = mtime.group(2) + mtime.group(3);
						String counterline = mtime.group();
						Pattern pforcode = Pattern.compile("code=(\\w{1}\\d{2})");
						Matcher mforcode = pforcode.matcher(counterline);
						lineObjects[4] = "";
						while (mforcode.find()) {
							lineObjects[4] += mforcode.group(1) + " ";
						}
						publish(lineObjects);// 若字符串匹配，则将值送往process中完成更新
					}
				}
			}
			System.out.println(count);
			return null;
		}

		@Override
		protected void process(List<Object[]> rows) {// 注意！仅在该线程中完成对表格数据的更改，避免多线程造成的混乱
			try {
				// TODO Auto-generated method stub
				for (Object[] row : rows) {
					if (row[4] != null) {
						int flag = 0;
						for (int i = 1; i < tModel.getRowCount(); i++) {
							if (row[1].equals(tModel.getValueAt(i, 1))) {
								tModel.setValueAt(row[4], i, 4);
								flag = 1;
								break;
							}
						}
						if (flag == 0) {
							String name = ((String) row[1]).substring(0, 2);
							ImageIcon icon = new ImageIcon(// 根据航班号匹配相应的航空公司图标
									"C:\\Users\\15093\\eclipse-workspace\\Check_in\\image\\" + name + ".jpg");
							if (icon.getImage() != null) {
								ImageIcon image = change(icon, 0.2);
								row[0] = image;
							}
							tModel.addRow(row);// 向表格添加新数据行
						}
						progressBar1.setValue(count);// 传输进度条数据
					} else {
						int flag = 0;
						for (int i = 1; i < tModel.getRowCount(); i++) {
							if (row[1].equals(tModel.getValueAt(i, 1))) {
								tModel.setValueAt(row[2], i, 2);
								tModel.setValueAt(row[3], i, 3);
								tModel.setValueAt(row[5], i, 5);
								flag = 1;
								break;
							}
						}
						if (flag == 0) {
							String name = ((String) row[1]).substring(0, 2);
							ImageIcon icon = new ImageIcon(
									"C:\\Users\\15093\\eclipse-workspace\\Check_in\\image\\" + name + ".jpg");
							if (icon.getImage() != null) {
								ImageIcon image = change(icon, 0.2);
								row[0] = image;
							}
							tModel.addRow(row);
						}
						progressBar1.setValue(count);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
