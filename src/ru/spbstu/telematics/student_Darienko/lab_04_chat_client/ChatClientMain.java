package ru.spbstu.telematics.student_Darienko.lab_04_chat_client;

public class ChatClientMain
{
	public static void main(String args[])
	{
		java.awt.EventQueue.invokeLater(new Runnable() {public void run() {
		                new ChatClientGUI().setVisible(true);
		            }
		        });
		    }		

}
