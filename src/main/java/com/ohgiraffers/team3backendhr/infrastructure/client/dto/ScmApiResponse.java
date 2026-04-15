package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScmApiResponse<T> {

    private boolean success;
    private T data;
    private String errorCode;
    private String message;
    private String timestamp;
}
