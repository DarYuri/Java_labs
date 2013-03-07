package ru.spbstu.telematics.student_Darienko.lab3_rollercoaster;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// класс описывает тележку
public class RCTram implements Runnable
{
	private ReentrantLock tramLock_;
	private Condition tramLoadingCondition_;
	public RCController controller_;
	
	public RCTram()
	{
		tramLock_ = new ReentrantLock();
		tramLoadingCondition_ = tramLock_.newCondition();
	}
	
	public void tramLoadingSignal()
	{	// метод генерации сигнала к началу погрузки
		tramLock_.lock();
		try
			{tramLoadingCondition_.signalAll();}
		finally
			{tramLock_.unlock();}
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			tramLock_.lock();
			try
			{
				try
					{tramLoadingCondition_.await();} // пока нет сигнала - ждем
				catch (InterruptedException e)
					{e.printStackTrace();}
				while (controller_.visitorPackCountChange(0) > 0)
				{	// пока есть группы ожидающих посетителей для поездки - загружаем их и катаем
					controller_.visitorPackCountChange(-1);
					controller_.tramLoadingProcess(); // загружаемся 
					controller_.invertRcTramStatus(true);
					System.out.println("[>] Tram is outgoing...");
					try	// катаемся
						{Thread.sleep(controller_.TRAM_RIDE_DURATION);} 
					catch (InterruptedException e)
						{e.printStackTrace();}
					System.out.println("[<] Tram arrived!");
					controller_.invertRcTramStatus(true);
				}
			}
			finally
				{tramLock_.unlock();}
		}
	}

}
