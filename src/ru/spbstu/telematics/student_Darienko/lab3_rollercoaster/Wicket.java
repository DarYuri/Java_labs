package ru.spbstu.telematics.student_Darienko.lab3_rollercoaster;

import java.util.Random;

// класс описывает турникет
public class Wicket implements Runnable
{
	private RCController controller_; // контроллер	
	private Integer wicketNumber_; // номер турникета
	
	public Wicket(RCController controller, Integer num)
		{controller_ = controller; setWicketNumber_(num);}
	
	@Override
	public void run()
	{	// генерирует события прихода посетителя (регистрации)
		while (true)
		{
			try
				{Thread.sleep(1000 + new Random().nextInt(1000));}
			catch (InterruptedException e)
				{e.printStackTrace();}
			controller_.registerNewVisitor(wicketNumber_);
		}
	}

	public Integer getWicketNumber_()
		{return wicketNumber_;}

	public void setWicketNumber_(Integer wicketNumber_)
		{this.wicketNumber_ = wicketNumber_;}
}

