package com.livewithoutthinking.resq.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;
    private int status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;
   public ApiResponse(T data, String message, int status){
       this.data = data;
       this.message = message;
       this.status = status;
   }
    public ApiResponse() {
    }
    //success
    public static <T> ApiResponse<T>
            success(T data,String message){
        return new ApiResponse<>
                (data,message,200);
    }
    public static <T> ApiResponse<T> errorValidation(String message, List<String> errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(500);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }

    //created
    public static <T> ApiResponse<T>
    created(T data,String message){
        return new ApiResponse<>
                (data,message,201);
    }
    //not found
    public static <T> ApiResponse<T>
    notfound(T data,String message){
        return new ApiResponse<>
                (null,message,404,null);
    }
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(null, message, 200, null);
    }
    //badRequest
    public static <T> ApiResponse<Object>
    badRequest(BindingResult bindingResult){
        List<String> errorsBadrequest = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
        return  new ApiResponse<>
                (null,"Validation errors",400,errorsBadrequest);
    }
    // ✅ BAD REQUEST – lỗi dạng Map<field, message>
    public static ApiResponse<Object> badRequest(Map<String, String> errors) {
        return new ApiResponse<>(null, "Validation errors", 400, errors);
    }

    // ✅ BAD REQUEST – lỗi dạng List<String>
    public static ApiResponse<Object> badRequest(List<String> errorList) {
        return new ApiResponse<>(null, "Validation errors", 400, errorList);
    }

    public static <T> ApiResponse<T> conflictData(T data, String message) {
        return new ApiResponse<>(null, message, 409, null);
    }

    //error server
    public static <T> ApiResponse<T>
    errorServer(String message){
        return new ApiResponse<>(null,message,
                500,null);
    }
}
