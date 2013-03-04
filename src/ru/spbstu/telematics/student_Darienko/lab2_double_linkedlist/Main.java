package ru.spbstu.telematics.student_Darienko.lab2_double_linkedlist;

public class Main
{

	static <T> void printList (LinkedList<T> list)
	{
		System.out.print("[list-head] -> ");
		for (T elem : list) {
			System.out.print(elem.toString() + " -> ");
		}
		System.out.println("[list-tail]");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		System.out.println("Adding to tail 10 elements...");
		for (int i=0; i < 10; i++)
			list.addLast(i); // добавляем к хвосту числа от 0 до 9
		printList(list);
		
		System.out.println("\nRemoving from tail 5 elements...");
		for (int i=0; i < 5; i++)
			System.out.println("Removing element "+list.removeLast().toString()); // удаляем 5 последних
		printList(list);
		
		System.out.println("\nAdding to head and tail...");
		list.addLast(100);   // добавляем к хвосту число 100 
		list.addFirst(-100); // добавляем в голове число -100
		printList(list);
		
		System.out.println("\nRemoving from head 5 elements...");
		for (int i=0; i < 5; i++)
			System.out.println("Removing element "+list.removeFirst().toString());	// удаляем 5 с головы
		printList(list);
		
		System.out.println("\nTrying to remove more then list contains...");
		for (int i=0; i < 100; i++)
		{
			Integer data = list.removeFirst();
			if (data != null)
				System.out.println("Removing element "+data.toString());	// пробуем удалить много с головы
			else
				System.out.println("No element to remove!");
		}
		printList(list);
	}

}
