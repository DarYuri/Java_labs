package ru.spbstu.telematics.student_Darienko.lab_04_chat_client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;


public class ChatClientCore implements Runnable
{
	private Selector selector_; // селектор для работы с каналом
	private int serverPort_ = 4321; // порт на сервере
	private SocketChannel clientSocketChannel_; // канал сокета
	
		/*Буффер и кодер/декодер для чтения/записи в канал*/
	private ByteBuffer readMsgBuffer_ = ByteBuffer.allocateDirect(1024);  
	private CharsetEncoder msgEncoder_ =  Charset.forName("UTF-8").newEncoder();
	private CharsetDecoder msgDecoder_ =  Charset.forName("UTF-8").newDecoder();
	
	private String nickName_;
	public Vector<String> messagesToSendList_ = new Vector<String>();
	public ReentrantLock messagesListLock_ = new ReentrantLock();
	public ChatClientGUI clientGUI_;
	public boolean isConnected_ = false;
	
	public ChatClientCore(ChatClientGUI gui)
		{clientGUI_=gui;}
	
	public boolean setUpConnection(String serverAddress)
	{
		boolean result = false;
		try 
		{	// пытаемся открыть сокет и соединиться с сервером
			selector_ = Selector.open();	
			clientSocketChannel_ = SocketChannel.open();
			try
				{result = clientSocketChannel_.connect(new InetSocketAddress(serverAddress, serverPort_));}
			catch (UnresolvedAddressException ex)
				{result = false;}
			clientSocketChannel_.configureBlocking(false);
			clientSocketChannel_.register(selector_, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		} 
		catch (ConnectException connectEx)
			{result = false;}
		catch (IOException e)
			{e.printStackTrace();}
		return result;
	}	
	
	public void disconnect()
	{
		isConnected_ = false;
		clientGUI_.setTitle("Java chat (disconnected)");
		try
			{clientSocketChannel_.close();}
		catch (IOException e)
			{e.printStackTrace();}
	}
	
	private String readMsg() throws CharacterCodingException
	{
		int count = -1;
	    String msg = new String();
		readMsgBuffer_.clear();
	    try
	    {
			while ((count = clientSocketChannel_.read(readMsgBuffer_)) > 0)
		    {	// читаем сообщение в буфер, декодируем  и добавляем к выходной строке 
		    	readMsgBuffer_.flip();
		    	msg+=msgDecoder_.decode(readMsgBuffer_).toString();
		        readMsgBuffer_.clear();
		    }
	    } catch (IOException ex)
    		{this.disconnect(); return null;}
	    if (count < 0)
	    {	// если соединение прервалось
	    	this.disconnect();
	    	return null;
	    }
	    return msg;
	}
	
	private void sendMsg(String msg) 
	{
	    try
			{clientSocketChannel_.write(msgEncoder_.encode(CharBuffer.wrap(msg)));}
	    catch (IOException e)
			{e.printStackTrace();}		
	}
	
	private boolean registrationAttempt(String nickName)
	{
		String result = null;
		sendMsg(nickName);
		while (result == null)
		{
			try
				{selector_.select();} 
			catch (IOException e1)
				{e1.printStackTrace();}
			Iterator keysIter = selector_.selectedKeys().iterator();
	
			while (keysIter.hasNext())
			{
				SelectionKey key = (SelectionKey) keysIter.next();
				keysIter.remove();
				if (!key.isValid())
					continue;
				if (key.isReadable())
				{
					try
					{
						result = readMsg();
					} catch (CharacterCodingException e)
						{e.printStackTrace();} 
					catch (IOException e)
						{e.printStackTrace();}
				}
			}
		}
		if (result.compareTo("ok") == 0)
			return true;
		return false;
	}
	
	public boolean registerInChat(String nickname)
	{
		if (!registrationAttempt(nickname))
			return false;
		nickName_ = nickname;
		return true;
	}
	

	@Override
	public void run()
	{
		String inMsg = null;
		while (true)
		{
			try
				{selector_.select();} 
			catch (IOException e1)
				{e1.printStackTrace();}
			Iterator keysIter = selector_.selectedKeys().iterator();
		
			while (keysIter.hasNext())
			{
				SelectionKey key = (SelectionKey) keysIter.next();
				keysIter.remove();
				if (!key.isValid())
					continue;
				if (key.isReadable())
				{
					try
					{
						inMsg = readMsg();
						clientGUI_.chatTextArea_.insert(inMsg+"\n\n", 0);
					} catch (CharacterCodingException e)
						{e.printStackTrace();} 
				}
				try
				{
					if (key.isWritable())
					{
						messagesListLock_.lock();
						try
						{
							for (String outMsg : messagesToSendList_)
								{sendMsg(outMsg);}
							messagesToSendList_.clear();
						}
						finally {messagesListLock_.unlock();}
						
					}
				}
				catch (CancelledKeyException ex)
				{
					disconnect();
					clientGUI_.chatTextArea_.setText("Connection to server has been lost! Try to reconnect, please!");
					break;
				}
			}
		}
			
	}
}
