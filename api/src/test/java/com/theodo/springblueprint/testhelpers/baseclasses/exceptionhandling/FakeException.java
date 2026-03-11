package com.theodo.springblueprint.testhelpers.baseclasses.exceptionhandling;

class FakeException extends RuntimeException {

    public FakeException() {
        super();
    }

    public FakeException(String message) {
        super(message);
    }
}
