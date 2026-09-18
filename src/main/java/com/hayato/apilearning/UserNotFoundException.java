package com.hayato.apilearning;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long id) {
        super("ユーザーが見つかりません。id=" + id);
    }
}