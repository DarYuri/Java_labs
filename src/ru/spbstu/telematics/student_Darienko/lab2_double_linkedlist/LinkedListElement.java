package ru.spbstu.telematics.student_Darienko.lab2_double_linkedlist;

public class LinkedListElement <T>
{
	private LinkedListElement <T> nextElement_ = null;
	private LinkedListElement <T> prevElement_ = null;
	private T data_;
	
	public LinkedListElement(T data, LinkedListElement<T> prev, LinkedListElement<T> next)
	{	// констурктор с параметрами (данные, предыдущий элемент, следующий элемент)
		setNextElement_(next);
		setPrevElement_(prev);
		setData_(data);
	}

	/*Setters and getters*/
	public T getData_()
		{return data_;}

	public void setData_(T data_)
		{this.data_ = data_;}

	public LinkedListElement <T> getNextElement_()
		{return nextElement_;}

	public void setNextElement_(LinkedListElement <T> nextElement_)
		{this.nextElement_ = nextElement_;}

	public LinkedListElement <T> getPrevElement_()
		{return prevElement_;}

	public void setPrevElement_(LinkedListElement <T> prevElement_)
		{this.prevElement_ = prevElement_;}
}
