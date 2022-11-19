package com.example.account.exception;

import com.example.account.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException{ //요즘 checked exception 잘 안씀, 거의 RuntimeException 자주 씀.
    private ErrorCode errorCode;
    private String errorMessgage;

    public AccountException(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.errorMessgage = errorCode.getDescription();
    }
}
