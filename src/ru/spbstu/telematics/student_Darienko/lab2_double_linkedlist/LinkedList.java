package ru.spbstu.telematics.student_Darienko.lab2_double_linkedlist;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedList <T> implements ILinkedList <T>, Iterable<T>
{
	private LinkedListElement<T> firstElement_ = null; // головной элемент
	private LinkedListElement<T> lastElement_ = null;  // хвостовой элемент
	private Integer elementsCount_ = 0;	// количество элементов в списке
	
		/*КЛАСС ИТЕРАТОРА*/
	public class LinkedListIterator implements Iterator <T>
	{
		LinkedListElement<T> currentElement_ = null;
		LinkedListElement<T> nextElement_ = null;
		
		public LinkedListIterator()
			{nextElement_=LinkedList.this.firstElement_;}
		
		@Override
		public boolean hasNext()
		{	// проверка на существование следующего элемента
			if (nextElement_ != null)
				return true;
			return false;
		}

		@Override
		public T next() throws NoSuchElementException
		{	// возвращает элемент и перепрыгивает на следующий
			if (nextElement_ == null)
				throw new NoSuchElementException();
			currentElement_ = nextElement_;
			nextElement_ = nextElement_.getNextElement_();
			return currentElement_.getData_();
		}

		@Override	// не реализован (кидает исключение)
		public void remove()  throws NoSuchMethodError 
			{throw new NoSuchMethodError();}		
	}
	
	@Override
	public Iterator<T> iterator()
	{	// получение итератора для списка
		LinkedListIterator iter = new LinkedListIterator();
		return iter;
	}

	@Override
	public void addFirst(T o)
	{	// добавление к голове
		if (elementsCount_ == 0)
		{	// если элементов не было 
			LinkedListElement<T> newElem = new LinkedListElement<T>(o, null, null);
			firstElement_ = newElem;
			lastElement_ = newElem;
		}
		else
		{	// если элементвы были
			LinkedListElement<T> newElem = new LinkedListElement<T>(o, null, firstElement_);
			firstElement_.setPrevElement_(newElem);
			firstElement_ = newElem;
		}
		elementsCount_++;
	}

	@Override
	public void addLast(T o)
	{	// добавление к хвосту
		if (elementsCount_ == 0)
		{	// если элементов не было
			LinkedListElement<T> newElem = new LinkedListElement<T>(o, null, null);
			firstElement_ = newElem;
			lastElement_ = newElem;
		}
		else
		{	// если элементы были
			LinkedListElement<T> newElem = new LinkedListElement<T>(o, lastElement_, null);
			lastElement_.setNextElement_(newElem);
			lastElement_ = newElem;
		}		
		elementsCount_++;
	}

	@Override
	public T getFirst()
	{	// получение элемента с головы списка (если элементов нет - возвращает  null)
		if (elementsCount_ != 0)
			return firstElement_.getData_();
		return null;
	}

	@Override
	public T getLast()
	{	// получение элемента с хвоста списка (если элементов нет - возвращает  null)
		if (elementsCount_ != 0)
			return lastElement_.getData_();
		return null;
	}

	@Override
	public T removeFirst()
	{	// удаление элемента с головы списка (если элементов нет - возвращает  null)
		if (elementsCount_ != 0)
		{	// если есть элементы
			T data = firstElement_.getData_();
			if (elementsCount_ == 1)
			{	// если элемент только один
				firstElement_ = null;
				lastElement_ = null;
			}
			else
			{	// если элементов больше одного
				firstElement_.getNextElement_().setPrevElement_(null);
				firstElement_=firstElement_.getNextElement_();
			}
			elementsCount_--;
			return data;
		}
		return null;
	}

	@Override
	public T removeLast()
	{	// удаление элемента с хвоста списка (если элементов нет - возвращает  null)
		if (elementsCount_ != 0)
		{
			T data = lastElement_.getData_();
			if (elementsCount_ == 1)
			{	// если элемент только один
				firstElement_ = null;
				lastElement_ = null;
			}
			else
			{	// если элементов больше одного
				lastElement_.getPrevElement_().setNextElement_(null);
				lastElement_=lastElement_.getPrevElement_();
			}
			elementsCount_--;
			return data;
		}
		return null;
	}
}