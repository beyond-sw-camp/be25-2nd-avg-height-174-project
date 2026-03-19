package com.example.team3Project.domain.policy.exception;

// extends Runtime exception - 실행 중 발생하는 오류
public class BlockedWordAlreadyExistsException extends RuntimeException{
    public BlockedWordAlreadyExistsException(String message){
        super(message);
    }
}
