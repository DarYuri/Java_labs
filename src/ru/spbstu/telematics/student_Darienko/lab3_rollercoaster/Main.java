package ru.spbstu.telematics.student_Darienko.lab3_rollercoaster;

import java.util.Scanner;

public class Main
{

	public static void main(String[] args)
	{
		RCTram tram = new RCTram();
		RCController controller = new RCController(tram);

		new Thread(controller).start();
		new Thread(tram).start();
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter number of wickets: ");
		Integer numOfWickets=scanner.nextInt();
		for (int i=1; i <= numOfWickets; i++)
		{
			new Thread(new Wicket(controller, i)).start();
		}
	}
}