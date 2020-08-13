import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class Sever {
	private static Selector selector;
	private static ByteBuffer sourcefile;

	public static void main(String[] args) {
		try {
			ServerSocketChannel serverChannel = ServerSocketChannel.open();// 调用静态工厂方法open，默认工作在阻塞模式下
			selector = Selector.open();// 建立selector对象来监视就绪事件

			serverChannel.configureBlocking(false);// 让serverChannel工作在非阻塞模式下
			serverChannel.bind(new InetSocketAddress(6666));// 打开端口

			serverChannel.register(selector, SelectionKey.OP_ACCEPT);// 进行注册
			System.out.println("服务器启动成功");

//			Path dataPath = FileSystems.getDefault().getPath("FDSdata.txt");
			Path dataPath = Paths.get("FDSdata.txt");
			byte[] data = Files.readAllBytes(dataPath);// 把文件一口吞进一个字节数组中
			sourcefile = ByteBuffer.wrap(data);// 把这个字节数组wrap成一个ByteBuffer

			while (true) {
				if (selector.select() > 0) {// 如果值大于零，就表示有就绪事件发生。返回值为发生就绪事件的个数
					// 获取相关事件已发生的SelectionKey集合
					Set<SelectionKey> readKeys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = readKeys.iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();// 避免下一次仍旧使用该key
						// 有新客户端来连服务器
						if (key.isAcceptable()) {
							echoAccept(key);
						}
						// 可以向某个客户端写数据了
						if (key.isWritable()) {
							echoWrite(key);
						}
					}
				}
			}
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 处理连接就绪函数
	private static void echoAccept(SelectionKey key) {
		try {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel clientChannel = server.accept();
			System.out.println("接收到新的客户端连接" + clientChannel.getRemoteAddress());// 输出端口号
			clientChannel.configureBlocking(false);// 修改工作模式，让servercChannel工作在非阻塞模式下
			SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_WRITE);// 注册写就绪事件
			ByteBuffer fileBuffer = sourcefile.duplicate();// Bytebuffer复用，避免空间浪费
			clientKey.attach(fileBuffer);// Buffer是一个进行数据读写的工具
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 处理写就绪函数
	private static void echoWrite(SelectionKey key) {
		try {
			SocketChannel client = (SocketChannel) key.channel();
			ByteBuffer outBuffer = (ByteBuffer) key.attachment();
			if (outBuffer.hasRemaining()) {// 能写多少写多少
				client.write(outBuffer);
				try {
					Thread.sleep(10);// 短暂睡眠
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if (client != null) {
					client.close();// 写完毕时，关闭与客户端的连接
				}
			}
		} catch (IOException e) {
			key.cancel();// 注意修改异常，当客户端关闭连接时，就关闭相应的key
			try {
				key.channel().close();
			} catch (IOException cex) {
				// TODO: handle exception
			}
		}
	}
}
