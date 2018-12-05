package com.lobotino.collector;

/**
 * Created by Олег on 30.11.2018.
 */

public class Element {

    public int id;

    public String good;

    public double price;

    public String category_name;

    // Конструктор
    public Element(int id, String good, double price, String category_name) {
        this.id = id;
        this.good = good;
        this.price = price;
        this.category_name = category_name;
    }

    // Выводим информацию по продукту
    @Override
    public String toString() {
        return String.format("ID: %s | Товар: %s | Цена: %s | Категория: %s",
                this.id, this.good, this.price, this.category_name);
    }
}
