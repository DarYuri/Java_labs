package ru.spbstu.telematics.student_Darienko.lab3_rollercoaster;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* класс описывает контроллер */
public class RCController implements Runnable
{
	private Integer visitorsOnPlatformCount_ = 0; // количество ожидающих посетителей на платформе
	private ReentrantLock visitorsCountLock_;	  // блокировка на счетчик посетителей
	private Condition controllerActionNeeded_;	  // условие на работу контроллера
	private final Integer TRAM_CAPACITY = 10;	  // вместительность тележки
	public final int TRAM_RIDE_DURATION = 4000;	  // длительность поездки тележки
	private Integer visitorPackCounter_ = 0;	  // количество групп посетителей из TRAM_CAPACITY штук, ожидающих на платформе 
	private RCTram rcTram_;
	private boolean rcTramIsOnRide_ = false;
	
	public RCController(RCTram tram)
	{
		visitorsCountLock_ = new ReentrantLock();
		controllerActionNeeded_ = visitorsCountLock_.newCondition();
		rcTram_ = tram;
		tram.controller_ = this;
	}
	
	public void registerNewVisitor(Integer wicketNum)
	{	// регистрация посетителя
		visitorsCountLock_.lock();
		try
		{
			visitorsOnPlatformCount_++;
			controllerActionNeeded_.signalAll(); // кидаем сигнал контроллеру
			//System.out.println(Thread.currentThread().toString());
			System.out.println("[+] New visitor registered on wicket #" +wicketNum.toString()+"! Waiting visitors number: "+visitorsOnPlatformCount_.toString());
		}
		finally
			{visitorsCountLock_.unlock();}
	}
	
	public void tramLoadingProcess()
	{	// погрузка посетителей на тележку
		visitorsCountLock_.lock();
		try
		{
			visitorsOnPlatformCount_-=TRAM_CAPACITY;
			System.out.println("[-] Tram is loading with " +TRAM_CAPACITY.toString() +" visitors... Waiting visitors number: "+visitorsOnPlatformCount_.toString());
		}
		finally
			{visitorsCountLock_.unlock();}
	}
	
	public synchronized boolean invertRcTramStatus(boolean invert)
		{rcTramIsOnRide_=invert^rcTramIsOnRide_; return rcTramIsOnRide_;}
	
	public synchronized Integer visitorPackCountChange(int amount) //  метод изменения значений счетчика групп посетителей
		{visitorPackCounter_+=amount; return visitorPackCounter_;}
	
	@Override
	public void run()
	{
		boolean newPackReady;
		while (true)
		{
			newPackReady = false;
			visitorsCountLock_.lock();
			try
			{
				try
					{controllerActionNeeded_.await();} // ожидаем прихода посетителя
				catch (InterruptedException e)
					{e.printStackTrace();}
				if (visitorsOnPlatformCount_/TRAM_CAPACITY > visitorPackCountChange(0))
				{	// если число посетителей на платформе достаточно, для поездки
					newPackReady = true; // выставляем флаг готовности группы
					visitorPackCountChange(1); 
				}
			}
			finally
				{visitorsCountLock_.unlock();}
			
			if ((newPackReady)&&(!invertRcTramStatus(false)))	// если группа готова - сигналим тележке
				rcTram_.tramLoadingSignal();
		}
	}
}
