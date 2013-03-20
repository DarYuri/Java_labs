package ru.spbstu.telematics.student_Darienko.lab_04_chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatServer
{
		/*--СЕТЬ И ВЗАИМОДЕЙСТВИЕ--*/
	private static final boolean READ_FROM_SOCKETS = true;
	private static final boolean WRITE_TO_SOCKETS = false;
	private Selector selector_; // селектор для работы с каналами
	private boolean currentOperation_ = READ_FROM_SOCKETS;
	
		/*Буффер и кодер/декодер для чтения/записи в каналы*/
	private ByteBuffer readMsgBuffer_ = ByteBuffer.allocateDirect(1024);  
    private CharsetEncoder msgEncoder_ =  Charset.forName("UTF-8").newEncoder();
    private CharsetDecoder msgDecoder_ =  Charset.forName("UTF-8").newDecoder();
	
    	/*--КЛИЕНТЫ--*/
    private Map<SocketChannel, ChatClientData> clientsDataMap_; // данные о клиентах (пары [канал;данные]) 
    private ArrayList<String> messagesToSendList_; // список сообщений для рассылки
    
    // Класс ChatClientData инкапсулирует данные о клиенте 
    private class ChatClientData 
    {	
    	public String nickName_ = null; // ник 
    	public boolean clientRegStatus_ = false; // состояние реги
    	public boolean clientNotificationStatus_ = false;
    	public boolean isRegistered()
    		{return clientRegStatus_;}
    	public boolean hasBeenNotified()
    		{return clientNotificationStatus_;}
    }
    
    /*МЕТОДЫ*/
    public ChatServer()
	{
		try
			{initServer();}
		catch (IOException e)
			{e.printStackTrace();}

		while (true)
		{
			try
				{serverLoopCycle();}
			catch (IOException e)
				{e.printStackTrace();}
			currentOperation_=!currentOperation_;
		}
	}
	
	private void initServer() throws IOException
	{	// инициализация сервера
		clientsDataMap_ = new HashMap<SocketChannel, ChatClientData>();
		messagesToSendList_ = new ArrayList<String>();
		selector_ = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(4321));
		serverChannel.register(selector_, SelectionKey.OP_ACCEPT);
	}
	
	private void acceptConnection(SelectionKey key) throws IOException
	{	// прием нового соединения
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        clientsDataMap_.put(channel, new ChatClientData());
        channel.configureBlocking(false);
        channel.register(selector_, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	
	private String readMsgFromChannel(SelectionKey key)
	{	// чтение данных из канала
		SocketChannel socketChannel = (SocketChannel) key.channel();
		int count = -1;
	    String msg = new String();
		readMsgBuffer_.clear();
	    try
	    {
		    while ((count = socketChannel.read(readMsgBuffer_)) > 0)
		    {	// читаем сообщение в буфер, декодируем  и добавляем к выходной строке 
		    	readMsgBuffer_.flip();
		    	msg+=msgDecoder_.decode(readMsgBuffer_).toString();
		        readMsgBuffer_.clear();
		    }
	    } catch (IOException ex)
	    	{return null;}
	    if (count < 0)
	       	return null; // если клиент закрыл сокет
	    return msg;
	}
	
	private void sendMsgToChannel(String msg, SelectionKey key) throws IOException
	{	// отправка данных в канал
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.write(msgEncoder_.encode(CharBuffer.wrap(msg)));
	}
	
	private void clientUnregister(SocketChannel socketChannel) throws IOException
	{
    	socketChannel.close();
    	if (clientsDataMap_.get(socketChannel).isRegistered())
    	{
    		String nick = clientsDataMap_.get(socketChannel).nickName_;
    		messagesToSendList_.add(new Date().toString()+"\n<Server> "+nick + " has left chat!");
    	}
    	clientsDataMap_.remove(socketChannel);
	}
	
	private boolean nickRegistrationCheck(String nick)
	{
		Iterator iter = clientsDataMap_.values().iterator();
		ChatClientData existClientData;
		while (iter.hasNext())
		{
			existClientData=(ChatClientData) iter.next();
			if (existClientData.isRegistered())
			{
				if (existClientData.nickName_.compareTo(nick) == 0)
					return false;
			}
		}
		return true;
	}
	
	private boolean recieveMessageFromClient(SelectionKey key) throws IOException
	{
		String msg;
		msg = readMsgFromChannel(key);
		if (msg == null)
			return false; // если клиент отвалился
		if (!clientsDataMap_.get(key.channel()).isRegistered())
		{	//	// если клиент не зареген (пришло сообщение с ником для реги)
			if (!nickRegistrationCheck(msg))  // проверяем возможность реги ника
				sendMsgToChannel("fail", key); // если рега невозможна - отправляем сообщение 
			else
			{	// если рега такого ника возможна 
				clientsDataMap_.get(key.channel()).nickName_=msg; // заносим данные о клиенте
				clientsDataMap_.get(key.channel()).clientRegStatus_=true;
				// добавляем в список рассылки приветствие
				messagesToSendList_.add(new Date().toString()+"\n<Server> Welcome, "+msg+"!"); 
			}
		}
		else	//если пришло обычное сообщение - добавляем в список рассылки
			messagesToSendList_.add(new Date().toString()+"\n<"+clientsDataMap_.get(key.channel()).nickName_+"> "+msg);
		return true;
	}
	
	private void sendMessagesToClient(SelectionKey key) throws IOException
	{
		if (clientsDataMap_.get(key.channel()).isRegistered())
		{	// если клиент зареган
			if (!clientsDataMap_.get(key.channel()).hasBeenNotified())
			{	// если клиенту еще не отсылалось оповещение об успешной реге
				clientsDataMap_.get(key.channel()).clientNotificationStatus_=true;
				sendMsgToChannel("ok", key); // отсылаем оповещение
			}
			else
			{	// если клиент зареган и чатится - рассылаем сообщения
				for (int i = 0; i < messagesToSendList_.size(); i++)
					sendMsgToChannel(messagesToSendList_.get(i), key);
			}
		}			
	}
	
	private void serverLoopCycle() throws IOException
	{
		selector_.select();
		Iterator keysIter = selector_.selectedKeys().iterator();
		while (keysIter.hasNext())
		{	//	проходим по всем ключам 
			SelectionKey key = (SelectionKey) keysIter.next();
			keysIter.remove();
			if (!key.isValid())
				continue;
			if (key.isAcceptable()) // прием входящего соединения
				acceptConnection(key);
			if (currentOperation_ == READ_FROM_SOCKETS)
			{	// если текущая операция чтение из сокетов
				if (key.isReadable())
					if (!recieveMessageFromClient(key)) // если клиент отвалился - убираем его
						clientUnregister((SocketChannel)key.channel());
			}
			if (currentOperation_ == WRITE_TO_SOCKETS)
			{	// если текущая операция запись в сокеты
				if (key.isWritable()) // рассылаем сообщения клиенту
					sendMessagesToClient(key);
			}
		}
		if (currentOperation_ == WRITE_TO_SOCKETS)
			messagesToSendList_.clear();
	}
}
