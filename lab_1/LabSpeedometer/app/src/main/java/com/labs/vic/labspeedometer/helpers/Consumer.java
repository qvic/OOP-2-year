package com.labs.vic.labspeedometer.helpers;

public interface Consumer<T> {
    void accept(T t);
}
