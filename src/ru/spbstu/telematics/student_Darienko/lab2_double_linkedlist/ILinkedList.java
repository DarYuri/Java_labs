package ru.spbstu.telematics.student_Darienko.lab2_double_linkedlist;

/**
 * Двусвязный список.
 */
public interface ILinkedList <T> {
    /**
     * Добавить элемент в голову списка
     */
    void addFirst(T o);

    /**
     * Добавить элемент в хвост списка
     * @param o
     */
    void addLast(T o);

    /**
     * Взять первый элемент. Но не удалять из списка!
     * @return
     */
    T getFirst();

    /**
     * Взять последний элемент. Но не удалять!
     * @return
     */
    T getLast();

    /**
     * Вернуть первый элемент и удалить его из списка.
     * @return
     */
    T removeFirst();

    /**
     * Вернуть последний элемент и удалить его из списка.
     * @return
     */
    T removeLast();

}

