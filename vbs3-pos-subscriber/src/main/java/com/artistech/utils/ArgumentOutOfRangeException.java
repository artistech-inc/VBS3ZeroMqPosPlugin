/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.utils;

/**
 *
 * @author matta
 */
public class ArgumentOutOfRangeException extends Exception {

    private final String _argument;

    public ArgumentOutOfRangeException(String argument, String message) {
        super(message);
        _argument = argument;
    }

    public String getArgument() {
        return _argument;
    }
}
